package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static eu.goodlike.oblivion.core.Effector.Factory.ARROW;
import static eu.goodlike.oblivion.core.Effector.Factory.BOW;
import static eu.goodlike.oblivion.core.Effector.Factory.MELEE;
import static eu.goodlike.oblivion.core.Effector.Factory.POISON;
import static java.util.stream.Collectors.joining;

/**
 * Combination of an armament and optional accessories which all apply their effects at once.
 * Effects that are applied at once cannot influence subsequent effects in the same hit.
 * For example: enchanted bow which fires an enchanted arrow with poison.
 * The order in which these effects are applied is consistent with the natural order of the effectors.
 */
public final class Hit implements Iterable<Effector>, HitPattern {

  /**
   * @return armament of this hit if it's a weapon, empty otherwise
   */
  public Optional<Armament> getWeapon() {
    return armament.isEquipment()
      ? Optional.of(armament)
      : Optional.empty();
  }

  /**
   * Compares the armament of this hit with the given weapon.
   * If this hit uses a different weapon, returns it.
   * Otherwise returns empty.
   */
  public Optional<Armament> requiresSwap(Armament oldWeapon) {
    return getWeapon().filter(newWeapon -> !newWeapon.equals(oldWeapon));
  }

  /**
   * The rules for cooldown cancelling are:
   * <p/>1) Never cancel cooldown if you cast a spell.
   * <p/>2) Never cancel cooldown if you need to swap.
   * <p/>3) Never cancel cooldown if you continue to use a rigid weapon (e.g. staff).
   * <p/>4) In all other cases, cancel cooldown.
   *
   * @return true if this hit cannot cancel the cooldown of the last hit
   */
  public boolean requiresCooldownAfter(Hit last) {
    return last != null && (isSpell() || requiresSwap(last) || last.usesRigidWeapon());
  }

  /**
   * @return true if last hit used the exact same weapon, false otherwise
   */
  public boolean isCombo(Hit last) {
    return last != null
      && getWeapon().isPresent()
      && getWeapon().equals(last.getWeapon());
  }

  /**
   * @return amount of given type of effects in this hit; 2 or more is only possible if this hit has accessories
   */
  public long count(Effect.Type type) {
    return Streams.stream(this)
      .flatMap(Streams::stream)
      .map(EffectText::getType)
      .filter(type::equals)
      .count();
  }

  public boolean hasMultiple(Effect.Type type) {
    return count(type) > 1;
  }

  @Override
  public Iterator<Effector> iterator() {
    return effectors.iterator();
  }

  @Override
  public double timeToHit(int combo) {
    return armament.timeToHit(combo);
  }

  @Override
  public double cooldown(int combo) {
    return armament.cooldown(combo);
  }

  public Hit(Effector... effectors) {
    this(Arrays.asList(effectors));
  }

  /**
   * Creates a new hit from given effectors.
   * They are sorted according to their natural ordering.
   * <p/>
   * In some cases, the effectors will be supplemented with implicit equipment with no enchants.
   * For example: if there is an arrow, a bow will be added if missing.
   * This removes the need to specify them explicitly.
   * Poison prefers a melee weapon if it cannot be decided otherwise.
   *
   * @throws StructureException if the hit resulting from given effectors is completely invalid
   */
  public Hit(List<Effector> effectors) {
    this.effectors = ensureOrderAndEquipment(effectors);

    StructureException.throwOnInvalidHit(hitTrace());

    this.armament = this.effectors.stream()
      .filter(Armament.class::isInstance)
      .map(Armament.class::cast)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("Should be at least 1 armament"));
  }

  private final Armament armament;
  private final List<Effector> effectors;

  private List<Effector> ensureOrderAndEquipment(List<Effector> effectors) {
    List<Effector> mutableCopy = new ArrayList<>(effectors);

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

  private void ensure(Category equipment, List<Effector> effectors) {
    if (!equipment.any(effectors)) {
      effectors.add(equipment.withNoEffect());
    }
  }

  private boolean isSpell() {
    return !getWeapon().isPresent();
  }

  private boolean requiresSwap(Hit last) {
    return getWeapon().flatMap(last::requiresSwap).isPresent();
  }

  private boolean usesRigidWeapon() {
    return getWeapon().filter(Armament::isRigid).isPresent();
  }

  private String hitTrace() {
    return effectors.stream()
      .map(Effector::getCategory)
      .map(Category::toString)
      .collect(joining(" + "));
  }

  @Override
  public String toString() {
    return effectors.stream()
      .map(Effector::toString)
      .collect(joining(" + "));
  }

  public String toPerformString() {
    return armament.describeAction() + " " + toLabelString();
  }

  public String toLabelString() {
    return effectors.stream()
      .map(Effector::getName)
      .collect(joining(" + "));
  }

  /**
   * Describes timing for attacks of some armament.
   * <p/>
   * Single combo always return the same values.
   * Combos of 2 alternate, etc.
   */
  public static final class Combo implements HitPattern, HitPattern.Builder {
    @Override
    public double timeToHit(int combo) {
      int index = combo % fullCombo.size(); // TODO: might as well refactor duplicate code
      Combo actual = fullCombo.get(index);
      return actual.timeToHit;
    }

    @Override
    public double cooldown(int combo) {
      int index = combo % fullCombo.size();
      Combo actual = fullCombo.get(index);
      return actual.cooldown;
    }

    @Override
    public Combo combo(double nextTimeToHit, double nextCooldown) {
      return new Combo(nextTimeToHit, nextCooldown, fullCombo);
    }

    @Override
    public Combo build() {
      return this;
    }

    public static HitPattern.Builder builder() {
      return new Builder();
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

    private static final class Builder implements HitPattern.Builder {
      @Override
      public HitPattern.Builder combo(double nextTimeToHit, double nextCooldown) {
        return combo == null
          ? combo = new Combo(nextTimeToHit, nextCooldown)
          : combo.combo(nextTimeToHit, nextCooldown);
      }

      @Override
      public HitPattern build() {
        if (combo == null) {
          throw new StructureException("Combo must have size of at least one!");
        }
        return combo;
      }

      private Combo combo;
    }
  }

}
