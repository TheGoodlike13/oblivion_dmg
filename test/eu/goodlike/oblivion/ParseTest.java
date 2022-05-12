package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.StructureException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ObjectAssert;
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
    assertCategory(Parse.category("melee")).isEqualTo(Effector.Factory.MELEE);
    assertCategory(Parse.category("bow")).isEqualTo(Effector.Factory.BOW);
    assertCategory(Parse.category("arrow")).isEqualTo(Effector.Factory.ARROW);
    assertCategory(Parse.category("poison")).isEqualTo(Effector.Factory.POISON);
    assertCategory(Parse.category("spell")).isEqualTo(Effector.Factory.SPELL);
    assertCategory(Parse.category("staff")).isEqualTo(Effector.Factory.STAFF);

    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.category("Main.java"));
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

    assertCategory(Parse.category("s")).isEqualTo(Effector.Factory.SPELL);
    assertCategory(Parse.category("st")).isEqualTo(Effector.Factory.STAFF);
  }

  @Test
  void weakness() {
    assertThat(Parse.effect("100wfr")).isEqualTo(Factor.FROST.weakness(100).forSecs(1));
    assertThat(Parse.effect("25weaknessshock10s")).isEqualTo(Factor.SHOCK.weakness(25).forSecs(10));
  }

  @Test
  void resist() {
    assertThat(Parse.effect("50rp")).isEqualTo(Factor.POISON.resist(50).forSecs(1));
    assertThat(Parse.effect("99resistm90")).isEqualTo(Factor.MAGIC.resist(99).forSecs(90));
  }

  @Test
  void drain() {
    assertThat(Parse.effect("30d")).isEqualTo(Factor.MAGIC.drain(30).forSecs(1));
    assertThat(Parse.effect("55d9s")).isEqualTo(Factor.MAGIC.drain(55).forSecs(9));
  }

  @Test
  void damage() {
    assertThat(Parse.effect("6s")).isEqualTo(Factor.SHOCK.damage(6).forSecs(1));
    assertThat(Parse.effect("13fr37")).isEqualTo(Factor.FROST.damage(13).forSecs(37));
  }

  @Test
  void instant() {
    assertThat(Parse.effect("6s0s")).isEqualTo(Factor.SHOCK.damage(6).instant());
    assertThat(Parse.effect("6ws0s")).isEqualTo(Factor.SHOCK.weakness(6).instant());
    assertThat(Parse.effect("6d0s")).isEqualTo(Factor.MAGIC.drain(6).instant());
  }

  @Test
  void badEffects() {
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect(""));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("wf"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100r"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100e"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("-100wf"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100ww"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100wf-10"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100wf10z"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100dd"));
    assertThatExceptionOfType(StructureException.class).isThrownBy(() -> Parse.effect("100ms"));
  }

  // required to avoid compilation issue due to Category now being a Predicate
  // see http://joel-costigliola.github.io/assertj/assertj-core.html#ambiguous-compilation-error
  private ObjectAssert<? extends Category<?>> assertCategory(Category<?> category) {
    return AssertionsForClassTypes.assertThat(category);
  }

}
