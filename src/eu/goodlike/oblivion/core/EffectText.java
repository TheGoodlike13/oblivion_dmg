package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;

/**
 * Effects as they appear in-game on various effectors (e.g. spells in your spell book).
 * The magnitudes of these effects can be affected by the resistance of the target or in-game difficulty
 * when actually applied.
 * The duration of these effects is 1s by default.
 * Use {@link #forSecs)} to add custom duration.
 * <p/>
 * Not to be confused with {@link Effect}, which describes active effects which have already taken all
 * the multipliers into account.
 */
public final class EffectText {

  /**
   * @return copy of this effect, but with given duration instead
   */
  public EffectText forSecs(int duration) {
    return this.duration == duration
      ? this
      : new EffectText(factor, type, magnitude, duration);
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

  /**
   * @return permanent active effect (i.e. racial bonus, equipment, etc.); unaffected by any multipliers
   */
  public Effect permanent() {
    return type.activate(magnitude, -1);
  }

  public Effect.Type getType() {
    return type;
  }

  public EffectText(Factor factor, Effect.Type type, int magnitude) {
    this(factor, type, magnitude, 1);
  }

  public EffectText(Factor factor, Effect.Type type, int magnitude, int duration) {
    this.factor = factor;
    this.type = type;
    this.magnitude = magnitude;
    this.duration = duration;
  }

  private final Factor factor;
  private final Effect.Type type;
  private final int magnitude;
  private final int duration;

  @Override
  public String toString() {
    return String.format("%s %d for %ds", type, magnitude, duration);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EffectText that = (EffectText)o;
    return magnitude == that.magnitude &&
      duration == that.duration &&
      Objects.equals(factor, that.factor) &&
      Objects.equals(getType(), that.getType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(factor, getType(), magnitude, duration);
  }

}
