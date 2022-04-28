package eu.goodlike.oblivion.core.effect;

import eu.goodlike.oblivion.Settings;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.Target;

import static eu.goodlike.oblivion.Settings.TICK;

/**
 * Common logic for active effects.
 * Ensures that ticking is done using {@link Settings#TICK}.
 */
public abstract class BaseEffect implements Effect {

  /**
   * Called by {@link #onTick)} to influence the target in proportion to the exact amount of time
   * used in the last {@link Settings#TICK}.
   * In most cases, given tick will be equal to {@link Settings#TICK}.
   * However, when the effect is about to expire and the remaining duration is less than the tick,
   * it will be equal to remaining duration instead.
   * After the effect is expired, it will always be 0.
   * This ensures large values of {@link Settings#TICK} do not cause the effect to influence the target
   * more than the magnitude would suggest.
   * <p/>
   * Undefined if target is null or tick is negative.
   */
  protected abstract void onEffectiveTick(Target target, double tick);

  @Override
  public final void onTick(Target target) {
    double effectiveTick = Math.min(TICK, remaining);
    onEffectiveTick(target, effectiveTick);
    remaining -= effectiveTick;

    expireGracefully(target);
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

  private void expireGracefully(Target target) {
    if (!hasExpired() && remaining < BASICALLY_EXPIRED) {
      onTick(target);
    }
  }

  private static final double BASICALLY_EXPIRED = 1d / 1_000_000_000;

}
