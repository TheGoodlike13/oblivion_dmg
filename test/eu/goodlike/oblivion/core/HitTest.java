package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static eu.goodlike.oblivion.core.Factor.MAGIC;
import static eu.goodlike.oblivion.core.Source.ARROW;
import static eu.goodlike.oblivion.core.Source.BOW;
import static eu.goodlike.oblivion.core.Source.MELEE;
import static eu.goodlike.oblivion.core.Source.POISON;
import static eu.goodlike.oblivion.core.Source.SPELL;
import static eu.goodlike.oblivion.core.Source.STAFF;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class HitTest {

  @Test
  void emptyHit() {
    assertInvalid();
  }

  @Test
  void validHits() {
    assertHit(MELEE);
    assertHit(MELEE, POISON);

    assertHit(BOW, ARROW);
    assertHit(BOW, ARROW, POISON);

    assertHit(STAFF);

    assertHit(SPELL);
  }

  @Test
  void implicitEquipment() {
    assertImplicit("ARROW", BOW);
    assertImplicit("ARROW", BOW, POISON);

    assertImplicit("BOW", ARROW);
    assertImplicit("BOW", ARROW, POISON);

    assertImplicit("MELEE", POISON);
  }

  @Test
  void incompatibleTypes() {
    assertInvalid(MELEE, SPELL);
  }

  @Test
  void duplicateEquipment() {
    assertInvalid(BOW, ARROW, ARROW);
  }

  @Test
  void delivery() {
    assertThat(hit(MELEE).getDeliveryMechanism()).isEqualTo(MELEE);
    assertThat(hit(BOW).getDeliveryMechanism()).isEqualTo(BOW);
    assertThat(hit(SPELL).getDeliveryMechanism()).isEqualTo(SPELL);
    assertThat(hit(STAFF).getDeliveryMechanism()).isEqualTo(STAFF);

    // implicits
    assertThat(hit(ARROW).getDeliveryMechanism()).isEqualTo(BOW);
    assertThat(hit(POISON).getDeliveryMechanism()).isEqualTo(MELEE);
  }

  @Test
  void weapons() {
    assertThat(hit(MELEE).getWeapon()).contains(MELEE.withNoEffect());
    assertThat(hit(BOW).getWeapon()).contains(BOW.withNoEffect());
    assertThat(hit(SPELL).getWeapon()).isEmpty();
    assertThat(hit(STAFF).getWeapon()).contains(STAFF.withNoEffect());

    // implicits
    assertThat(hit(ARROW).getWeapon()).contains(BOW.withNoEffect());
    assertThat(hit(POISON).getWeapon()).contains(MELEE.withNoEffect());
  }

  @Test
  void alwaysSwapWhenNoWeapon() {
    for (Source source : ImmutableList.of(MELEE, BOW, STAFF)) {
      assertThat(hit(source).requiresSwap(null)).contains(source.withNoEffect());
    }
  }

  @Test
  void alwaysSwapDifferentWeaponOfSameType() {
    for (Source source : ImmutableList.of(MELEE, BOW, STAFF)) {
      assertThat(hit(source).requiresSwap(source.create())).contains(source.withNoEffect());
    }
  }

  @Test
  void alwaysSwapBetweenWeaponTypes() {
    assertThat(hit(MELEE).requiresSwap(MELEE.withNoEffect())).isEmpty();
    assertThat(hit(MELEE).requiresSwap(BOW.withNoEffect())).contains(MELEE.withNoEffect());
    assertThat(hit(MELEE).requiresSwap(STAFF.withNoEffect())).contains(MELEE.withNoEffect());

    assertThat(hit(BOW).requiresSwap(MELEE.withNoEffect())).contains(BOW.withNoEffect());
    assertThat(hit(BOW).requiresSwap(BOW.withNoEffect())).isEmpty();
    assertThat(hit(BOW).requiresSwap(STAFF.withNoEffect())).contains(BOW.withNoEffect());

    assertThat(hit(STAFF).requiresSwap(MELEE.withNoEffect())).contains(STAFF.withNoEffect());
    assertThat(hit(STAFF).requiresSwap(BOW.withNoEffect())).contains(STAFF.withNoEffect());
    assertThat(hit(STAFF).requiresSwap(STAFF.withNoEffect())).isEmpty();
  }

  @Test
  void neverRequireCooldownForFirstHit() {
    for (Source source : ImmutableList.of(MELEE, BOW, STAFF, SPELL)) {
      assertThat(hit(source).requiresCooldownAfter(null)).isFalse();
    }
  }

  @Test
  void alwaysRequireCooldownWhenSwappingWeapons() {
    for (Source source : ImmutableList.of(MELEE, BOW, STAFF)) {
      assertThat(new Hit(source.create()).requiresCooldownAfter(hit(source))).isTrue();
    }

    assertThat(hit(BOW).requiresCooldownAfter(hit(MELEE))).isTrue();
    assertThat(hit(STAFF).requiresCooldownAfter(hit(MELEE))).isTrue();

    assertThat(hit(MELEE).requiresCooldownAfter(hit(BOW))).isTrue();
    assertThat(hit(STAFF).requiresCooldownAfter(hit(BOW))).isTrue();

    assertThat(hit(MELEE).requiresCooldownAfter(hit(STAFF))).isTrue();
    assertThat(hit(BOW).requiresCooldownAfter(hit(STAFF))).isTrue();
  }

  @Test
  void alwaysRequireCooldownWhenCastingASpell() {
    assertThat(hit(SPELL).requiresCooldownAfter(hit(MELEE))).isTrue();
    assertThat(hit(SPELL).requiresCooldownAfter(hit(BOW))).isTrue();
    assertThat(hit(SPELL).requiresCooldownAfter(hit(STAFF))).isTrue();
    assertThat(hit(SPELL).requiresCooldownAfter(hit(SPELL))).isTrue();
  }

  @Test
  void attacksAlwaysIgnoreSpellCooldown() {
    assertThat(hit(MELEE).requiresCooldownAfter(hit(SPELL))).isFalse();
    assertThat(hit(BOW).requiresCooldownAfter(hit(SPELL))).isFalse();
    assertThat(hit(STAFF).requiresCooldownAfter(hit(SPELL))).isFalse();
  }

  @Test
  void physicalWeaponCombosIgnoreCooldown() {
    assertThat(hit(MELEE).requiresCooldownAfter(hit(MELEE))).isFalse();
    assertThat(hit(BOW).requiresCooldownAfter(hit(BOW))).isFalse();

    assertThat(hit(STAFF).requiresCooldownAfter(hit(STAFF))).isTrue();
  }

  @Test
  void isCombo() {
    assertThat(hit(MELEE).isCombo(hit(MELEE))).isTrue();
    assertThat(hit(BOW).isCombo(hit(BOW))).isTrue();
    assertThat(hit(STAFF).isCombo(hit(STAFF))).isTrue();
  }

  @Test
  void isNotCombo() {
    for (Source source : ImmutableList.of(MELEE, BOW, STAFF, SPELL)) {
      assertThat(hit(source).isCombo(null)).isFalse();
      assertThat(hit(source).isCombo(new Hit(source.create()))).isFalse();
      assertThat(hit(source).isCombo(hit(SPELL))).isFalse();
      assertThat(hit(SPELL).isCombo(hit(source))).isFalse();
    }
  }

  @Test
  void performString() {
    assertThat(hit(MELEE).toPerformString()).isEqualTo("swing <MELEE>");
    assertThat(hit(BOW).toPerformString()).isEqualTo("aim <ARROW> + <BOW>");
    assertThat(hit(SPELL).toPerformString()).isEqualTo("cast <SPELL>");
    assertThat(hit(STAFF).toPerformString()).isEqualTo("invoke <STAFF>");

    // implicit
    assertThat(hit(ARROW).toPerformString()).isEqualTo("aim <ARROW> + <BOW>");
    assertThat(hit(POISON).toPerformString()).isEqualTo("swing <MELEE> + <POISON>");
  }

  @Test
  void effectCount() {
    Carrier melee = MELEE.create(MAGIC.damage(10), MAGIC.weakness(100));
    Carrier poison = POISON.create(MAGIC.damage(20));
    Hit hit = new Hit(melee, poison);

    assertThat(hit.count(MAGIC.drain())).isEqualTo(0);
    assertThat(hit.count(MAGIC.resist())).isEqualTo(1);
    assertThat(hit.count(MAGIC.damage())).isEqualTo(2);
    assertThat(hit.count(POISON.resist())).isEqualTo(0);
  }

  private void assertHit(Source... sources) {
    assertThatNoException().isThrownBy(() -> new Hit(dummyCarriers(sources)));
  }

  private void assertImplicit(String implicit, Source... sources) {
    Hit hit = new Hit(dummyCarriers(sources));
    assertThat(hit).anyMatch(carrier -> carrier.getSource().toString().equals(implicit));
  }

  private void assertInvalid(Source... sources) {
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> new Hit(dummyCarriers(sources)));
  }

  private Hit hit(Source... sources) {
    return new Hit(dummyCarriers(sources));
  }

  private List<Carrier> dummyCarriers(Source... sources) {
    return Stream.of(sources).map(Source::withNoEffect).collect(toList());
  }

}
