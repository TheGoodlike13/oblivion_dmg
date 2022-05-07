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

  /**
   * Some armaments will ignore cooldown when chaining consecutive hits.
   * In that case the delay between such hits will be equivalent to {@link #timeToHit}.
   * Other armaments incur a cooldown and the time between their hits will be equivalent to
   * {@link #cooldown} + {@link #timeToHit}.
   * For all situations where cooldown is incurred or cancelled refer to {@link Hit#requiresCooldownAfter}.
   *
   * @return true if consecutive hits with this armament incur a cooldown, false if cooldown is skipped
   */
  boolean isRigid();

  /**
   * @return time it takes to prepare this armament if not already prepared
   */
  double timeToSwap();

  /**
   * @return verb which explains the initiation of attack, e.g. for 'you swing the blade', return 'swing'
   */
  String describeAction();

}
