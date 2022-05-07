package eu.goodlike.oblivion.core;

/**
 * Describes timings for some underlying {@link Hit}.
 * <p/>
 * Hits are modeled like this:
 * <p/>1) You initiate the hit.
 * <p/>2) Some time is consumed by the swing animation.
 * <p/>3) The hit lands.
 * <p/>4) Some time is consumed by the hit cooldown animation.
 * <p/>5) Back to 1)
 * <p/>
 * In some cases step 4) can be cancelled (please refer to {@link Hit#requiresCooldownAfter}).
 * <p/>
 * It is possible for consecutive hits to have different timings.
 * Then the timings are defined by the amount of such hits so far.
 * In other words, a combo count.
 */
public interface HitPattern {

  double timeToHit(int combo);
  double cooldown(int combo);

  interface Builder {
    Builder combo(double nextTimeToHit, double nextCooldown);
    HitPattern build();
  }

}
