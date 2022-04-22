package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableSet;

/**
 * Effects as they appear in-game on various carriers (e.g. spells in your spell book).
 * The magnitudes of these effects can be affected by the resistance of the target or in-game difficulty
 * when actually applied.
 * The duration of these effects is 1s by default.
 * Use {@link #forSecs(double)} to add custom duration.
 * <p/>
 * Not to be confused with {@link Effect}, which describes active effects which have already taken all
 * the multipliers into account.
 */
public final class EffectText {

  /**
   * @return copy of this effect, but with given duration instead
   */
  public EffectText forSecs(double duration) {
    return new EffectText(factor, type, magnitude, duration);
  }

  /**
   * @param method the way this effect is applied ({@link Factor#MAGIC} or {@link Factor#POISON})
   * @param target the affected unit; used to determine resistance to the effect
   * @return active effect with all magnitude multipliers taken into account
   */
  public Effect activate(Method method, Target target) {
    double magnitudeSnapshot = magnitude;
    for (Factor f : ImmutableSet.of(method, factor)) {
      magnitudeSnapshot *= target.getMultiplier(f);
    }
    if (type.affectsHp()) {
      magnitudeSnapshot *= method.damageMultiplier();
    }
    return type.activate(magnitudeSnapshot, duration);
  }

  public Effect.Type getType() {
    return type;
  }

  public EffectText(Factor factor, Effect.Type type, double magnitude) {
    this(factor, type, magnitude, 1);
  }

  public EffectText(Factor factor, Effect.Type type, double magnitude, double duration) {
    this.factor = factor;
    this.type = type;
    this.magnitude = magnitude;
    this.duration = duration;
  }

  private final Factor factor;
  private final Effect.Type type;
  private final double magnitude;
  private final double duration;

  @Override
  public String toString() {
    return String.format("%s %.2f for %.1fs", type, magnitude, duration);
  }

}
