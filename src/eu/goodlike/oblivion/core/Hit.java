package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import eu.goodlike.oblivion.core.source.Equipment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static eu.goodlike.oblivion.core.Source.ARROW;
import static eu.goodlike.oblivion.core.Source.BOW;
import static eu.goodlike.oblivion.core.Source.MELEE;
import static eu.goodlike.oblivion.core.Source.POISON;
import static java.util.stream.Collectors.joining;

public final class Hit implements Iterable<Carrier> {

  @Override
  public Iterator<Carrier> iterator() {
    return carriers.iterator();
  }

  public Hit(Carrier... carriers) {
    this(Arrays.asList(carriers));
  }

  public Hit(List<Carrier> carriers) {
    this.carriers = ensureOrderAndEquipment(carriers);

    StructureException.throwOnInvalidHit(hitTrace());
  }

  private final List<Carrier> carriers;

  private List<Carrier> ensureOrderAndEquipment(List<Carrier> carriers) {
    List<Carrier> mutableCopy = new ArrayList<>(carriers);

    if (BOW.any(mutableCopy)) {
      ensure(ARROW, mutableCopy);
    }
    else if (ARROW.any(mutableCopy)) {
      ensure(BOW, mutableCopy);
    }
    else if (POISON.any(mutableCopy)) {
      ensure(MELEE, mutableCopy);
    }

    return ImmutableList.sortedCopyOf(mutableCopy);
  }

  private void ensure(Equipment equipment, List<Carrier> carriers) {
    if (!equipment.any(carriers)) {
      carriers.add(equipment.withNoEffect());
    }
  }

  private String hitTrace() {
    return carriers.stream()
      .map(Carrier::getSource)
      .map(Source::toString)
      .collect(joining(" + "));
  }

  @Override
  public String toString() {
    return carriers.stream()
      .map(Carrier::toString)
      .collect(joining(" + "));
  }

}
