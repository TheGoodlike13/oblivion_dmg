package eu.goodlike.oblivion.core;

/**
 * Entity which receives effects.
 * Called by {@link Effect} to influence or damage this target.
 */
public interface Target {

  /**
   * @return this target's magnitude multiplier for given factor using resistance or weakness;
   * 1 implies no resistance or weakness;
   * 0 to 1 implies resistance;
   * more than 1 implies weakness;
   * never negative
   */
  default double getMultiplier(Factor factor) {
    return 1;
  }

  /**
   * Adds given amount of percent of resistance to given factor to this target.
   * Negative amounts add weakness instead.
   */
  void modifyResist(Factor factor, double percent);

  /**
   * Damage the target's hp by given amount.
   * Negative values are undefined.
   */
  void damage(double dmg);

  /**
   * Drain this target's hp by given amount.
   * Negative values can be used to remove the drain.
   * Does not affect dead targets.
   */
  void drain(double hp);

}
