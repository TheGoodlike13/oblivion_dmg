package eu.goodlike.oblivion.core.effect;

import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.Target;

import static eu.goodlike.oblivion.global.Settings.TICK;

public abstract class BaseEffect implements Effect {

  protected abstract void onEffectiveTick(Target target, double tick);

  @Override
  public final void onTick(Target target) {
    double effectiveTick = Math.min(TICK, remaining);
    onEffectiveTick(target, effectiveTick);
    remaining -= effectiveTick;
  }

  @Override
  public boolean hasExpired() {
    return remaining <= 0;
  }

  protected BaseEffect(double magnitude, double duration) {
    this.magnitude = magnitude;
    this.remaining = duration;
  }

  protected final double magnitude;

  private double remaining;

}
