package eu.goodlike.oblivion.core;

import org.junit.jupiter.api.Test;

import static eu.goodlike.oblivion.core.Factor.FIRE;
import static eu.goodlike.oblivion.core.Factor.FROST;
import static eu.goodlike.oblivion.core.Factor.MAGIC;
import static eu.goodlike.oblivion.core.Factor.POISON;
import static eu.goodlike.oblivion.core.Factor.SHOCK;
import static eu.goodlike.oblivion.core.Source.SPELL;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class EffectorTest {

  @Test
  void noMoreThanOneInstanceOfAnEffect() {
    assertThatExceptionOfType(StructureException.class).isThrownBy(() ->
      SPELL.create(MAGIC.weakness(100), MAGIC.weakness(100)));
  }

  @Test
  void resistAndWeaknessAreTreatedAsSameType_forSimplicity() {
    assertThatExceptionOfType(StructureException.class).isThrownBy(() ->
      SPELL.create(MAGIC.weakness(100), MAGIC.resist(100)));
  }

  @Test
  void maxEffects() {
    assertThatNoException().isThrownBy(() -> SPELL.create(
      POISON.weakness(100),
      MAGIC.weakness(100),
      FIRE.weakness(100),
      FROST.weakness(100),
      SHOCK.weakness(100),
      MAGIC.damage(100),
      FIRE.damage(100),
      FROST.damage(100),
      SHOCK.damage(100),
      MAGIC.drain(100)
    ));
  }

}
