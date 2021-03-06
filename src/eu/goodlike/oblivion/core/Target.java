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
   * Damage the target's HP by given amount.
   * Negative values are undefined.
   */
  void damage(double dmg);

  /**
   * Drain this target's HP by given amount.
   * Negative values can be used to remove the drain.
   * Does not affect dead targets.
   *
   * @return true if it was applied, false if the target was already dead
   */
  boolean drain(double hp);

  /**
   * Inform this target that an effect was applied with given magnitude & duration.
   * Calling this implies the effect has no other effect upon application.
   * Does nothing by default, but can be overridden to collect or print some information.
   */
  default void poke(double magnitude, double duration) {
  }

}
