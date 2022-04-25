package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.StructureException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ParseTest {

  @Test
  void factors() {
    assertThat(Parse.factor("poison")).isEqualTo(Factor.POISON);
    assertThat(Parse.factor("magic")).isEqualTo(Factor.MAGIC);
    assertThat(Parse.factor("fire")).isEqualTo(Factor.FIRE);
    assertThat(Parse.factor("frost")).isEqualTo(Factor.FROST);
    assertThat(Parse.factor("shock")).isEqualTo(Factor.SHOCK);

    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.factor("x"));
  }

  @Test
  void elements() {
    assertThat(Parse.element("magic")).isEqualTo(Factor.MAGIC);
    assertThat(Parse.element("fire")).isEqualTo(Factor.FIRE);
    assertThat(Parse.element("frost")).isEqualTo(Factor.FROST);
    assertThat(Parse.element("shock")).isEqualTo(Factor.SHOCK);

    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.element("blood"));
  }

  @Test
  void sources() {
    assertThat(Parse.source("melee")).isEqualTo(Source.MELEE);
    assertThat(Parse.source("bow")).isEqualTo(Source.BOW);
    assertThat(Parse.source("arrow")).isEqualTo(Source.ARROW);
    assertThat(Parse.source("poison")).isEqualTo(Source.POISON);
    assertThat(Parse.source("spell")).isEqualTo(Source.SPELL);
    assertThat(Parse.source("staff")).isEqualTo(Source.STAFF);

    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.source("Main.java"));
  }

  @Test
  void casesAreOk() {
    assertThat(Parse.factor("pOiSoN")).isEqualTo(Factor.POISON);
    assertThat(Parse.factor("POISON")).isEqualTo(Factor.POISON);
  }

  @Test
  void prefixesAreOk() {
    assertThat(Parse.factor("p")).isEqualTo(Factor.POISON);
    assertThat(Parse.factor("po")).isEqualTo(Factor.POISON);
    assertThat(Parse.factor("poi")).isEqualTo(Factor.POISON);
    assertThat(Parse.factor("pois")).isEqualTo(Factor.POISON);
    assertThat(Parse.factor("poiso")).isEqualTo(Factor.POISON);
  }

  @Test
  void prefixPrecedence() {
    assertThat(Parse.factor("f")).isEqualTo(Factor.FIRE);
    assertThat(Parse.factor("fr")).isEqualTo(Factor.FROST);

    assertThat(Parse.source("s")).isEqualTo(Source.SPELL);
    assertThat(Parse.source("st")).isEqualTo(Source.STAFF);
  }

  @Test
  void weakness() {
    assertThat(Parse.effect("100wfr")).isEqualTo(Factor.FROST.weakness(100));
    assertThat(Parse.effect("25weaknessshock10s")).isEqualTo(Factor.SHOCK.weakness(25).forSecs(10));
  }

  @Test
  void resist() {
    assertThat(Parse.effect("50rp")).isEqualTo(Factor.POISON.resist(50));
    assertThat(Parse.effect("99resistm90")).isEqualTo(Factor.MAGIC.resist(99).forSecs(90));
  }

  @Test
  void drain() {
    assertThat(Parse.effect("30d")).isEqualTo(Factor.MAGIC.drain(30));
    assertThat(Parse.effect("55d9s")).isEqualTo(Factor.MAGIC.drain(55).forSecs(9));
  }

  @Test
  void damage() {
    assertThat(Parse.effect("6s")).isEqualTo(Factor.SHOCK.damage(6));
    assertThat(Parse.effect("13fr37")).isEqualTo(Factor.FROST.damage(13).forSecs(37));
  }

  @Test
  void badEffects() {
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect(""));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("wf"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("-100wf"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100ww"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100wf-10"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100wf10z"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100dd"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100ms"));
  }

}
