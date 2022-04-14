package eu.goodlike.oblivion.calc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static eu.goodlike.oblivion.calc.Element.FIRE;
import static eu.goodlike.oblivion.calc.Element.MAGIC;
import static org.assertj.core.api.Assertions.assertThat;

class TargetTest {

  private Target target;

  @BeforeEach
  void setup() {
    target = new Target(1000);
  }

  @Test
  void initialHealth() {
    assertDamageTaken("0.00");
    assertHealthRemaining("1000.00");
  }

  @Test
  void damage() {
    target.hit(10, MAGIC);

    assertDamageTaken("10.00");
    assertHealthRemaining("990.00");
  }

  @Test
  void death() {
    target.hit(10000, MAGIC);

    assertDamageTaken("10000.00");
    assertHealthRemaining("0.00");
  }

  @Test
  void damageResisted() {
    target.addModifier(MAGIC.resist(50));

    target.hit(10, MAGIC);
    target.hit(10, FIRE);

    assertDamageTaken("15.00");
  }

  @Test
  void damageAmplified() {
    target.addModifier(MAGIC.weakness(100));

    target.hit(10, MAGIC);
    target.hit(10, FIRE);

    assertDamageTaken("30.00");
  }

  @Test
  void immunity() {
    target.addModifier(MAGIC.resist(100));

    target.hit(10, MAGIC);

    assertDamageTaken("0.00");
  }

  @Test
  void damageAmplifiedTwice() {
    target.addModifier(MAGIC.weakness(100));
    target.addModifier(FIRE.weakness(100));

    target.hit(10, MAGIC, FIRE);

    assertDamageTaken("40.00");
  }

  @Test
  void spellDamage() {
    target.addModifier(MAGIC.weakness(100));

    target.spellHit(1, MAGIC.damage(10));

    assertDamageTaken("20.00");
  }

  @Test
  void elementalSpellDamage() {
    target.addModifier(MAGIC.weakness(100));
    target.addModifier(FIRE.weakness(100));

    target.spellHit(1, FIRE.damage(10));

    assertDamageTaken("40.00");
  }

  @Test
  void magicAmplification() {
    target.addModifier(MAGIC.weakness(100));

    target.spellHit(1, FIRE.weakness(100));
    target.spellHit(2, FIRE.damage(10));

    assertDamageTaken("60.00");
  }

  @Test
  void magicSelfAmplification() {
    target.addModifier(MAGIC.weakness(100));

    target.spellHit(1, MAGIC.weakness(100));
    target.spellHit(2, MAGIC.damage(10));

    assertDamageTaken("40.00");
  }

  @Test
  void sameSpellModifierAffectsItself() {
    target.spellHit(1, MAGIC.weakness(100));  // +100 (replaced)
    target.spellHit(1, MAGIC.weakness(100));  // +200
    target.spellHit(2, MAGIC.damage(10));

    assertDamageTaken("30.00");
  }

  @Test
  void sameSpellDamageAddsUp() {
    target.spellHit(1, MAGIC.damage(10));
    target.spellHit(1, MAGIC.damage(10));

    assertDamageTaken("20.00");
  }

  @Test
  void allSpellEffectsAreAppliedAtOnce() {
    target.spellHit(1, MAGIC.weakness(100), FIRE.weakness(100), FIRE.damage(10));

    assertDamageTaken("10.00");

    target.spellHit(2, FIRE.damage(10));

    assertDamageTaken("50.00");  // +40
  }

  @Test
  void wastedMagicWeaknessEffectDueToInefficientOrder() {
    target.spellHit(1, MAGIC.weakness(100), FIRE.weakness(100));  // +100, +100 (replaced)
    target.spellHit(1, MAGIC.weakness(100), FIRE.weakness(100));  // +200, +100

    target.spellHit(2, FIRE.damage(10));

    assertDamageTaken("60.00");
  }

  @Test
  void efficientOrder() {
    target.spellHit(1, FIRE.weakness(100), MAGIC.weakness(100));  // +100, +100 (replaced)
    target.spellHit(1, FIRE.weakness(100), MAGIC.weakness(100));  // +200, +200

    target.spellHit(2, FIRE.damage(10));

    assertDamageTaken("90.00");
  }

  @Test
  void spellStacking_twice() {
    target.spellHit(1, FIRE.weakness(100), MAGIC.weakness(100));  // +100, +100
    target.spellHit(2, FIRE.weakness(100), MAGIC.weakness(100));  // +200, +200

    target.spellHit(3, FIRE.damage(10));

    assertDamageTaken("160.00");
  }

  @Test
  void spellStacking_thrice() {
    target.spellHit(1, FIRE.weakness(100), MAGIC.weakness(100));  // +100, +100 (replaced)
    target.spellHit(2, FIRE.weakness(100), MAGIC.weakness(100));  // +200, +200
    target.spellHit(1, FIRE.weakness(100), MAGIC.weakness(100));  // +400, +400

    target.spellHit(3, FIRE.damage(10));

    assertDamageTaken("490.00");
  }

  @Test
  void spellStacking_fourTimes() {
    target.spellHit(1, FIRE.weakness(100), MAGIC.weakness(100));  // +100, +100 (replaced)
    target.spellHit(2, FIRE.weakness(100), MAGIC.weakness(100));  // +200, +200 (replaced)
    target.spellHit(1, FIRE.weakness(100), MAGIC.weakness(100));  // +400, +400
    target.spellHit(2, FIRE.weakness(100), MAGIC.weakness(100));  // +700, +700

    target.spellHit(3, FIRE.damage(10));

    assertDamageTaken("1440.00");
  }

  @Test
  void spellStacking_fourTimes_inefficient() {
    target.spellHit(1, MAGIC.weakness(100), FIRE.weakness(100));  // +100, +100 (replaced)
    target.spellHit(2, MAGIC.weakness(100), FIRE.weakness(100));  // +200, +200 (replaced)
    target.spellHit(1, MAGIC.weakness(100), FIRE.weakness(100));  // +400, +300
    target.spellHit(2, MAGIC.weakness(100), FIRE.weakness(100));  // +700, +500

    target.spellHit(3, FIRE.damage(10));

    assertDamageTaken("1080.00");
  }

  private void assertDamageTaken(String expected) {
    assertThat(target.damageTaken()).isEqualTo(expected);
  }

  private void assertHealthRemaining(String expected) {
    assertThat(target.healthRemaining()).isEqualTo(expected);
  }

}
