package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableSet;

public final class EffectText {

  public EffectText forSecs(double duration) {
    return new EffectText(factor, type, magnitude, duration);
  }

  public Effect activate(Method method, Actor target) {
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

}
