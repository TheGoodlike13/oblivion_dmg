package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.global.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static eu.goodlike.oblivion.core.Factor.FIRE;
import static eu.goodlike.oblivion.core.Factor.FROST;
import static eu.goodlike.oblivion.core.Factor.MAGIC;
import static eu.goodlike.oblivion.core.Factor.POISON;
import static eu.goodlike.oblivion.core.Factor.SHOCK;
import static eu.goodlike.oblivion.core.Source.MELEE;
import static eu.goodlike.oblivion.core.Source.SPELL;
import static eu.goodlike.oblivion.global.Settings.DIFFICULTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class EnemyTest {

  private Enemy target;

  @BeforeEach
  void setup() {
    resurrect("Standard enemy");
  }

  @AfterEach
  void tearDown() {
    Settings.resetToFactory();
  }

  @Test
  void initialHealth() {
    assertDamageTaken(0);
    assertHealthRemaining(1000);
    assertThat(target.isAlive()).isTrue();
  }

  @Test
  void damage() {
    target.hit(MAGIC.damage(10));

    assertDamageTaken(10);
    assertHealthRemaining(990);
  }

  @Test
  void difficultyMultiplier() {
    DIFFICULTY = 100;

    target.hit(MAGIC.damage(60));

    assertDamageTaken(10);
    assertHealthRemaining(990);
  }

  @Test
  void death() {
    target.hit(MAGIC.damage(10000));

    assertDamageTaken(10000);
    assertHealthRemaining(0);
    assertThat(target.isAlive()).isFalse();
  }

  @Test
  void damageResisted() {
    target.hit(FIRE.resist(50));

    target.hit(MAGIC.damage(10));
    target.hit(FIRE.damage(10));

    assertDamageTaken(15);
  }

  @Test
  void damageAmplified() {
    target.hit(FIRE.weakness(100));

    target.hit(MAGIC.damage(10));
    target.hit(FIRE.damage(10));

    assertDamageTaken(30);
  }

  @Test
  void immunity() {
    target.hit(MAGIC.resist(100));

    target.hit(MAGIC.damage(10));

    assertDamageTaken(0);
  }

  @Test
  void damageAmplifiedTwice() {
    target.hit(MAGIC.weakness(100), FIRE.weakness(100));

    target.hit(SPELL, FIRE.damage(10));

    assertDamageTaken(40);
  }

  @Test
  void poisonDamage() {
    target.hit(POISON.weakness(100));

    target.hit(POISON, MAGIC.damage(10));

    assertDamageTaken(20);
  }

  @Test
  void elementalPoisonDamage() {
    target.hit(POISON.weakness(100), FIRE.weakness(100));

    target.hit(POISON, FIRE.damage(10));

    assertDamageTaken(40);
  }

  @Test
  void elementalPoisonDamageIsNotMagic() {
    target.hit(MAGIC.weakness(100));

    target.hit(POISON, FIRE.damage(10));

    assertDamageTaken(10);
  }

  @Test
  void magicAmplification() {
    target.hit(MAGIC.weakness(100));
    target.hit(FIRE.weakness(100));

    target.hit(FIRE.damage(10));

    assertDamageTaken(60);
  }

  @Test
  void magicSelfAmplification() {
    target.hit(MAGIC.weakness(100));
    target.hit(MAGIC.weakness(100));

    target.hit(MAGIC.damage(10));

    assertDamageTaken(40);
  }

  @Test
  void sameSpellModifierAffectsItself() {
    target.hit("#1", MAGIC.weakness(100));  // +100 (replaced)
    target.hit("#1", MAGIC.weakness(100));  // +200

    target.hit(MAGIC.damage(10));

    assertDamageTaken(30);
  }

  @Test
  void sameSpellDamageCompleteOverlap() {
    target.hit("#1", MAGIC.damage(10));
    target.hit("#1", MAGIC.damage(10));

    assertDamageTaken(10);
  }

  @Test
  void sameSpellDamagePartialOverlap() {
    target.hit("#1", MAGIC.damage(10));
    target.tick(0.5);
    target.hit("#1", MAGIC.damage(10));

    assertDamageTaken(15);
  }

  @Test
  void allSpellEffectsAreAppliedAtOnce() {
    target.hit(MAGIC.weakness(100), FIRE.weakness(100), FIRE.damage(10));

    assertDamageTaken(10);
  }

  @Test
  void wastedMagicWeaknessEffectDueToInefficientOrder() {
    target.hit("#1", MAGIC.weakness(100), FIRE.weakness(100));  // +100, +100 (replaced)
    target.hit("#1", MAGIC.weakness(100), FIRE.weakness(100));  // +200, +100

    target.hit(FIRE.damage(10));

    assertDamageTaken(60);
  }

  @Test
  void efficientOrder() {
    target.hit("#1", FIRE.weakness(100), MAGIC.weakness(100));  // +100, +100 (replaced)
    target.hit("#1", FIRE.weakness(100), MAGIC.weakness(100));  // +200, +200

    target.hit(FIRE.damage(10));

    assertDamageTaken(90);
  }

  @Test
  void spellStacking_twice() {
    target.hit("#1", FIRE.weakness(100), MAGIC.weakness(100));  // +100, +100
    target.hit("#2", FIRE.weakness(100), MAGIC.weakness(100));  // +200, +200

    target.hit(FIRE.damage(10));

    assertDamageTaken(160);
  }

  @Test
  void spellStacking_thrice() {
    target.hit("#1", FIRE.weakness(100), MAGIC.weakness(100));  // +100, +100 (replaced)
    target.hit("#2", FIRE.weakness(100), MAGIC.weakness(100));  // +200, +200
    target.hit("#1", FIRE.weakness(100), MAGIC.weakness(100));  // +400, +400

    target.hit(FIRE.damage(10));

    assertDamageTaken(490);
  }

  @Test
  void spellStacking_fourTimes() {
    target.hit("#1", FIRE.weakness(100), MAGIC.weakness(100));  // +100, +100 (replaced)
    target.hit("#2", FIRE.weakness(100), MAGIC.weakness(100));  // +200, +200 (replaced)
    target.hit("#1", FIRE.weakness(100), MAGIC.weakness(100));  // +400, +400
    target.hit("#2", FIRE.weakness(100), MAGIC.weakness(100));  // +700, +700

    target.hit(FIRE.damage(10));

    assertDamageTaken(1440);
  }

  @Test
  void spellStacking_fourTimes_inefficient() {
    target.hit("#1", MAGIC.weakness(100), FIRE.weakness(100));  // +100, +100 (replaced)
    target.hit("#2", MAGIC.weakness(100), FIRE.weakness(100));  // +200, +200 (replaced)
    target.hit("#1", MAGIC.weakness(100), FIRE.weakness(100));  // +400, +300
    target.hit("#2", MAGIC.weakness(100), FIRE.weakness(100));  // +700, +500

    target.hit(FIRE.damage(10));

    assertDamageTaken(1080);
  }

  @Test
  void drain() {
    target.hit(MAGIC.drain(100));

    assertThat(target.damageTaken()).isEqualTo(100);

    target.resolve();

    assertThat(target.damageTaken()).isEqualTo(0);
  }

  @Test
  void drainWeakness() {
    target.hit(MAGIC.weakness(100));

    target.hit(MAGIC.drain(100));

    assertThat(target.damageTaken()).isEqualTo(200);
  }

  @Test
  void poisonDrainWeakness() {
    target.hit(POISON.weakness(100), MAGIC.weakness(100));

    target.hit(POISON, MAGIC.drain(100));

    assertThat(target.damageTaken()).isEqualTo(400);
  }

  @Test
  void drainStacking() {
    target.hit("#1", MAGIC.drain(100), MAGIC.weakness(100));
    target.hit("#1", MAGIC.drain(100), MAGIC.weakness(100));

    assertThat(target.damageTaken()).isEqualTo(200);
  }

  @Test
  void drainKill() {
    target.hit(MAGIC.drain(10000));

    assertThat(target.damageTaken()).isEqualTo(10000);
    assertThat(target.isAlive()).isFalse();

    target.resolve();

    assertThat(target.damageTaken()).isEqualTo(10000);
  }

  @Test
  void drainReplaceOnDeath() {
    target.hit("#1", MAGIC.drain(10000), MAGIC.weakness(100));
    target.hit("#1", MAGIC.drain(10000), MAGIC.weakness(100));

    assertThat(target.damageTaken()).isEqualTo(10000);

    target.resolve();

    assertThat(target.damageTaken()).isEqualTo(10000);
  }

  @Test
  void enchantsSuck() {
    DIFFICULTY = 100;

    target.hit("#1", FIRE.weakness(100), MAGIC.weakness(100));
    target.hit("#2", FIRE.weakness(100), MAGIC.weakness(100));

    target.hit(FIRE.damage(40));  // grand soul enchant (no extras, 1s)

    assertDamageTaken(106.67);

    resurrect("Standard enemy");

    target.hit("#1", FIRE.weakness(100), MAGIC.weakness(100));
    target.hit("#2", FIRE.weakness(100), MAGIC.weakness(100));

    target.hit(FIRE.damage(5).forSecs(14));  // grand soul enchant (no extras)

    assertDamageTaken(186.67);  // ~13 * 14

    resurrect("Breton", MAGIC.resist(50));

    target.hit("#1", FIRE.weakness(100), MAGIC.weakness(100));
    target.hit("#2", FIRE.weakness(100), MAGIC.weakness(100));

    target.hit(FIRE.damage(5).forSecs(14));

    assertDamageTaken(58.33);  // ~4 * 14
  }

  @Test
  void poisonOP() {
    DIFFICULTY = 100;

    target.hit("#1", POISON.weakness(100), MAGIC.weakness(100));
    target.hit("#2", POISON.weakness(100), MAGIC.weakness(100));

    target.hit(POISON, MAGIC.damage(4).forSecs(15));  // journeyman-ish poison with 1 effect

    assertDamageTaken(960);  // 64 * 15

    resurrect("Argonian", POISON.resist(100));

    target.hit("#1", POISON.weakness(100), MAGIC.weakness(100));
    target.hit("#2", POISON.weakness(100), MAGIC.weakness(100));

    target.hit(POISON, MAGIC.damage(4).forSecs(15));

    assertDamageTaken(720);  // 48 * 15

    resurrect("Breton", MAGIC.resist(50));

    target.hit("#1", POISON.weakness(100), MAGIC.weakness(100));
    target.hit("#2", POISON.weakness(100), MAGIC.weakness(100));

    target.hit(POISON, MAGIC.damage(4).forSecs(15));

    assertDamageTaken(300);  // 20 * 15
  }

  @Test
  void dontEnchantWeaponsWithPoisonWeakness() {
    Hit combo = new Hit(
      MELEE.create("Weapon", POISON.weakness(100).forSecs(5), MAGIC.weakness(100).forSecs(5)),
      POISON.create("Poison", MAGIC.damage(10))
    );

    target.hit(combo);
    target.tick(1);
    assertThat(target.damageTaken()).isEqualTo(10, within(0.01));

    target.hit(combo);
    target.tick(1);
    assertThat(target.damageTaken()).isEqualTo(20, within(0.01));
    // both weakness effects are bundled with poison on the same hit
    // they always have precedence so they override existing effects which then cannot affect the poison
  }

  @Test
  void poisonStacks() {
    target.hit(POISON.create("Poison #1", MAGIC.damage(10), FROST.damage(10), SHOCK.damage(10)));
    target.hit(POISON.create("Poison #1", MAGIC.damage(10), FROST.damage(10), SHOCK.damage(10)));
    target.hit(POISON.create("Poison #1", MAGIC.damage(10), FROST.damage(10), SHOCK.damage(10)));

    assertDamageTaken(90);
  }

  private void resurrect(String description, EffectText... baseEffects) {
    target = new Enemy(1000, baseEffects);
  }

  private void assertDamageTaken(double expected) {
    target.resolve();
    assertThat(target.damageTaken()).isEqualTo(expected, within(0.01));
  }

  private void assertHealthRemaining(double expected) {
    target.resolve();
    assertThat(target.healthRemaining()).isEqualTo(expected, within(0.01));
  }

}
