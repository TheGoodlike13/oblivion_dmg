package eu.goodlike.oblivion.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import eu.goodlike.oblivion.Global.Settings;
import eu.goodlike.oblivion.Neaterator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static eu.goodlike.oblivion.Global.Settings.LEVEL;
import static eu.goodlike.oblivion.Global.Settings.TICK;
import static eu.goodlike.oblivion.core.Effector.Factory.SPELL;

/**
 * Target for all of the hits.
 */
public final class Enemy implements Target {

  /**
   * This method will sometimes return a number equivalent to a floating point calculation error.
   * Such targets are still considered dead, but for clarity, will only have 0 HP when rounded.
   *
   * @return exact amount of health remaining, never less than 0
   */
  public double healthRemaining() {
    return Math.max(0, health);
  }

  /**
   * Overkill does not include drain effects applied after death.
   *
   * @return exact amount of damage taken, including overkill
   */
  public double damageTaken() {
    return maxHealth - health;
  }

  /**
   * Overkill does not include drain effects applied after death.
   *
   * @return amount of damage taken after death, 0 if target is still alive
   */
  public double overkill() {
    return Math.max(0, -health);
  }

  /**
   * Target is considered dead if when rounded to 2 digits, its remaining health is zero.
   *
   * @return true if the target is dead, false otherwise
   */
  public boolean isAlive() {
    return health >= BASICALLY_DEAD;
  }

  /**
   * @return true if there are any active effects on the target, false otherwise
   */
  public boolean isAffected() {
    return !activeEffects.isEmpty();
  }

  /**
   * Shorthand for {@link #hit(Hit)}, helps with testing.
   * The actual hit will be a unique spell.
   */
  public void hit(EffectText... effects) {
    hit(SPELL, effects);
  }

  /**
   * Shorthand for {@link #hit(Hit)}, helps with testing.
   * The actual hit will be unique.
   */
  public void hit(Category<?> category, EffectText... effects) {
    hit(category.create(effects));
  }

  /**
   * Shorthand for {@link #hit(Hit)}, helps with testing.
   * If the effector is not an armament, it will be supplemented with one.
   * See {@link Hit#Hit(Iterable)} for details.
   */
  public void hit(Effector byItself) {
    hit(new Hit(byItself));
  }

  public void hit(Hit hit) {
    hit(hit, Observer.NONE);
  }

  /**
   * Hits this target.
   * All the effects from all the effectors are applied at once, consistent with in-game logic.
   * If any of the effectors has hit this target before, may replace existing effects instead of stacking.
   */
  public void hit(Hit hit, Observer observer) {
    Dummy dummy = new Dummy();
    Target target = observer.observing(dummy);

    for (Effector effector : hit) {
      for (EffectText e : effector) {
        Effect.Id id = effector.toId(e);
        Effect effect = e.activate(effector.getMethod(), this);

        observer.next(id);
        Effect original = activeEffects.put(id, effect);
        if (original != null) {
          observer.markForRemoval(original);
          original.onRemove(this);
        }
        effect.onApply(target);
      }
    }

    dummy.applyResistModsAtOnce(this);
    cleanupEffects(observer, false);
  }

  public void tick() {
    tick(Observer.NONE);
  }

  /**
   * Performs a tick on every active effect on this target.
   * If any of them expire as a result of this tick, they are removed.
   * Tick length is configured in {@link Settings#TICK}.
   */
  public void tick(Observer observer) {
    observer.tick();
    cleanupEffects(observer, true);
  }

  public void tick(double seconds) {
    tick(seconds, Observer.NONE);
  }

  /**
   * Performs ticks for an equivalent of given amount of seconds.
   * Tick length is configured in {@link Settings#TICK}.
   */
  public void tick(double seconds, Observer observer) {
    double total = 0;
    while (total < seconds) {
      tick(observer);
      total += TICK;
    }
  }

  public void resolve() {
    resolve(Observer.NONE);
  }

  /**
   * Ticks until all active effects have expired.
   */
  public void resolve(Observer observer) {
    while (isAffected()) {
      tick(observer);
    }
  }

  /**
   * @return this enemy with all active effects wiped and HP reset
   */
  public Enemy resurrect() {
    resolve();
    this.health = maxHealth;
    return this;
  }

  /**
   * Sets this enemy as leveled, allowing it to scale with {@link Settings#LEVEL} of the player.
   * Only HP is scaled with the level.
   * Scaling is not automatic - you must call {@link #updateLevel} to scale the enemy.
   *
   * @return this enemy, leveled to player; wipes all active effects and resets HP as well
   * @throws StructureException if this enemy is already leveled
   * @throws StructureException if level multiplier is not positive
   * @throws StructureException if min level is not positive
   * @throws StructureException if max level is not larger than min level
   */
  public Enemy setLeveled(int levelMultiplier, int minLevel, int maxLevel) {
    if (level > 0) {
      throw new StructureException("Enemy is already leveled: " + this);
    }

    this.levelMultiplier = StructureException.natOrThrow(levelMultiplier, "level multiplier");

    StructureException.natOrThrow(minLevel, "min level");
    this.minLevel = Math.max(1, minLevel);

    if (maxLevel <= minLevel) {
      throw new StructureException("Max level must exceed min level: min=<" + minLevel + "> max=<" + maxLevel + ">");
    }
    this.maxLevel = Math.max(minLevel, maxLevel);

    this.level = minLevel;
    return updateLevel();
  }

  /**
   * @return this enemy, leveled to player (if leveled); wipes all active effects and resets HP as well
   */
  public Enemy updateLevel() {
    int diff = confine(LEVEL) - confine(level);

    level = LEVEL;
    maxHealth = maxHealth + diff * levelMultiplier;
    return resurrect();
  }

  @Override
  public double getMultiplier(Factor factor) {
    return Math.max(0, getWeakness(factor) / 100);
  }

  @Override
  public void modifyResist(Factor factor, double percent) {
    double currentPc = getWeakness(factor);
    weaknessPercent.put(factor, currentPc - percent);
  }

  @Override
  public void damage(double dmg) {
    health -= dmg;
  }

  @Override
  public boolean drain(double hp) {
    if (isAlive()) {
      health -= hp;
      return true;
    }
    return false;
  }

  public Enemy(int maxHealth, EffectText... bonus) {
    this(maxHealth, Arrays.asList(bonus));
  }

  /**
   * Creates a new enemy with given max health and given permanent effects.
   * The enemy starts at max HP.
   * The effects are equivalent to racial or passive equipment bonuses that various characters can have.
   * Damage effects are ignored.
   *
   * @throws StructureException if maxHealth is too low to be considered alive
   */
  public Enemy(int maxHealth, Iterable<EffectText> bonus) {
    this.maxHealth = maxHealth;
    this.health = maxHealth;
    StructureException.throwOnAlreadyDead(this);

    this.weaknessPercent = new HashMap<>();

    this.activeEffects = new LinkedHashMap<>();

    for (EffectText effect : bonus) {
      effect.permanent().onApply(this);
    }
  }

  private int maxHealth;
  private double health;

  private final Map<Factor, Double> weaknessPercent;

  private final Map<Effect.Id, Effect> activeEffects;

  private int levelMultiplier;
  private int minLevel;
  private int maxLevel;

  private int level;

  private void cleanupEffects(Observer observer, boolean isTicking) {
    Target target = observer.observing(this);

    new Neaterator<>(activeEffects).forEach((i, id, effect) -> {
      observer.next(id);

      if (isTicking) {
        effect.onTick(target);
      }

      if (effect.hasExpired()) {
        i.remove();
        observer.markForRemoval(effect);
        effect.onRemove(target);
      }
    });
  }

  private double getWeakness(Factor factor) {
    return weaknessPercent.computeIfAbsent(factor, any -> NO_WEAKNESS);
  }

  private int confine(int level) {
    return Math.max(minLevel, Math.min(level, maxLevel));
  }

  private static final double NO_WEAKNESS = 100;
  private static final double BASICALLY_DEAD = 0.005;

  @Override
  public String toString() {
    return level() + healthStatus() + multipliers();
  }

  public String healthStatus() {
    return String.format("%.1f/%d", health, maxHealth);
  }

  private String level() {
    return confine(level) <= 0 ? "" : "[LVL " + confine(level) + "] ";
  }

  private String multipliers() {
    StringBuilder b = new StringBuilder();
    for (Factor factor : Factor.ALL) {
      double multiplier = getMultiplier(factor);
      if (multiplier != 1) {
        b.append(" ")
          .append(factor)
          .append("x")
          .append(String.format("%.2f", multiplier));
      }
    }
    return b.toString();
  }

  /**
   * Collects information about some activity on the enemy.
   */
  public interface Observer {
    /**
     * Observes actual target.
     * All methods are passed through, but information is collected as well.
     * Should be called for every hit or tick.
     */
    Target observing(Target actual);

    /**
     * Informs this observer that a tick is about to be processed.
     * If this is not called after {@link #observing}, it assumes we're observing a hit.
     */
    void tick();

    /**
     * Sets given effect id as the next to be processed.
     */
    void next(Effect.Id id);

    /**
     * Marks the effect being processed as having expired or replaced.
     *
     * @param effect active effect associated with the effect being processed
     */
    void markForRemoval(Effect effect);

    /**
     * Non-observing observer, stand-in for null.
     */
    Observer NONE = new Observer() {
      @Override
      public Target observing(Target actual) {
        return actual;
      }

      @Override
      public void tick() {
      }

      @Override
      public void next(Effect.Id id) {
      }

      @Override
      public void markForRemoval(Effect effect) {
      }
    };
  }

  private final class Dummy implements Target {
    @Override
    public void modifyResist(Factor factor, double percent) {
      allMods.put(factor, percent);
    }

    @Override
    public void damage(double dmg) {
      Enemy.this.damage(dmg);
    }

    @Override
    public boolean drain(double hp) {
      return Enemy.this.drain(hp);
    }

    public void applyResistModsAtOnce(Target target) {
      allMods.forEach(target::modifyResist);
    }

    private final ListMultimap<Factor, Double> allMods = ArrayListMultimap.create();
  }

}
