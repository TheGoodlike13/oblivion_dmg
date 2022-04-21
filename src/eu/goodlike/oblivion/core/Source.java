package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import eu.goodlike.oblivion.core.source.Equipment;
import eu.goodlike.oblivion.core.source.Magic;
import eu.goodlike.oblivion.core.source.Poison;

import java.util.Arrays;
import java.util.List;

public interface Source extends Comparable<Source> {

  Equipment MELEE = new Equipment("MELEE");
  Equipment BOW = new Equipment("BOW");
  Equipment ARROW = new Equipment("ARROW");
  Poison POISON = Poison.getInstance();
  Magic SPELL = Magic.INSTANCE;
  Equipment STAFF = new Equipment("STAFF");

  List<Source> ORDER = ImmutableList.of(MELEE, BOW, ARROW, POISON, SPELL, STAFF);

  default Carrier withNoEffect() {
    return create("EMPTY_" + toString());
  }

  default Carrier create(String name, EffectText... effects) {
    return create(name, Arrays.asList(effects));
  }

  Carrier create(String name, List<EffectText> effects);

  @Override
  String toString();

  @Override
  default int compareTo(Source other) {
    return ORDER.indexOf(this) - ORDER.indexOf(other);
  }

  default boolean matches(Carrier carrier) {
    return carrier.getSource() == this;
  }

  default boolean any(Iterable<? extends Carrier> carriers) {
    for (Carrier carrier : carriers) {
      if (matches(carrier)) {
        return true;
      }
    }
    return false;
  }

}
