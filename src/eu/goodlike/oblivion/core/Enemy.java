package eu.goodlike.oblivion.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import eu.goodlike.oblivion.Global.Settings;
import eu.goodlike.oblivion.Neaterator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static eu.goodlike.oblivion.Global.Settings.LEVEL;
import static eu.goodlike.oblivion.Global.Settings.TICK;
import static eu.goodlike.oblivion.core.Source.SPELL;

/**
 * Target for all of the hits.
 */
public final class Enemy implements Target {

  /**
   * This method will sometimes return a number equivalent to a floating point calculation error.
   * Such targets are still considered dead, but for clarity, will only have 0 hp when rounded.
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
  public void hit(Source source, EffectText... effects) {
    hit(source.create(effects));
  }

  /**
   * Shorthand for {@link #hit(Hit)}, helps with testing.
   * If the carrier cannot be considered a hit by itself, it will be supplemented with enchanted equipment.
   * See {@link Hit#Hit(List)} for details.
   */
  public void hit(Carrier byItself) {
    hit(new Hit(byItself));
  }

  public void hit(Hit hit) {
    hit(hit, Observer.NONE);
  }

  /**
   * Hits this target.
   * All the effects from all the carriers are applied at once, consistent with in-game logic.
   * If any of the carriers has hit this target before, may replace existing effects instead of stacking.
   */
  public void hit(Hit hit, Observer observer) {
    Dummy dummy = new Dummy();
    Target target = observer.observing(dummy);

    for (Carrier carrier : hit) {
      for (EffectText e : carrier) {
        Effect.Id id = carrier.toId(e);
        Effect effect = e.activate(carrier.getMethod(), this);

        observer.next(id);
        Effect original = activeEffects.put(id, effect);
        if (original != null) {
          observer.markExpired();
          original.onRemove(this);
        }
        effect.onApply(target);
      }
    }

    dummy.applyResistModsAtOnce(this);
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
    Target target = observer.observing(this);
    observer.tick();

    new Neaterator<>(activeEffects).forEach((i, id, effect) -> {
      observer.next(id);

      effect.onTick(target);

      if (effect.hasExpired()) {
        i.remove();
        observer.markExpired();
        effect.onRemove(target);
      }
    });
  }

  /**
   * Performs ticks for an equivalent of given amount of seconds.
   * Tick length is configured in {@link Settings#TICK}.
   */
  public void tick(double seconds) {
    double total = 0;
    while (total < seconds) {
      tick();
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
   * Wipes all active effects and sets hp back to max.
   */
  public void resurrect() {
    resolve();
    this.health = maxHealth;
  }

  public Enemy setLeveled(int levelMultiplier, int minLevel, int maxLevel) {
    if (level > 0) {
      throw new StructureException("Enemy is already leveled: " + this);
    }
    StructureException.natOrThrow(levelMultiplier, "level multiplier");
    StructureException.natOrThrow(minLevel, "level multiplier");
    if (maxLevel <= minLevel) {
      throw new StructureException("Max must be higher than min, but was: <" + maxLevel + " <= " + minLevel + ">");
    }

    this.levelMultiplier = levelMultiplier;
    this.minLevel = Math.max(1, minLevel);
    this.maxLevel = Math.max(minLevel, maxLevel);

    this.level = minLevel;
    updateLevel();
    return this;
  }

  public void updateLevel() {
    int diff = confine(LEVEL) - confine(level);

    level = LEVEL;
    maxHealth = maxHealth + diff * levelMultiplier;
    resurrect();
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
  public void drain(double hp) {
    if (isAlive()) {
      health -= hp;
    }
  }

  /**
   * @return current health over max health
   */
  public String healthStatus() {
    return String.format("%.1f/%d", health, maxHealth);
  }

  public Enemy(int maxHealth, EffectText... bonus) {
    this(maxHealth, Arrays.asList(bonus));
  }

  /**
   * Creates a new enemy with given max health and given permanent effects.
   * The enemy starts at max hp.
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
     */
    void markExpired();

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
      public void markExpired() {
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
    public void drain(double hp) {
      Enemy.this.drain(hp);
    }

    public void applyResistModsAtOnce(Target target) {
      allMods.forEach(target::modifyResist);
    }

    private final ListMultimap<Factor, Double> allMods = ArrayListMultimap.create();
  }

}
