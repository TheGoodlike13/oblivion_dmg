package eu.goodlike.oblivion.core;

/**
 * Effector which can be armed.
 * In-game this corresponds to a weapon which can be equipped or a spell which can be prepared.
 * Notably this does not correspond to ammunition or poison, which are considered accessories instead.
 * <p/>
 * Every armament can act as the base for a {@link Hit} and therefore define its {@link HitPattern}.
 */
public interface Armament extends Effector, HitPattern {

  /**
   * @return true if this armament can be found in equipment screen, false otherwise (e.g. spell book)
   */
  boolean isEquipment();

  boolean isPhysical(); // TODO: rename and doc

  /**
   * @return time it takes to prepare this armament if not already prepared
   */
  double timeToSwap();

  /**
   * @return verb which explains the initiation of attack, e.g. for 'you swing the blade', return 'swing'
   */
  String describeAction();

}
