package eu.goodlike.oblivion.core;

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

  private List<Carrier> dummyCarriers(Source... sources) {
    return Stream.of(sources).map(Source::withNoEffect).collect(toList());
  }

}
