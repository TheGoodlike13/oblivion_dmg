package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

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
  void weapons() {
    assertThat(hit(MELEE).getWeapon()).contains(MELEE.withNoEffect());
    assertThat(hit(BOW).getWeapon()).contains(BOW.withNoEffect());
    assertThat(hit(ARROW).getWeapon()).contains(BOW.withNoEffect());     // implicit
    assertThat(hit(POISON).getWeapon()).contains(MELEE.withNoEffect());  // implicit
    assertThat(hit(SPELL).getWeapon()).isEmpty();
    assertThat(hit(STAFF).getWeapon()).contains(STAFF.withNoEffect());
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
  void alwaysRequireCooldownWhenSwappingWeapons() {
    for (Source source : ImmutableList.of(MELEE, BOW, STAFF)) {
      assertThat(hit(source).requiresCooldown(new Hit(source.create()))).isTrue();
    }

    assertThat(hit(MELEE).requiresCooldown(hit(BOW))).isTrue();
    assertThat(hit(MELEE).requiresCooldown(hit(STAFF))).isTrue();

    assertThat(hit(BOW).requiresCooldown(hit(MELEE))).isTrue();
    assertThat(hit(BOW).requiresCooldown(hit(STAFF))).isTrue();

    assertThat(hit(STAFF).requiresCooldown(hit(MELEE))).isTrue();
    assertThat(hit(STAFF).requiresCooldown(hit(BOW))).isTrue();
  }

  @Test
  void alwaysRequireCooldownWhenCastingASpell() {
    assertThat(hit(MELEE).requiresCooldown(hit(SPELL))).isTrue();
    assertThat(hit(BOW).requiresCooldown(hit(SPELL))).isTrue();
    assertThat(hit(STAFF).requiresCooldown(hit(SPELL))).isTrue();
    assertThat(hit(SPELL).requiresCooldown(hit(SPELL))).isTrue();
  }

  @Test
  void attacksAlwaysIgnoreSpellCooldown() {
    assertThat(hit(SPELL).requiresCooldown(hit(MELEE))).isFalse();
    assertThat(hit(SPELL).requiresCooldown(hit(BOW))).isFalse();
    assertThat(hit(SPELL).requiresCooldown(hit(STAFF))).isFalse();
  }

  @Test
  void physicalWeaponCombosIgnoreCooldown() {
    assertThat(hit(MELEE).requiresCooldown(hit(MELEE))).isFalse();
    assertThat(hit(BOW).requiresCooldown(hit(BOW))).isFalse();

    assertThat(hit(STAFF).requiresCooldown(hit(STAFF))).isTrue();
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
