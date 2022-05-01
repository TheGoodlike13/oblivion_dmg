package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static eu.goodlike.oblivion.core.Source.ARROW;
import static eu.goodlike.oblivion.core.Source.BOW;
import static eu.goodlike.oblivion.core.Source.MELEE;
import static eu.goodlike.oblivion.core.Source.POISON;
import static java.util.stream.Collectors.joining;

/**
 * Combination of carriers which apply their effects at once upon a hit.
 * For example: enchanted bow which fires an enchanted arrow with poison.
 * All of them apply their effects at once (i.e. cannot influence each other for this hit).
 * This class ensures the combination of carriers is faithful to the game.
 * <p/>
 * The order of carriers in this hit is consistent with their natural ordering.
 */
public final class Hit implements Iterable<Carrier>, HitPattern {

  public Source getDeliveryMechanism() {
    return getWeapon().orElse(carriers.get(0)).getSource();
  }

  public Optional<Carrier> getWeapon() {
    return carriers.stream()
      .filter(c -> c.getSource().isWeapon())
      .findFirst();
  }

  public Optional<Carrier> requiresSwap(Carrier oldWeapon) {
    return getWeapon().filter(newWeapon -> !newWeapon.equals(oldWeapon));
  }

  public boolean requiresCooldownAfter(Hit last) {
    return last != null && (!getWeapon().isPresent() || requiresSwap(last) || last.usesMysticalWeapon());
  }

  public boolean isCombo(Hit last) {
    return last != null
      && getWeapon().isPresent()
      && getWeapon().equals(last.getWeapon());
  }

  public boolean hasMultipleChunks() {
    return carriers.stream().filter(this::isExplicit).count() > 1;
  }

  @Override
  public Iterator<Carrier> iterator() {
    return carriers.iterator();
  }

  @Override
  public double timeToHit(int combo) {
    return getDeliveryMechanism().timeToHit(combo);
  }

  @Override
  public double cooldown(int combo) {
    return getDeliveryMechanism().cooldown(combo);
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

  private boolean requiresSwap(Hit last) {
    return getWeapon().flatMap(last::requiresSwap).isPresent();
  }

  private boolean usesMysticalWeapon() {
    return getWeapon()
      .map(Carrier::getSource)
      .filter(source -> !source.isPhysical())
      .isPresent();
  }

  private boolean isExplicit(Carrier carrier) {
    Carrier implicit = carrier.getSource().withNoEffect();
    return !carrier.equals(implicit);
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

  public String toPerformString() {
    return getDeliveryMechanism().describeAction() + " " + toLabelString();
  }

  public String toLabelString() {
    return carriers.stream()
      .map(Carrier::getLabel)
      .collect(joining(" + "));
  }

  public static final class Combo implements HitPattern {
    @Override
    public double timeToHit(int combo) {
      int index = combo % fullCombo.size();
      Combo actual = fullCombo.get(index);
      return actual.timeToHit;
    }

    @Override
    public double cooldown(int combo) {
      int index = combo % fullCombo.size();
      Combo actual = fullCombo.get(index);
      return actual.cooldown;
    }

    public Combo combo(double nextTimeToHit, double nextCooldown) {
      return new Combo(nextTimeToHit, nextCooldown, fullCombo);
    }

    public Combo(double timeToHit, double cooldown) {
      this(timeToHit, cooldown, new ArrayList<>());
    }

    private Combo(double timeToHit, double cooldown, List<Combo> fullCombo) {
      this.timeToHit = StructureException.positiveOrThrow(timeToHit, "time to hit");
      this.cooldown = StructureException.positiveOrThrow(cooldown, "cooldown");
      this.fullCombo = fullCombo;
      fullCombo.add(this);
    }

    private final double timeToHit;
    private final double cooldown;
    private final List<Combo> fullCombo;
  }

}
