package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import eu.goodlike.oblivion.core.method.Poison;
import eu.goodlike.oblivion.core.source.Equipment;
import eu.goodlike.oblivion.core.source.Spell;

import java.util.Arrays;
import java.util.List;

import static eu.goodlike.oblivion.Global.Settings.EMIT;
import static eu.goodlike.oblivion.Global.Settings.SHOOT;
import static eu.goodlike.oblivion.Global.Settings.STRIKE;
import static eu.goodlike.oblivion.Global.Settings.SWAP_BOW;
import static eu.goodlike.oblivion.Global.Settings.SWAP_MELEE;
import static eu.goodlike.oblivion.Global.Settings.SWAP_STAFF;

/**
 * Unique way to carry effects.
 * Only some of these ways are compatible with each other for creating a real {@link Hit}.
 * All possible sources are defined in this interface.
 * <p/>
 * Sources have a natural ordering which defines how they should be processed as part of a single {@link Hit}.
 */
public interface Source extends HitPattern, Comparable<Source> {

  Source MELEE = new Equipment("MELEE", "swing", STRIKE, SWAP_MELEE, true);
  Source BOW = new Equipment("BOW", "aim", SHOOT, SWAP_BOW, true);
  Source ARROW = new Equipment("ARROW");
  Poison POISON = Poison.getInstance();
  Source SPELL = new Spell();
  Source STAFF = new Equipment("STAFF", "invoke", EMIT, SWAP_STAFF, false);

  List<Source> ALL_IN_ORDER = ImmutableList.of(ARROW, MELEE, POISON, BOW, SPELL, STAFF);

  /**
   * @return equivalent to {@link #create} with no params, but always returns the same instance
   */
  default Effector withNoEffect() {
    return create();
  }

  default Effector create(EffectText... effects) {
    return create(Arrays.asList(effects));
  }

  default Effector create(List<EffectText> effects) {
    return create(null, effects);
  }

  /**
   * Creates a new effector with given effects.
   * This source is effectively the type of the new effector.
   * Hits from this effector will always stack with other effectors.
   * <p/>
   * The label is optional and can be null. It's entirely cosmetic (affects only {@link #toString}).
   *
   * @throws StructureException if any of the given effects are duplicate
   */
  Effector create(String label, List<EffectText> effects);

  default boolean isWeapon() {
    return false;
  }

  default boolean isPhysical() {
    return false;
  }

  default double timeToSwap() {
    return 0;
  }

  @Override
  default double timeToHit(int combo) {
    return 0;
  }

  @Override
  default double cooldown(int combo) {
    return 0;
  }

  /**
   * @return unique name of the source; can be used to identify it, but need not match variable name
   */
  @Override
  String toString();

  default String describeAction() {
    return "";
  }

  @Override
  default int compareTo(Source other) {
    return ALL_IN_ORDER.indexOf(this) - ALL_IN_ORDER.indexOf(other);
  }

  /**
   * @return true if any of the given effectors are of this source, false otherwise
   */
  default boolean any(Iterable<? extends Effector> effectors) {
    for (Effector effector : effectors) {
      if (this == effector.getSource()) {
        return true;
      }
    }
    return false;
  }

}
