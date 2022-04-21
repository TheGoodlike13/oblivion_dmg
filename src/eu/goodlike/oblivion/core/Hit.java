package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import eu.goodlike.oblivion.core.source.Equipment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static eu.goodlike.oblivion.core.Source.ARROW;
import static eu.goodlike.oblivion.core.Source.BOW;
import static eu.goodlike.oblivion.core.Source.MELEE;
import static eu.goodlike.oblivion.core.Source.POISON;
import static java.util.stream.Collectors.joining;

public final class Hit implements Iterable<Carrier> {

  public boolean isMelee() {
    return MELEE.matches(baseCarrier());
  }

  public boolean isBow() {
    return BOW.matches(baseCarrier());
  }

  @Override
  public Iterator<Carrier> iterator() {
    return carriers.iterator();
  }

  public Hit(Carrier... carriers) {
    this(Arrays.asList(carriers));
  }

  public Hit(List<Carrier> carriers) {
    this.carriers = ensureOrderAndEquipment(carriers);

    if (!VALID_HITS.contains(hitTrace())) {
      throw new StructureException("Invalid hit: " + hitTrace() + "; expected one of " + VALID_HITS);
    }
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

  private Carrier baseCarrier() {
    return carriers.get(0);
  }

  private static final Set<String> VALID_HITS = ImmutableSet.of(
    "SPELL",
    "STAFF",
    "MELEE",
    "MELEE + POISON",
    "BOW + ARROW",
    "BOW + ARROW + POISON"
  );

}
