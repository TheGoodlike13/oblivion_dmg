package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;

import static eu.goodlike.oblivion.Global.Settings.EFFECTIVENESS;

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

  public EffectText instant() {
    return forSecs(0);
  }

  /**
   * @return copy of this effect, but with given duration instead
   * @throws StructureException if duration is < 0
   */
  public EffectText forSecs(int duration) {
    StructureException.natOrZeroOrThrow(duration, "effect duration");
    return this.duration == duration
      ? this
      : new EffectText(factor, type, magnitude, duration, unscaled);
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

  /**
   * This method should only be called by spells.
   * <p/>
   * If effectiveness is not exactly 100, there will be an asterisk near the magnitude of the returned effect.
   *
   * @return this effect but scaled by the factor of current spell effectiveness
   */
  public EffectText scale() {
    if (EFFECTIVENESS == 100) {
      return unscaled == null ? this : unscaled;
    }

    int scaledMagnitude = scaledMagnitude();
    if (unscaled == null) {
      return new EffectText(factor, type, scaledMagnitude, duration, this);
    }

    return scaledMagnitude == magnitude
      ? this
      : new EffectText(factor, type, magnitude, duration, unscaled);
  }

  public EffectText(Factor factor, Effect.Type type, int magnitude) {
    this(factor, type, magnitude, 1);
  }

  public EffectText(Factor factor, Effect.Type type, int magnitude, int duration) {
    this(factor, type, magnitude, duration, null);
  }

  public EffectText(Factor factor, Effect.Type type, int magnitude, int duration, EffectText unscaled) {
    this.factor = factor;
    this.type = type;
    this.magnitude = magnitude;
    this.duration = duration;
    this.unscaled = unscaled;
  }

  private final Factor factor;
  private final Effect.Type type;
  private final int magnitude;
  private final int duration;

  private final EffectText unscaled;

  private int scaledMagnitude() {
    int baseMagnitude = unscaled == null ? magnitude : unscaled.magnitude;
    return baseMagnitude * EFFECTIVENESS / 100;
  }

  @Override
  public String toString() {
    return type + " " + magnitude + scaleIndicator() + durationIndicator();
  }

  private String scaleIndicator() {
    return unscaled == null ? "" : "*";
  }

  private String durationIndicator() {
    return duration == 0 ? " (instant)" : " for " + duration + "s";
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
