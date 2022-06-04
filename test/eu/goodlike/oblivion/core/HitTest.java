package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static eu.goodlike.oblivion.core.Effector.Factory.ARROW;
import static eu.goodlike.oblivion.core.Effector.Factory.BOW;
import static eu.goodlike.oblivion.core.Effector.Factory.MELEE;
import static eu.goodlike.oblivion.core.Effector.Factory.POISON;
import static eu.goodlike.oblivion.core.Effector.Factory.POWER;
import static eu.goodlike.oblivion.core.Effector.Factory.SPELL;
import static eu.goodlike.oblivion.core.Effector.Factory.STAFF;
import static eu.goodlike.oblivion.core.Factor.MAGIC;
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
    assertHit(POWER);
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
  void weapons() {
    assertThat(hit(MELEE).getWeapon()).contains(MELEE.withNoEffect());
    assertThat(hit(BOW).getWeapon()).contains(BOW.withNoEffect());
    assertThat(hit(SPELL).getWeapon()).isEmpty();
    assertThat(hit(POWER).getWeapon()).isEmpty();
    assertThat(hit(STAFF).getWeapon()).contains(STAFF.withNoEffect());

    // implicits
    assertThat(hit(ARROW).getWeapon()).contains(BOW.withNoEffect());
    assertThat(hit(POISON).getWeapon()).contains(MELEE.withNoEffect());
  }

  @Test
  void alwaysSwapWhenNoWeapon() {
    for (Category<? extends Armament> category : ImmutableList.of(MELEE, BOW, STAFF)) {
      assertThat(hit(category).requiresSwap(null)).contains(category.withNoEffect());
    }
  }

  @Test
  void alwaysSwapDifferentWeaponOfSameType() {
    for (Category<? extends Armament> category : ImmutableList.of(MELEE, BOW, STAFF)) {
      assertThat(hit(category).requiresSwap(category.create())).contains(category.withNoEffect());
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
    for (Category<? extends Armament> category : ImmutableList.of(MELEE, BOW, STAFF, SPELL, POWER)) {
      assertThat(hit(category).requiresCooldownAfter(null)).isFalse();
    }
  }

  @Test
  void alwaysRequireCooldownWhenSwappingWeapons() {
    for (Category<? extends Armament> category : ImmutableList.of(MELEE, BOW, STAFF)) {
      assertThat(new Hit(category.create()).requiresCooldownAfter(hit(category))).isTrue();
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
    assertThat(hit(SPELL).requiresCooldownAfter(hit(POWER))).isTrue();

    assertThat(hit(POWER).requiresCooldownAfter(hit(MELEE))).isTrue();
    assertThat(hit(POWER).requiresCooldownAfter(hit(BOW))).isTrue();
    assertThat(hit(POWER).requiresCooldownAfter(hit(STAFF))).isTrue();
    assertThat(hit(POWER).requiresCooldownAfter(hit(SPELL))).isTrue();
    assertThat(hit(POWER).requiresCooldownAfter(hit(POWER))).isTrue();
  }

  @Test
  void attacksAlwaysIgnoreSpellCooldown() {
    assertThat(hit(MELEE).requiresCooldownAfter(hit(SPELL))).isFalse();
    assertThat(hit(BOW).requiresCooldownAfter(hit(SPELL))).isFalse();
    assertThat(hit(STAFF).requiresCooldownAfter(hit(SPELL))).isFalse();

    assertThat(hit(MELEE).requiresCooldownAfter(hit(POWER))).isFalse();
    assertThat(hit(BOW).requiresCooldownAfter(hit(POWER))).isFalse();
    assertThat(hit(STAFF).requiresCooldownAfter(hit(POWER))).isFalse();
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
    for (Category<? extends Armament> category : ImmutableList.of(MELEE, BOW, STAFF, SPELL, POWER)) {
      assertThat(hit(category).isCombo(null)).isFalse();
      assertThat(hit(category).isCombo(new Hit(category.create()))).isFalse();
      assertThat(hit(category).isCombo(hit(SPELL))).isFalse();
      assertThat(hit(SPELL).isCombo(hit(category))).isFalse();
      assertThat(hit(category).isCombo(hit(POWER))).isFalse();
      assertThat(hit(POWER).isCombo(hit(category))).isFalse();
    }
  }

  @Test
  void performString() {
    assertThat(hit(MELEE).toPerformString()).isEqualTo("swing <MELEE>");
    assertThat(hit(BOW).toPerformString()).isEqualTo("aim <ARROW> + <BOW>");
    assertThat(hit(SPELL).toPerformString()).isEqualTo("cast <SPELL>");
    assertThat(hit(POWER).toPerformString()).isEqualTo("cast <POWER>");
    assertThat(hit(STAFF).toPerformString()).isEqualTo("invoke <STAFF>");

    // implicit
    assertThat(hit(ARROW).toPerformString()).isEqualTo("aim <ARROW> + <BOW>");
    assertThat(hit(POISON).toPerformString()).isEqualTo("swing <MELEE> + <POISON>");
  }

  @Test
  void effectCount() {
    Effector melee = MELEE.create(MAGIC.damage(10), MAGIC.weakness(100));
    Effector poison = POISON.create(MAGIC.damage(20));
    Hit hit = new Hit(melee, poison);

    assertThat(hit.count(MAGIC.drain())).isEqualTo(0);
    assertThat(hit.count(MAGIC.resist())).isEqualTo(1);
    assertThat(hit.count(MAGIC.damage())).isEqualTo(2);
    assertThat(hit.count(POISON.resist())).isEqualTo(0);
  }

  private void assertHit(Category<?>... categories) {
    assertThatNoException().isThrownBy(() -> new Hit(dummies(categories)));
  }

  private void assertImplicit(String implicit, Category<?>... categories) {
    Hit hit = new Hit(dummies(categories));
    assertThat(hit).anyMatch(effector -> effector.getCategory().toString().equals(implicit));
  }

  private void assertInvalid(Category<?>... categories) {
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> new Hit(dummies(categories)));
  }

  private Hit hit(Category<?>... categories) {
    return new Hit(dummies(categories));
  }

  private List<Effector> dummies(Category<?>... categories) {
    return Stream.of(categories).map(Category::withNoEffect).collect(toList());
  }

}
