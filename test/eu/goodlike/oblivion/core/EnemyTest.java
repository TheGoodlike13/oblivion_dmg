package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.Global;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static eu.goodlike.oblivion.Global.Settings.DIFFICULTY;
import static eu.goodlike.oblivion.Global.Settings.LEVEL;
import static eu.goodlike.oblivion.core.Effector.Factory.MELEE;
import static eu.goodlike.oblivion.core.Effector.Factory.SPELL;
import static eu.goodlike.oblivion.core.Factor.FIRE;
import static eu.goodlike.oblivion.core.Factor.FROST;
import static eu.goodlike.oblivion.core.Factor.MAGIC;
import static eu.goodlike.oblivion.core.Factor.POISON;
import static eu.goodlike.oblivion.core.Factor.SHOCK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;

class EnemyTest {

  private static final Effector FIRE_WEAK_1 = SPELL.create(FIRE.weakness(100), MAGIC.weakness(100));
  private static final Effector FIRE_WEAK_2 = FIRE_WEAK_1.copy();

  private static final Effector POISON_WEAK_1 = SPELL.create(POISON.weakness(100), MAGIC.weakness(100));
  private static final Effector POISON_WEAK_2 = POISON_WEAK_1.copy();

  private static final Effector JOURNEYMAN = POISON.create(MAGIC.damage(4).forSecs(15));

  private Enemy target;

  @BeforeEach
  void setup() {
    nextEnemy("Standard enemy");
  }

  @AfterEach
  void tearDown() {
    Global.Settings.load();
  }

  @Test
  void initialHealth() {
    assertDamageTaken(0);
    assertHealthRemaining(1000);
    assertOverkill(0);
    assertThat(target.isAlive()).isTrue();
  }

  @Test
  void alreadyDeadNotAllowed() {
    assertBadStructure(() -> new Enemy(0));
    assertBadStructure(() -> new Enemy(-1));
  }

  @Test
  void damage() {
    target.hit(MAGIC.damage(10));

    assertDamageTaken(10);
    assertHealthRemaining(990);
    assertOverkill(0);
    assertThat(target.isAlive()).isTrue();
  }

  @Test
  void healingNotAllowed() {
    assertBadStructure(() -> MAGIC.damage(-1));
  }

  @Test
  void difficultyMultiplier() {
    DIFFICULTY = 100;

    target.hit(MAGIC.damage(60));

    assertDamageTaken(10);
    assertHealthRemaining(990);
    assertOverkill(0);
    assertThat(target.isAlive()).isTrue();
  }

  @Test
  void death() {
    target.hit(MAGIC.damage(10000));

    assertDamageTaken(10000);
    assertHealthRemaining(0);
    assertOverkill(9000);
    assertThat(target.isAlive()).isFalse();
  }

  @Test
  void damageResisted() {
    target.hit(FIRE.resist(50));

    target.hit(FIRE.damage(10));

    assertDamageTaken(5);
  }

  @Test
  void damageAmplified() {
    target.hit(FIRE.weakness(100));

    target.hit(FIRE.damage(10));

    assertDamageTaken(20);
  }

  @Test
  void damageUnaffected() {
    target.hit(FROST.resist(50));

    target.hit(FIRE.damage(10));

    assertDamageTaken(10);
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
    Effector weaknessToMagic100 = SPELL.create(MAGIC.weakness(100));

    target.hit(weaknessToMagic100);  // +100 (replaced)
    target.hit(weaknessToMagic100);  // +200

    target.hit(MAGIC.damage(10));

    assertDamageTaken(30);
  }

  @Test
  void sameSpellDamageCompleteOverlap() {
    Effector dmgMagic10 = SPELL.create(MAGIC.damage(10));

    target.hit(dmgMagic10);
    target.hit(dmgMagic10);

    assertDamageTaken(10);
  }

  @Test
  void sameSpellDamagePartialOverlap() {
    Effector dmgMagic10 = SPELL.create(MAGIC.damage(10));

    target.hit(dmgMagic10);
    target.tick(0.5);
    target.hit(dmgMagic10);

    assertDamageTaken(15);
  }

  @Test
  void allSpellEffectsAreAppliedAtOnce() {
    target.hit(MAGIC.weakness(100), FIRE.weakness(100), FIRE.damage(10));

    assertDamageTaken(10);
  }

  @Test
  void wastedMagicWeaknessEffectDueToInefficientOrder() {
    Effector weaknessBadOrder = SPELL.create(MAGIC.weakness(100), FIRE.weakness(100));

    target.hit(weaknessBadOrder);  // +100, +100 (replaced)
    target.hit(weaknessBadOrder);  // +200, +100

    target.hit(FIRE.damage(10));

    assertDamageTaken(60);
  }

  @Test
  void efficientOrder() {
    Effector weaknessGoodOrder = SPELL.create(FIRE.weakness(100), MAGIC.weakness(100));

    target.hit(weaknessGoodOrder);  // +100, +100 (replaced)
    target.hit(weaknessGoodOrder);  // +200, +200

    target.hit(FIRE.damage(10));

    assertDamageTaken(90);
  }

  @Test
  void spellStacking_twice() {
    target.hit(FIRE_WEAK_1);  // +100, +100
    target.hit(FIRE_WEAK_2);  // +200, +200

    target.hit(FIRE.damage(10));

    assertDamageTaken(160);
  }

  @Test
  void spellStacking_thrice() {
    target.hit(FIRE_WEAK_1);  // +100, +100 (replaced)
    target.hit(FIRE_WEAK_2);  // +200, +200
    target.hit(FIRE_WEAK_1);  // +400, +400

    target.hit(FIRE.damage(10));

    assertDamageTaken(490);
  }

  @Test
  void spellStacking_fourTimes() {
    target.hit(FIRE_WEAK_1);  // +100, +100 (replaced)
    target.hit(FIRE_WEAK_2);  // +200, +200 (replaced)
    target.hit(FIRE_WEAK_1);  // +400, +400
    target.hit(FIRE_WEAK_2);  // +700, +700

    target.hit(FIRE.damage(10));

    assertDamageTaken(1440);
  }

  @Test
  void spellStacking_fourTimes_inefficient() {
    Effector badWeakness1 = SPELL.create(MAGIC.weakness(100), FIRE.weakness(100));
    Effector badWeakness2 = SPELL.create(MAGIC.weakness(100), FIRE.weakness(100));

    target.hit(badWeakness1);  // +100, +100 (replaced)
    target.hit(badWeakness2);  // +200, +200 (replaced)
    target.hit(badWeakness1);  // +400, +300
    target.hit(badWeakness2);  // +700, +500

    target.hit(FIRE.damage(10));

    assertDamageTaken(1080);
  }

  @Test
  void drainDamagesInstantly() {
    target.hit(MAGIC.drain(100));

    assertThat(target.damageTaken()).isEqualTo(100);
  }

  @Test
  void multipleDrainsFromDifferentSources() {
    target.hit(MAGIC.drain(100));
    target.hit(MAGIC.drain(100));

    assertThat(target.damageTaken()).isEqualTo(200);
  }

  @Test
  void multipleDrainsFromSameSource() {
    Effector drainHp = SPELL.create(MAGIC.drain(100));

    target.hit(drainHp);
    target.hit(drainHp);

    assertThat(target.damageTaken()).isEqualTo(100);
  }

  @Test
  void drainRestoresHealthOnSurvival() {
    target.hit(MAGIC.drain(100));
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
  void drainWeaknessStacking() {
    Effector weapon = MELEE.create(MAGIC.drain(100), MAGIC.weakness(100));

    target.hit(weapon);
    target.hit(weapon);

    assertThat(target.damageTaken()).isEqualTo(200);
  }

  @Test
  void drainKill() {
    target.hit(MAGIC.drain(10000));

    assertThat(target.damageTaken()).isEqualTo(10000);
    assertThat(target.isAlive()).isFalse();
  }

  @Test
  void drainRestoresNoHealthOnDeath() {
    target.hit(MAGIC.drain(10000));
    target.resolve();

    assertThat(target.damageTaken()).isEqualTo(10000);
  }

  @Test
  void drainDoesNotApplyOnDeadTargets() {
    target.hit(MAGIC.drain(10000));
    target.hit(MAGIC.drain(10000));

    assertThat(target.damageTaken()).isEqualTo(10000);
  }

  @Test
  void enchantsSuck_singleHit_grandSoul() {
    DIFFICULTY = 100;

    Effector usedGrandSoul = MELEE.create(FIRE.damage(40));

    target.hit(FIRE_WEAK_1);
    target.hit(FIRE_WEAK_2);
    target.hit(usedGrandSoul);

    assertDamageTaken(106.67);
  }

  @Test
  void enchantsSuck_damageOverTime_grandSoul() {
    DIFFICULTY = 100;

    Effector usedGrandSoul = MELEE.create(FIRE.damage(5).forSecs(14));

    target.hit(FIRE_WEAK_1);
    target.hit(FIRE_WEAK_2);
    target.hit(usedGrandSoul);

    assertDamageTaken(186.67);  // ~13 * 14
  }

  @Test
  void enchantsSuck_breton() {
    DIFFICULTY = 100;

    nextEnemy("Breton", MAGIC.resist(50));

    Effector usedGrandSoul = MELEE.create(FIRE.damage(5).forSecs(14));

    target.hit(FIRE_WEAK_1);
    target.hit(FIRE_WEAK_2);
    target.hit(usedGrandSoul);

    assertDamageTaken(58.33);  // ~4 * 14
  }

  @Test
  void poisonOP_journeyman() {
    DIFFICULTY = 100;

    target.hit(POISON_WEAK_1);
    target.hit(POISON_WEAK_2);
    target.hit(JOURNEYMAN);

    assertDamageTaken(960);  // 64 * 15
  }

  @Test
  void poisonOP_journeyman_argonian() {
    DIFFICULTY = 100;

    nextEnemy("Argonian", POISON.resist(100));

    target.hit(POISON_WEAK_1);
    target.hit(POISON_WEAK_2);
    target.hit(JOURNEYMAN);

    assertDamageTaken(720);  // 48 * 15
  }

  @Test
  void poisonOP_journeyman_breton() {
    DIFFICULTY = 100;

    nextEnemy("Breton", MAGIC.resist(50));

    target.hit(POISON_WEAK_1);
    target.hit(POISON_WEAK_2);
    target.hit(JOURNEYMAN);

    assertDamageTaken(300);  // 20 * 15
  }

  @Test
  void dontEnchantWeaponsWithPoisonWeakness() {
    Hit poisonedWeapon = new Hit(
      MELEE.create(POISON.weakness(100).forSecs(5), MAGIC.weakness(100).forSecs(5)),
      POISON.create(MAGIC.damage(10))
    );

    target.hit(poisonedWeapon);
    target.tick(1);
    target.hit(poisonedWeapon);

    assertDamageTaken(20);
    // both weakness effects are bundled with poison on the same hit
    // they always have precedence so they override existing effects which then cannot affect the poison
  }

  @Test
  void poisonStacks() {
    Effector mix = POISON.create(MAGIC.damage(10), FROST.damage(10), SHOCK.damage(10));

    target.hit(mix);
    target.hit(mix);
    target.hit(mix);

    assertDamageTaken(90);
  }

  @Test
  void spellStacking_withWeapons() {
    Effector weapon1 = MELEE.create(FIRE.weakness(100), MAGIC.weakness(100));
    Effector weapon2 = weapon1.copy();

    target.hit(weapon1);
    target.hit(weapon2);

    target.hit(FIRE.damage(10));

    assertDamageTaken(160);
  }

  @Test
  void floatingPointDeath() {
    target.hit(MAGIC.damage(1000));
    target.resolve();

    assertThat(target.healthRemaining()).isGreaterThan(0);
    assertThat(target.healthRemaining()).isLessThan(0.005);
    assertThat(target.isAlive()).isFalse();
  }

  @Test
  void noPermanentDamageEffect() {
    nextEnemy("This is fine. No, really.", FIRE.damage(10));

    target.tick(10);

    assertDamageTaken(0);
  }

  @Test
  void levels() {
    target.setLeveled(10, 20, 30);
    assertHealthRemaining(1100);

    LEVEL = 25;
    assertHealthRemaining(1100);

    target.updateLevel();
    assertHealthRemaining(1050);

    LEVEL = 1;
    target.updateLevel();
    assertHealthRemaining(1000);

    LEVEL = 50;
    target.updateLevel();
    assertHealthRemaining(1100);
  }

  @Test
  void badLevels() {
    assertBadStructure(() -> target.setLeveled(-1, 1, 2));
    assertBadStructure(() -> target.setLeveled(0, 1, 2));

    assertBadStructure(() -> target.setLeveled(1, -1, 2));
    assertBadStructure(() -> target.setLeveled(1, 0, 2));

    assertBadStructure(() -> target.setLeveled(1, 1, -1));
    assertBadStructure(() -> target.setLeveled(1, 1, 0));
    assertBadStructure(() -> target.setLeveled(1, 1, 1));

    assertBadStructure(() -> target
      .setLeveled(1, 1, 2)
      .setLeveled(1, 1, 2));
  }

  @Test
  void noLevels() {
    target.updateLevel();

    assertHealthRemaining(1000);
  }

  @Test
  void dontBrickOnMeNow() {
    target.hit(FIRE.damage(5).forSecs(5), FIRE.damage(70).forSecs(1));

    assertDamageTaken(95);
  }

  @Test
  void itsAGhost() {
    assertThatExceptionOfType(StructureException.class)
      .isThrownBy(() -> nextEnemy("Dead due to drain", MAGIC.drain(2000)));

    assertThatExceptionOfType(StructureException.class)
      .isThrownBy(() -> nextEnemy("Dead due to permanent damage", MAGIC.damage(2000).instant()));
  }

  @Test
  void alrightFineHaveYourPermanentInstantEffects() {
    nextEnemy("Drain & instant damage should persist I guess", MAGIC.drain(100), MAGIC.damage(100).instant());

    assertHealthRemaining(800);

    target.resurrect();

    assertHealthRemaining(800);
  }

  @Test
  void permanentDamageEffectsIgnoreResists() {
    nextEnemy("Not resisting these", MAGIC.resist(50), MAGIC.drain(100), MAGIC.damage(100).instant());

    assertHealthRemaining(800);

    target.resurrect();

    assertHealthRemaining(800);
  }

  @Test
  void resurrectionKeepsOnlyPermanentResists() {
    nextEnemy("Breton", MAGIC.resist(50));

    target.hit(MAGIC.resist(50));
    target.hit(MAGIC.damage(100));

    assertDamageTaken(25);

    target.resurrect();

    target.hit(MAGIC.damage(100));

    assertDamageTaken(50);
  }

  @SuppressWarnings("unused")
  private void nextEnemy(String description, EffectText... baseEffects) {
    target = new Enemy(1000, baseEffects);
  }

  private void assertDamageTaken(double expected) {
    target.resolve();
    assertThat(target.damageTaken()).isEqualTo(expected, within(0.005));
  }

  private void assertHealthRemaining(double expected) {
    target.resolve();
    assertThat(target.healthRemaining()).isEqualTo(expected, within(0.005));
  }

  private void assertOverkill(double expected) {
    target.resolve();
    assertThat(target.overkill()).isEqualTo(expected, within(0.005));
  }

  private void assertBadStructure(ThrowingCallable throwingCallable) {
    assertThatExceptionOfType(StructureException.class).isThrownBy(throwingCallable);
  }

}
