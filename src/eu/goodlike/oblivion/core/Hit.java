package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import eu.goodlike.oblivion.core.source.Equipment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static eu.goodlike.oblivion.core.Source.ARROW;
import static eu.goodlike.oblivion.core.Source.BOW;
import static eu.goodlike.oblivion.core.Source.MELEE;
import static eu.goodlike.oblivion.core.Source.POISON;
import static eu.goodlike.oblivion.core.Source.STAFF;
import static java.util.stream.Collectors.joining;

/**
 * Combination of carriers which apply their effects at once upon a hit.
 * For example: enchanted bow which fires an enchanted arrow with poison.
 * All of them apply their effects at once (i.e. cannot influence each other for this hit).
 * This class ensures the combination of carriers is faithful to the game.
 * <p/>
 * The order of carriers in this hit is consistent with their natural ordering.
 */
public final class Hit implements Iterable<Carrier> {

  public Optional<Carrier> getWeapon() {
    return carriers.stream()
      .filter(c -> c.getSource() instanceof Equipment)
      .filter(c -> !c.getSource().equals(ARROW))
      .findFirst();
  }

  public Optional<Carrier> requiresSwap(Carrier oldWeapon) {
    return getWeapon()
      .filter(newWeapon -> !newWeapon.equals(oldWeapon));
  }

  public boolean requiresCooldown(Hit next) {
    return getWeapon().flatMap(next::requiresSwap).isPresent()
      || !next.getWeapon().isPresent()
      || getWeapon().map(Carrier::getSource).filter(STAFF::equals).isPresent();
  }

  @Override
  public Iterator<Carrier> iterator() {
    return carriers.iterator();
  }

  public Hit(Carrier... carriers) {
    this(Arrays.asList(carriers));
  }

  /**
   * Creates a new hit from given carriers.
   * They are sorted according to their natural ordering.
   * <p/>
   * In some cases, the carriers will be supplemented with implicit equipment with no enchants.
   * For example: if there is an arrow, a bow will be added if missing.
   * This removes the need to specify them explicitly.
   * Poison prefers a melee weapon if it cannot be decided otherwise.
   *
   * @throws StructureException if the hit resulting from given carriers is completely invalid
   */
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

  private void ensure(Source equipment, List<Carrier> carriers) {
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
