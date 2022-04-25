package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import eu.goodlike.oblivion.core.source.Equipment;
import eu.goodlike.oblivion.core.source.Magic;
import eu.goodlike.oblivion.core.source.Poison;

import java.util.Arrays;
import java.util.List;

/**
 * Unique way to carry effects.
 * Only some of these ways are compatible with each other for creating a real {@link Hit}.
 * All possible sources are defined in this interface.
 * <p/>
 * Sources have a natural ordering which defines how they should be processed as part of a single {@link Hit}.
 */
public interface Source extends Comparable<Source> {

  Source MELEE = new Equipment("MELEE");
  Source BOW = new Equipment("BOW");
  Source ARROW = new Equipment("ARROW");
  Source POISON = Poison.getInstance();
  Source SPELL = new Magic("SPELL");
  Source STAFF = new Equipment("STAFF");

  List<Source> ALL_IN_ORDER = ImmutableList.of(MELEE, BOW, ARROW, POISON, SPELL, STAFF);

  default Carrier withNoEffect() {
    return create();
  }

  default Carrier create(EffectText... effects) {
    return create(Arrays.asList(effects));
  }

  /**
   * Creates a new carrier with given effects.
   * This source is effectively the type of the new carrier.
   * Hits from this carrier will always stack with other carriers.
   *
   * @throws StructureException if any of the given effects are duplicate
   */
  Carrier create(List<EffectText> effects);

  /**
   * @return unique name of the source; can be used to identify it, but need not match variable name
   */
  @Override
  String toString();

  @Override
  default int compareTo(Source other) {
    return ALL_IN_ORDER.indexOf(this) - ALL_IN_ORDER.indexOf(other);
  }

  /**
   * @return true if any of the given carriers are of this source, false otherwise
   */
  default boolean any(Iterable<? extends Carrier> carriers) {
    for (Carrier carrier : carriers) {
      if (this == carrier.getSource()) {
        return true;
      }
    }
    return false;
  }

}
