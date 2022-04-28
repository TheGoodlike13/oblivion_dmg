package eu.goodlike.oblivion.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import eu.goodlike.oblivion.Global.Settings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

  /**
   * Hits this target.
   * All the effects from all the carriers are applied at once, consistent with in-game logic.
   * If any of the carriers has hit this target before, may replace existing effects instead of stacking.
   */
  public void hit(Hit hit) {
    Dummy dummy = new Dummy();

    for (Carrier carrier : hit) {
      for (EffectText e : carrier) {
        Effect.Id id = carrier.toId(e);
        Effect effect = e.activate(carrier.getMethod(), this);

        Effect original = activeEffects.put(id, effect);
        if (original != null) {
          original.onRemove(this);
        }

        effect.onApply(dummy);
      }
    }

    dummy.applyTo(this);
  }

  /**
   * Performs a tick on every active effect on this target.
   * If any of them expire as a result of this tick, they are removed.
   * Tick length is configured in {@link Settings#TICK}.
   */
  public void tick() {
    Iterator<Map.Entry<Effect.Id, Effect>> i = activeEffects.entrySet().iterator();
    while (i.hasNext()) {
      Map.Entry<Effect.Id, Effect> next = i.next();
      Effect effect = next.getValue();

      effect.onTick(this);
      if (effect.hasExpired()) {
        i.remove();
        effect.onRemove(this);
      }
    }
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

  /**
   * Ticks until all active effects have expired.
   * @return time it took for the remaining effects to expire, 0 if there were no such effects
   */
  public double resolve() {
    double total = 0;
    while (isAffected()) {
      tick();
      total += TICK;
    }
    return total;
  }

  /**
   * Wipes all active effects and sets hp back to max.
   */
  public void resurrect() {
    resolve();
    this.health = maxHealth;
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

  public Enemy(double maxHealth, EffectText... bonus) {
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
  public Enemy(double maxHealth, Iterable<EffectText> bonus) {
    this.maxHealth = maxHealth;
    this.health = maxHealth;

    this.weaknessPercent = new HashMap<>();

    this.activeEffects = new HashMap<>();

    for (EffectText effect : bonus) {
      effect.permanent().onApply(this);
    }

    StructureException.throwOnAlreadyDead(this);
  }

  private double maxHealth;
  private double health;

  private final Map<Factor, Double> weaknessPercent;

  private final Map<Effect.Id, Effect> activeEffects;

  private double getWeakness(Factor factor) {
    return weaknessPercent.computeIfAbsent(factor, any -> NO_WEAKNESS);
  }

  private static final double NO_WEAKNESS = 100;
  private static final double BASICALLY_DEAD = 0.005;

  private static final class Dummy implements Target {
    @Override
    public void modifyResist(Factor factor, double percent) {
      allMods.put(factor, percent);
    }

    @Override
    public void damage(double dmg) {
      totalDmg += dmg;
    }

    @Override
    public void drain(double hp) {
      totalDrain += hp;
    }

    public void applyTo(Target target) {
      allMods.forEach(target::modifyResist);
      target.damage(totalDmg);
      target.drain(totalDrain);
    }

    private final ListMultimap<Factor, Double> allMods = ArrayListMultimap.create();
    private double totalDmg = 0;
    private double totalDrain = 0;
  }

}
