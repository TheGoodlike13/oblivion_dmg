package eu.goodlike.oblivion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static eu.goodlike.oblivion.Global.Settings.DIFFICULTY;
import static org.apache.commons.lang3.StringUtils.split;
import static org.assertj.core.api.Assertions.assertThat;

class SpellStackingCalculatorTest implements Supplier<String>, Consumer<String> {

  private final SpellStackingCalculator calc = new SpellStackingCalculator(this);

  private Iterator<String> input;
  private List<String> output;

  @Override
  public String get() {
    return input.next();
  }

  @Override
  public void accept(String s) {
    if (!">> ".equals(s)) {
      output.add(s);
    }
  }

  @BeforeAll
  static void initialize() {
    Global.initializeEverything();
  }

  @BeforeEach
  void setup() {
    Global.WRITER = this;
    input = null;
    output = new ArrayList<>();
    Global.ITS_ALL_OVER = false;
  }

  @AfterEach
  void tearDown() {
    Global.initializeEverything();
  }

  @Test
  void unrecognizedOutput() {
    sendInput("?");

    assertOutput("No idea what <?> is supposed to mean.");
  }

  @Test
  void quitImmediately() {
    sendInput("quit");

    assertOutput();
  }

  @Test
  void prefixWorksForCommands() {
    sendInput("q");

    assertOutput();
  }

  @Test
  void newEnemy() {
    sendInput("enemy 1000");

    assertOutput("You face the enemy (1000 hp)");
  }

  @Test
  void whereHp() {
    sendInput("enemy");

    assertOutput("Bad input: Cannot parse enemy hp <>");
  }

  @Test
  void spaceMan() {
    sendInput("enemy        1000");

    assertOutput("You face the enemy (1000 hp)");
  }

  @Test
  void iAintAfraidOfNoTabs() {
    sendInput("enemy\t\t\t1000");

    assertOutput("You face the enemy (1000 hp)");
  }

  @Test
  void thisIsNotDarkSouls3() {
    sendInput("enemy 1001 999 1000");

    assertOutput("Bad input: Invalid effect format <999>");
  }

  @Test
  void whoIsTheRealEnemy() {
    sendInput("enemy 100 :enemy", "enemy $enemy");

    assertOutput(
      "You face the enemy (100 hp)",
      "Bad input: Nothing matches <enemy>"
    );
  }

  @Test
  void nowThatsALotOfDamage() {
    sendInput("+s 100m10s 100d 100wm");

    assertOutput("[#1] Next hit: <SPELL$1> {MAGIC DMG 100 for 10s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s}");
  }

  @Test
  void bigFuckingHit() {
    sendInput("+p 9999m 9999f 9999s +a 9999fr +b 9999m");

    assertOutput("[#1] Next hit: " +
      "<ARROW$2> {FROST DMG 9999 for 1s} + " +
      "<POISON$1> {MAGIC DMG 9999 for 1s + FIRE DMG 9999 for 1s + SHOCK DMG 9999 for 1s} + " +
      "<BOW$3> {MAGIC DMG 9999 for 1s}");
  }

  @Test
  void sliceAndDice() {
    sendInput("+m 1f", "$1", "$1", "$1", "$1");

    assertOutput(
      "[#1] Next hit: <MELEE$1> {FIRE DMG 1 for 1s}",
      "[#2] Next hit: <MELEE$1> {FIRE DMG 1 for 1s}",
      "[#3] Next hit: <MELEE$1> {FIRE DMG 1 for 1s}",
      "[#4] Next hit: <MELEE$1> {FIRE DMG 1 for 1s}",
      "[#5] Next hit: <MELEE$1> {FIRE DMG 1 for 1s}"
    );
  }

  @Test
  void iToldYouToTakeHisStaff() {
    sendInput("+st 9999s");

    assertOutput("[#1] Next hit: <STAFF$1> {SHOCK DMG 9999 for 1s}");
  }

  @Test
  void letsKillDaHo() {
    sendInput("enemy :beeeetch 999", "+s 1000s", "go");

    assertOutputSegment(
      "You face the beeeetch (999 hp)",
      "[#1] Next hit: <SPELL$1> {SHOCK DMG 1000 for 1s}",
      "00.000 You cast <SPELL$1>",
      "00.410 You hit with <SPELL$1>",
      "       Applied SHOCK DMG 1000.0 for 1s",
      "01.409 The beeeetch has died. Breakdown:",
      "           <SPELL$1> SHOCK DMG: 999.00"
    );
  }

  @Test
  void instakill() {
    sendInput("enemy 99", "+s 100d", "go");

    assertOutputSegment(
      "You face the enemy (99 hp)",
      "[#1] Next hit: <SPELL$1> {DRAIN LIFE 100 for 1s}",
      "00.000 You cast <SPELL$1>",
      "00.410 You hit with <SPELL$1>",
      "       Applied DRAIN LIFE 100.0 for 1s",
      "       The enemy hp drained [-1.0/99]",
      "       The enemy has died. Breakdown:",
      "           <SPELL$1> DRAIN LIFE: 100.00"
    );
  }

  @Test
  void wimp() {
    sendInput("enemy 100", "+s 50d", "go");

    assertOutputSegment(
      "You face the enemy (100 hp)",
      "[#1] Next hit: <SPELL$1> {DRAIN LIFE 50 for 1s}",
      "00.000 You cast <SPELL$1>",
      "00.410 You hit with <SPELL$1>",
      "       Applied DRAIN LIFE 50.0 for 1s",
      "       The enemy hp drained [50.0/100]",
      "01.410 Expired <SPELL$1> DRAIN LIFE",
      "       The enemy hp restored [100.0/100]",
      "The enemy has survived 0.0 damage (100.0 hp left).",
      "-----"
    );
  }

  @Test
  void definitionOfInsanity() {
    sendInput("enemy 10", "+s 10m", "go", "+s 10m", "go", "#1", "go");

    assertOutputSegment(
      "You face the enemy (10 hp)",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "00.000 You cast <SPELL$1>",
      "00.410 You hit with <SPELL$1>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "01.410 The enemy has died. Breakdown:",
      "           <SPELL$1> MAGIC DMG: 10.00",
      "       Expired <SPELL$1> MAGIC DMG",
      "The enemy took a total of 10.0 damage (0.0 overkill).",
      "-----",
      "You face the enemy (10 hp)",
      "[#2] Next hit: <SPELL$2> {MAGIC DMG 10 for 1s}",
      "00.000 You cast <SPELL$2>",
      "00.410 You hit with <SPELL$2>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "01.410 The enemy has died. Breakdown:",
      "           <SPELL$2> MAGIC DMG: 10.00",
      "       Expired <SPELL$2> MAGIC DMG",
      "The enemy took a total of 10.0 damage (0.0 overkill).",
      "-----",
      "You face the enemy (10 hp)",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "00.000 You cast <SPELL$1>",
      "00.410 You hit with <SPELL$1>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "01.410 The enemy has died. Breakdown:",
      "           <SPELL$1> MAGIC DMG: 10.00",
      "       Expired <SPELL$1> MAGIC DMG",
      "The enemy took a total of 10.0 damage (0.0 overkill)."
    );
  }

  @Test
  void watchYourHitting() {
    sendInput("#1");

    assertOutput("Bad input: Nothing matches <1>");
  }

  @Test
  void thisAintNoDatabaseBoy() {
    sendInput("enemy 30", "+s 10m", "#1 #2", "go");

    assertOutputSegment(
      "You face the enemy (30 hp)",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "Bad input: Nothing matches <2>",
      "00.000 You cast <SPELL$1>",
      "00.410 You hit with <SPELL$1>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "01.140 You cast <SPELL$1>",
      "01.410 Expired <SPELL$1> MAGIC DMG",
      "01.550 You hit with <SPELL$1>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "02.550 Expired <SPELL$1> MAGIC DMG",
      "The enemy has survived 20.0 damage (10.0 hp left).",
      "       Damage by effect:",
      "           <SPELL$1> MAGIC DMG: 20.00",
      "-----"
    );
  }

  @Test
  void couldYouRepeatThat() {
    sendInput("+s 10m", "#1 x3");

    assertOutput(
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}"
    );
  }

  @Test
  void repeatWhat() {
    sendInput("x3");

    assertOutput("No idea what <x3> is supposed to mean.");
  }

  @Test
  void whySoSerious() {
    sendInput("+s 1m", "#1 xD");

    assertOutput(
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 1 for 1s}",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 1 for 1s}",
      "Bad input: Cannot parse repeat count <d>"
    );
  }

  @Test
  void howManyTimesOldMan() {
    sendInput("go", "+s 10m", "go", "undo", "enemy 10", "go");

    assertOutput(
      "You stare at the void.",
      "The void stares at you.",
      "How about casting some spells?",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "All your hits land on the wall.",
      "Good job.",
      "How about picking an enemy?",
      "Removed hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "You face the enemy (10 hp)",
      "You stare at the enemy.",
      "The enemy stares at you.",
      "How about casting some spells?"
    );
  }

  @Test
  void resetIsSeriousBusiness() {
    sendInput("+s 10m", "reset");

    assertOutput(
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "-----"
    );

    setup();

    sendInput("+s 20m");

    assertOutput("[#1] Next hit: <SPELL$1> {MAGIC DMG 20 for 1s}");
  }

  @Test
  void imAlteringTheDeal() {
    sendInput("+s 10m", "undo");

    assertOutput(
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "Removed hit: <SPELL$1> {MAGIC DMG 10 for 1s}"
    );
  }

  @Test
  void imAlteringTheDealFurther() {
    sendInput("+s 10m", "+s 10m", "undo 2");

    assertOutput(
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "[#2] Next hit: <SPELL$2> {MAGIC DMG 10 for 1s}",
      "Removed hit: <SPELL$2> {MAGIC DMG 10 for 1s}",
      "Removed hit: <SPELL$1> {MAGIC DMG 10 for 1s}"
    );
  }

  @Test
  void whatTheFuckAmIDoing() {
    sendInput("undo", "+s 10m", "undo 2", "undo xxx");

    assertOutput(
      "No hits to remove.",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "Removed hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "No hits to remove.",
      "Bad input: Cannot parse undo amount <xxx>"
    );
  }

  @Test
  void nameIsTheNameOfTheGame() {
    sendInput("+b 100d10s +a 10f5s :fire_arrow +p 10m", "$1 $fire_arrow +p 10s");

    assertOutput(
      "[#1] Next hit: <ARROW$fire_arrow> {FIRE DMG 10 for 5s} + <POISON$2> {MAGIC DMG 10 for 1s} + <BOW$1> {DRAIN LIFE 100 for 10s}",
      "[#2] Next hit: <ARROW$fire_arrow> {FIRE DMG 10 for 5s} + <POISON$3> {SHOCK DMG 10 for 1s} + <BOW$1> {DRAIN LIFE 100 for 10s}"
    );
  }

  @Test
  void numericNamesAreWeird() {
    sendInput("+s 1m :1", "+s 3m :3", "+s 4m", "+s 2m :2", "$2");

    assertOutput(
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 1 for 1s}",
      "[#2] Next hit: <SPELL$3> {MAGIC DMG 3 for 1s}",
      "[#3] Next hit: <SPELL$4> {MAGIC DMG 4 for 1s}",
      "Bad input: Already reserved <2>",
      "Bad input: Nothing matches <2>"
    );
  }

  @Test
  void duplicateNamesNotAllowed() {
    sendInput("+m 1m :magic_1 +p 1m :magic_1", "$magic_1");

    assertOutput(
      "Bad input: Name already in use <magic_1>",
      "[#1] Next hit: <MELEE$magic_1> {MAGIC DMG 1 for 1s}"
    );
  }

  @Test
  void unknownName() {
    sendInput("$definitely_not_in_there");

    assertOutput("Bad input: Nothing matches <definitely_not_in_there>");
  }

  @Test
  void gettingALittleWildHere() {
    sendInput("+s 1m :poke", "$poke 1f");

    assertOutput(
      "[#1] Next hit: <SPELL$poke> {MAGIC DMG 1 for 1s}",
      "Bad input: Dangling hit param <1f>"
    );
  }


  @Test
  void heGivethAndHeTakethAway() {
    sendInput("enemy 100 :bad_idea", "+s 1m :bad_idea", "forget $bad_idea", "enemy $bad_idea", "$bad_idea");

    assertOutput(
      "You face the bad idea (100 hp)",
      "[#1] Next hit: <SPELL$bad_idea> {MAGIC DMG 1 for 1s}",
      "All references to <bad_idea> were removed from caches.",
      "Bad input: Nothing matches <bad_idea>",
      "Bad input: Nothing matches <bad_idea>"
    );
  }

  @Test
  void tryNotToForgetTooMuch() {
    sendInput("forget", "forget what", "forget $what");

    assertOutput(
      // I guess the app... FORGOT... to print something when there are no args!
      "All references to <what> were removed from caches.",
      "All references to <what> were removed from caches."
    );
  }

  @Test
  void cantForgetByPrefix() {
    sendInput("enemy 100 :good_idea", "forget $good", "enemy $good");

    assertOutput(
      "You face the good idea (100 hp)",
      "All references to <good> were removed from caches.",
      "You face the good idea (100 hp)"
    );
  }

  @Test
  void justCantBeDone() {
    sendInput("+m 1m +s 1m", "$1 +st 1m", "$1 +p 1m :1m $1m");

    assertOutput(
      "<MELEE$1> {MAGIC DMG 1 for 1s}",
      "<SPELL$2> {MAGIC DMG 1 for 1s}",
      "Bad input: Invalid hit: MELEE + SPELL; expected one of [SPELL, POWER, STAFF, MELEE, MELEE + POISON, ARROW + BOW, ARROW + POISON + BOW]",
      "<MELEE$1> {MAGIC DMG 1 for 1s}",
      "<STAFF$3> {MAGIC DMG 1 for 1s}",
      "Bad input: Invalid hit: MELEE + STAFF; expected one of [SPELL, POWER, STAFF, MELEE, MELEE + POISON, ARROW + BOW, ARROW + POISON + BOW]",
      "<MELEE$1> {MAGIC DMG 1 for 1s}",
      "<POISON$1m> {MAGIC DMG 1 for 1s}",
      "<POISON$1m> {MAGIC DMG 1 for 1s}",
      "Bad input: Invalid hit: MELEE + POISON + POISON; expected one of [SPELL, POWER, STAFF, MELEE, MELEE + POISON, ARROW + BOW, ARROW + POISON + BOW]"
    );
  }

  @Test
  void andNowForSomethingCompletelyDifferent() {
    sendInput(
      "difficulty 100",
      "enemy $skeleton_champion",
      "$divine_justice_apprentice",
      "$divine_justice_expert",
      "#1 #2",
      "$aetherius",
      "go"
    );

    assertOutputSegment(
      "Difficulty slider has been set to <100.0>",
      "You face the skeleton champion (350 hp)",
      "POISON x0.00",
      "FROST  x0.30",
      "[#1] Next hit: <SPELL$divine_justice_apprentice> {RESIST MAGIC -100 for 6s + RESIST SHOCK -100 for 6s + RESIST POISON -100 for 6s}",
      "[#2] Next hit: <SPELL$divine_justice_expert> {RESIST MAGIC -100 for 6s + RESIST SHOCK -100 for 6s + RESIST POISON -100 for 6s}",
      "[#1] Next hit: <SPELL$divine_justice_apprentice> {RESIST MAGIC -100 for 6s + RESIST SHOCK -100 for 6s + RESIST POISON -100 for 6s}",
      "[#2] Next hit: <SPELL$divine_justice_expert> {RESIST MAGIC -100 for 6s + RESIST SHOCK -100 for 6s + RESIST POISON -100 for 6s}",
      "[#3] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "00.000 You begin equipped with <MELEE$aetherius>",
      "       You cast <SPELL$divine_justice_apprentice>",
      "00.410 You hit with <SPELL$divine_justice_apprentice>",
      "       Applied RESIST MAGIC -100.0 for 6s",
      "       Applied RESIST SHOCK -100.0 for 6s",
      "       Applied RESIST POISON -100.0 for 6s",
      "       Resulting multipliers: MAGIC  x2.00, SHOCK  x2.00, POISON x1.00",
      "01.140 You cast <SPELL$divine_justice_expert>",
      "01.550 You hit with <SPELL$divine_justice_expert>",
      "       Applied RESIST MAGIC -200.0 for 6s",
      "       Applied RESIST SHOCK -200.0 for 6s",
      "       Applied RESIST POISON -200.0 for 6s",
      "       Resulting multipliers: MAGIC  x4.00, SHOCK  x4.00, POISON x3.00",
      "02.280 You cast <SPELL$divine_justice_apprentice>",
      "02.690 You hit with <SPELL$divine_justice_apprentice>",
      "       Replaced RESIST MAGIC -100.0 with -400.0 for 6s",
      "       Replaced RESIST SHOCK -100.0 with -300.0 for 6s",
      "       Replaced RESIST POISON -100.0 with -300.0 for 6s",
      "       Resulting multipliers: MAGIC  x7.00, SHOCK  x6.00, POISON x5.00",
      "03.420 You cast <SPELL$divine_justice_expert>",
      "03.830 You hit with <SPELL$divine_justice_expert>",
      "       Replaced RESIST MAGIC -200.0 with -700.0 for 6s",
      "       Replaced RESIST SHOCK -200.0 with -500.0 for 6s",
      "       Replaced RESIST POISON -200.0 with -500.0 for 6s",
      "       Resulting multipliers: MAGIC  x12.00, SHOCK  x9.00, POISON x8.00",
      "       You swing <MELEE$aetherius>",
      "04.230 You hit with <MELEE$aetherius>",
      "       Applied SHOCK DMG 324.0 for 1s",
      "       Applied DRAIN LIFE 200.0 for 1s",
      "       The skeleton champion hp drained [150.0/350]",
      "       Applied RESIST MAGIC -1200.0 for 1s",
      "       Applied RESIST SHOCK -1200.0 for 1s",
      "       Resulting multipliers: MAGIC  x24.00, SHOCK  x21.00",
      "04.693 The skeleton champion has died. Breakdown:",
      "           <MELEE$aetherius> DRAIN LIFE: 200.00",
      "           <MELEE$aetherius> SHOCK DMG: 150.01",
      "05.230 Expired <MELEE$aetherius> SHOCK DMG",
      "       Expired <MELEE$aetherius> DRAIN LIFE",
      "       Expired <MELEE$aetherius> RESIST MAGIC",
      "       Expired <MELEE$aetherius> RESIST SHOCK",
      "08.690 Expired <SPELL$divine_justice_apprentice> RESIST MAGIC",
      "       Expired <SPELL$divine_justice_apprentice> RESIST SHOCK",
      "       Expired <SPELL$divine_justice_apprentice> RESIST POISON",
      "09.830 Expired <SPELL$divine_justice_expert> RESIST MAGIC",
      "       Expired <SPELL$divine_justice_expert> RESIST SHOCK",
      "       Expired <SPELL$divine_justice_expert> RESIST POISON",
      "The skeleton champion took a total of 524.0 damage (174.0 overkill).",
      "       Overkill by effect:",
      "           <MELEE$aetherius> SHOCK DMG: 174.00",
      "-----"
    );
  }

  @Test
  void prefixesAreMagic() {
    sendInput("$a");

    assertOutput("[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}");
  }

  @Test
  void whenTheSettingsJustAreNotEnough() {
    sendInput("difficulty 100", "difficulty xxx", "difficulty");

    assertOutput(
      "Difficulty slider has been set to <100.0>",
      "Bad input: Cannot parse difficulty setting <xxx>",
      "Bad input: Cannot parse difficulty setting <>"
    );

    assertThat(DIFFICULTY).isEqualTo(100);
  }

  @Test
  void ding() {
    sendInput("enemy $xivilai", "level 31", "refresh");

    assertOutput(
      "You face the xivilai (336 hp)",
      "FIRE   x0.67",
      "SHOCK  x1.20",
      "Player level has been set to <31>.",
      "-----",
      "You face the xivilai (348 hp)",
      "FIRE   x0.67",
      "SHOCK  x1.20"
    );
  }

  @Test
  void lifeDoesNotWait() {
    sendInput("enemy $xivilai", "level 31", "enemy $xivilai");

    assertOutput(
      "You face the xivilai (336 hp)",
      "FIRE   x0.67",
      "SHOCK  x1.20",
      "Player level has been set to <31>.",
      "You face the xivilai (348 hp)",
      "FIRE   x0.67",
      "SHOCK  x1.20"
    );
  }

  @Test
  void thatIsQuiteTheGoblin() {
    sendInput("enemy :goblin 50 [5 25] *10");

    assertOutput("You face the goblin (250 hp)");
  }

  @Test
  void letsNotThinkSoNegatively() {
    // this test corresponds to a certain rat in a basement you can meet at any level (including 1)
    // but all the way up to level 11 it will remain at 20 HP
    // only starting with level 12 will it start scaling

    sendInput(
      "enemy :rat_(from_basement) 20 [11 *20",
      "level 10",
      "enemy $rat",
      "level 1",
      "enemy $rat");

    assertOutput(
      "You face the rat (from basement) (400 hp)",
      "Player level has been set to <10>.",
      "You face the rat (from basement) (20 hp)",
      "Player level has been set to <1>.",
      "You face the rat (from basement) (20 hp)"
    );
  }

  @Test
  void overDrain() {
    sendInput("enemy 10", "+s 100d", "+s 100d", "go");

    assertOutputSegment(
      "You face the enemy (10 hp)",
      "[#1] Next hit: <SPELL$1> {DRAIN LIFE 100 for 1s}",
      "[#2] Next hit: <SPELL$2> {DRAIN LIFE 100 for 1s}",
      "00.000 You cast <SPELL$1>",
      "00.410 You hit with <SPELL$1>",
      "       Applied DRAIN LIFE 100.0 for 1s",
      "       The enemy hp drained [-90.0/10]",
      "       The enemy has died. Breakdown:",
      "           <SPELL$1> DRAIN LIFE: 100.00",
      "01.140 You cast <SPELL$2>",
      "01.410 Expired <SPELL$1> DRAIN LIFE",
      "01.550 You hit with <SPELL$2>",
      "       Applied DRAIN LIFE 100.0 for 1s",
      "02.550 Expired <SPELL$2> DRAIN LIFE",
      "The enemy took a total of 100.0 damage (90.0 overkill).",
      "       Overkill by effect:",
      "           <SPELL$1> DRAIN LIFE: 90.00",
      "-----"
    );
  }

  @Test
  void overDrainHit() {
    sendInput("enemy 10", "+b 100d 1m +p 100d", "go");

    assertOutputSegment(
      "You face the enemy (10 hp)",
      "[#1] Next hit: <ARROW> {NO EFFECTS} + <POISON$2> {DRAIN LIFE 100 for 1s} + <BOW$1> {DRAIN LIFE 100 for 1s + MAGIC DMG 1 for 1s}",
      "00.000 You begin equipped with <BOW$1>",
      "       You aim <ARROW> + <POISON$2> + <BOW$1>",
      "01.581 You hit with <ARROW> + <POISON$2> + <BOW$1>",
      "       Applied (1)<POISON$2> DRAIN LIFE 100.0 for 1s",
      "       The enemy hp drained [-90.0/10]",
      "       The enemy has died. Breakdown:",
      "           (1)<POISON$2> DRAIN LIFE: 100.00",
      "       Applied <BOW$1> DRAIN LIFE 100.0 for 1s",
      "       Applied MAGIC DMG 1.0 for 1s",
      "02.581 Expired (1)<POISON$2> DRAIN LIFE",
      "       Expired <BOW$1> DRAIN LIFE",
      "       Expired <BOW$1> MAGIC DMG",
      "The enemy took a total of 101.0 damage (91.0 overkill).",
      "       Overkill by effect:",
      "           (1)<POISON$2> DRAIN LIFE: 90.00",
      "           <BOW$1> MAGIC DMG: 1.00",
      "-----"
    );
  }

  @Test
  void noEffectNoWorry() {
    sendInput("enemy 10", "+b 100d", "go");

    assertOutputSegment(
      "You face the enemy (10 hp)",
      "[#1] Next hit: <ARROW> {NO EFFECTS} + <BOW$1> {DRAIN LIFE 100 for 1s}",
      "00.000 You begin equipped with <BOW$1>",
      "       You aim <ARROW> + <BOW$1>",
      "01.581 You hit with <ARROW> + <BOW$1>",
      "       Applied DRAIN LIFE 100.0 for 1s",
      "       The enemy hp drained [-90.0/10]",
      "       The enemy has died. Breakdown:",
      "           <BOW$1> DRAIN LIFE: 100.00",
      "02.581 Expired <BOW$1> DRAIN LIFE",
      "The enemy took a total of 100.0 damage (90.0 overkill).",
      "       Overkill by effect:",
      "           <BOW$1> DRAIN LIFE: 90.00",
      "-----"
    );
  }

  @Test
  void countThePoisons() {
    sendInput("enemy 10", "+p 1m", "$1", "$1", "+p 2m", "$2", "go");

    assertOutputSegment(
      "You face the enemy (10 hp)",
      "[#1] Next hit: <MELEE> {NO EFFECTS} + <POISON$1> {MAGIC DMG 1 for 1s}",
      "[#2] Next hit: <MELEE> {NO EFFECTS} + <POISON$1> {MAGIC DMG 1 for 1s}",
      "[#3] Next hit: <MELEE> {NO EFFECTS} + <POISON$1> {MAGIC DMG 1 for 1s}",
      "[#4] Next hit: <MELEE> {NO EFFECTS} + <POISON$2> {MAGIC DMG 2 for 1s}",
      "[#5] Next hit: <MELEE> {NO EFFECTS} + <POISON$2> {MAGIC DMG 2 for 1s}",
      "00.000 You begin equipped with <MELEE>",
      "       You swing <MELEE> + <POISON$1>",
      "00.400 You hit with <MELEE> + <POISON$1>",
      "       Applied MAGIC DMG 1.0 for 1s",
      "       You swing <MELEE> + <POISON$1>",
      "00.680 You hit with <MELEE> + <POISON$1>",
      "       Applied MAGIC DMG 1.0 for 1s",
      "       You swing <MELEE> + <POISON$1>",
      "01.080 You hit with <MELEE> + <POISON$1>",
      "       Applied MAGIC DMG 1.0 for 1s",
      "       You swing <MELEE> + <POISON$2>",
      "01.360 You hit with <MELEE> + <POISON$2>",
      "       Applied MAGIC DMG 2.0 for 1s",
      "       You swing <MELEE> + <POISON$2>",
      "01.400 Expired (1)<POISON$1> MAGIC DMG",
      "01.680 Expired (2)<POISON$1> MAGIC DMG",
      "01.760 You hit with <MELEE> + <POISON$2>",
      "       Applied MAGIC DMG 2.0 for 1s",
      "02.080 Expired (3)<POISON$1> MAGIC DMG",
      "02.360 Expired (1)<POISON$2> MAGIC DMG",
      "02.760 Expired (2)<POISON$2> MAGIC DMG",
      "The enemy has survived 7.0 damage (3.0 hp left).",
      "       Damage by effect:",
      "           (1)<POISON$1> MAGIC DMG: 1.00",
      "           (2)<POISON$1> MAGIC DMG: 1.00",
      "           (3)<POISON$1> MAGIC DMG: 1.00",
      "           (1)<POISON$2> MAGIC DMG: 2.00",
      "           (2)<POISON$2> MAGIC DMG: 2.00"
    );
  }

  @Test
  void youWouldntPoisonARat() {
    sendInput("difficulty 100", "enemy 4 :rat", "$aetherius +p 7s27s", "go");

    assertOutputSegment(
      "Difficulty slider has been set to <100.0>",
      "You face the rat (4 hp)",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s} + <POISON$1> {SHOCK DMG 7 for 27s}",
      "00.000 You begin equipped with <MELEE$aetherius>",
      "       You swing <MELEE$aetherius> + <POISON$1>",
      "00.400 You hit with <MELEE$aetherius> + <POISON$1>",
      "       Applied <MELEE$aetherius> SHOCK DMG 3.0 for 1s",
      "       Applied DRAIN LIFE 16.7 for 1s",
      "       The rat hp drained [-12.7/4]",
      "       The rat has died. Breakdown:",
      "           <MELEE$aetherius> DRAIN LIFE: 16.67",
      "       Applied RESIST MAGIC -100.0 for 1s",
      "       Applied RESIST SHOCK -100.0 for 1s",
      "       Applied (1)<POISON$1> SHOCK DMG 7.0 for 27s",
      "01.400 Expired <MELEE$aetherius> SHOCK DMG",
      "       Expired <MELEE$aetherius> DRAIN LIFE",
      "       Expired <MELEE$aetherius> RESIST MAGIC",
      "       Expired <MELEE$aetherius> RESIST SHOCK",
      "27.400 Expired (1)<POISON$1> SHOCK DMG",
      "The rat took a total of 208.7 damage (204.7 overkill).",
      "       Overkill by effect:",
      "           <MELEE$aetherius> DRAIN LIFE: 12.67",
      "           <MELEE$aetherius> SHOCK DMG: 3.00",
      "           (1)<POISON$1> SHOCK DMG: 189.00",
      "-----"
    );
  }

  @Test
  void whyWontYouDie() {
    sendInput("enemy 1000", "+p 8m30s 9f37s 9s37s", "go");

    assertOutputSegment(
      "You face the enemy (1000 hp)",
      "[#1] Next hit: <MELEE> {NO EFFECTS} + <POISON$1> {MAGIC DMG 8 for 30s + FIRE DMG 9 for 37s + SHOCK DMG 9 for 37s}",
      "00.000 You begin equipped with <MELEE>",
      "       You swing <MELEE> + <POISON$1>",
      "00.400 You hit with <MELEE> + <POISON$1>",
      "       Applied MAGIC DMG 8.0 for 30s",
      "       Applied FIRE DMG 9.0 for 37s",
      "       Applied SHOCK DMG 9.0 for 37s",
      "30.400 Expired (1)<POISON$1> MAGIC DMG",
      "37.400 Expired (1)<POISON$1> FIRE DMG",
      "       Expired (1)<POISON$1> SHOCK DMG",
      "The enemy has survived 906.0 damage (94.0 hp left).",
      "       Damage by effect:",
      "           (1)<POISON$1> MAGIC DMG: 240.00",
      "           (1)<POISON$1> FIRE DMG: 333.00",
      "           (1)<POISON$1> SHOCK DMG: 333.00",
      "-----"
    );
  }

  @Test
  void doubleTap() {
    sendInput("enemy 1000", "+p 8m30s 9f37s 9s37s", "$1", "go");

    assertOutputSegment(
      "You face the enemy (1000 hp)",
      "[#1] Next hit: <MELEE> {NO EFFECTS} + <POISON$1> {MAGIC DMG 8 for 30s + FIRE DMG 9 for 37s + SHOCK DMG 9 for 37s}",
      "[#2] Next hit: <MELEE> {NO EFFECTS} + <POISON$1> {MAGIC DMG 8 for 30s + FIRE DMG 9 for 37s + SHOCK DMG 9 for 37s}",
      "00.000 You begin equipped with <MELEE>",
      "       You swing <MELEE> + <POISON$1>",
      "00.400 You hit with <MELEE> + <POISON$1>",
      "       Applied MAGIC DMG 8.0 for 30s",
      "       Applied FIRE DMG 9.0 for 37s",
      "       Applied SHOCK DMG 9.0 for 37s",
      "       You swing <MELEE> + <POISON$1>",
      "00.680 You hit with <MELEE> + <POISON$1>",
      "       Applied MAGIC DMG 8.0 for 30s",
      "       Applied FIRE DMG 9.0 for 37s",
      "       Applied SHOCK DMG 9.0 for 37s",
      "19.771 The enemy has died. Breakdown:",
      "           (1)<POISON$1> MAGIC DMG: 154.97",
      "           (1)<POISON$1> FIRE DMG: 174.34",
      "           (1)<POISON$1> SHOCK DMG: 174.34",
      "           (2)<POISON$1> MAGIC DMG: 152.73",
      "           (2)<POISON$1> FIRE DMG: 171.82",
      "           (2)<POISON$1> SHOCK DMG: 171.81",
      "30.400 Expired (1)<POISON$1> MAGIC DMG",
      "30.680 Expired (2)<POISON$1> MAGIC DMG",
      "37.400 Expired (1)<POISON$1> FIRE DMG",
      "       Expired (1)<POISON$1> SHOCK DMG",
      "37.680 Expired (2)<POISON$1> FIRE DMG",
      "       Expired (2)<POISON$1> SHOCK DMG",
      "The enemy took a total of 1812.0 damage (812.0 overkill).",
      "       Overkill by effect:",
      "           (2)<POISON$1> SHOCK DMG: 161.19",
      "           (1)<POISON$1> MAGIC DMG: 85.03",
      "           (1)<POISON$1> FIRE DMG: 158.66",
      "           (1)<POISON$1> SHOCK DMG: 158.66",
      "           (2)<POISON$1> MAGIC DMG: 87.27",
      "           (2)<POISON$1> FIRE DMG: 161.18",
      "-----"
    );
  }

  @Test
  void panic() {
    sendInput("difficulty 100", "enemy 330 20rfr :bear", "$aetherius", "#1 x9", "go");

    assertOutputSegment(
      "Difficulty slider has been set to <100.0>",
      "You face the bear (330 hp)",
      "FROST  x0.80",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "[#1] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "00.000 You begin equipped with <MELEE$aetherius>",
      "       You swing <MELEE$aetherius>",
      "00.400 You hit with <MELEE$aetherius>",
      "       Applied SHOCK DMG 3.0 for 1s",
      "       Applied DRAIN LIFE 16.7 for 1s",
      "       The bear hp drained [313.3/330]",
      "       Applied RESIST MAGIC -100.0 for 1s",
      "       Applied RESIST SHOCK -100.0 for 1s",
      "       Resulting multipliers: MAGIC  x2.00, SHOCK  x2.00",
      "       You swing <MELEE$aetherius>",
      "00.680 You hit with <MELEE$aetherius>",
      "       Replaced SHOCK DMG 3.0 with 12.0 for 1s",
      "       Replaced DRAIN LIFE 16.7 with 33.3 for 1s",
      "       The bear hp drained [295.8/330]",
      "       Replaced RESIST MAGIC -100.0 with -200.0 for 1s",
      "       Replaced RESIST SHOCK -100.0 for 1s",
      "       Resulting multipliers: MAGIC  x3.00, SHOCK  x2.00",
      "       You swing <MELEE$aetherius>",
      "01.080 You hit with <MELEE$aetherius>",
      "       Replaced SHOCK DMG 12.0 with 18.0 for 1s",
      "       Replaced DRAIN LIFE 33.3 with 50.0 for 1s",
      "       The bear hp drained [274.4/330]",
      "       Replaced RESIST MAGIC -200.0 with -300.0 for 1s",
      "       Replaced RESIST SHOCK -100.0 for 1s",
      "       Resulting multipliers: MAGIC  x4.00, SHOCK  x2.00",
      "       You swing <MELEE$aetherius>",
      "01.360 You hit with <MELEE$aetherius>",
      "       Replaced SHOCK DMG 18.0 with 24.0 for 1s",
      "       Replaced DRAIN LIFE 50.0 with 66.7 for 1s",
      "       The bear hp drained [252.7/330]",
      "       Replaced RESIST MAGIC -300.0 with -400.0 for 1s",
      "       Replaced RESIST SHOCK -100.0 for 1s",
      "       Resulting multipliers: MAGIC  x5.00, SHOCK  x2.00",
      "       You swing <MELEE$aetherius>",
      "01.760 You hit with <MELEE$aetherius>",
      "       Replaced SHOCK DMG 24.0 with 30.0 for 1s",
      "       Replaced DRAIN LIFE 66.7 with 83.3 for 1s",
      "       The bear hp drained [226.4/330]",
      "       Replaced RESIST MAGIC -400.0 with -500.0 for 1s",
      "       Replaced RESIST SHOCK -100.0 for 1s",
      "       Resulting multipliers: MAGIC  x6.00, SHOCK  x2.00",
      "       You swing <MELEE$aetherius>",
      "02.040 You hit with <MELEE$aetherius>",
      "       Replaced SHOCK DMG 30.0 with 36.0 for 1s",
      "       Replaced DRAIN LIFE 83.3 with 100.0 for 1s",
      "       The bear hp drained [201.3/330]",
      "       Replaced RESIST MAGIC -500.0 with -600.0 for 1s",
      "       Replaced RESIST SHOCK -100.0 for 1s",
      "       Resulting multipliers: MAGIC  x7.00, SHOCK  x2.00",
      "       You swing <MELEE$aetherius>",
      "02.440 You hit with <MELEE$aetherius>",
      "       Replaced SHOCK DMG 36.0 with 42.0 for 1s",
      "       Replaced DRAIN LIFE 100.0 with 116.7 for 1s",
      "       The bear hp drained [170.3/330]",
      "       Replaced RESIST MAGIC -600.0 with -700.0 for 1s",
      "       Replaced RESIST SHOCK -100.0 for 1s",
      "       Resulting multipliers: MAGIC  x8.00, SHOCK  x2.00",
      "       You swing <MELEE$aetherius>",
      "02.720 You hit with <MELEE$aetherius>",
      "       Replaced SHOCK DMG 42.0 with 48.0 for 1s",
      "       Replaced DRAIN LIFE 116.7 with 133.3 for 1s",
      "       The bear hp drained [141.8/330]",
      "       Replaced RESIST MAGIC -700.0 with -800.0 for 1s",
      "       Replaced RESIST SHOCK -100.0 for 1s",
      "       Resulting multipliers: MAGIC  x9.00, SHOCK  x2.00",
      "       You swing <MELEE$aetherius>",
      "03.120 You hit with <MELEE$aetherius>",
      "       Replaced SHOCK DMG 48.0 with 54.0 for 1s",
      "       Replaced DRAIN LIFE 133.3 with 150.0 for 1s",
      "       The bear hp drained [106.0/330]",
      "       Replaced RESIST MAGIC -800.0 with -900.0 for 1s",
      "       Replaced RESIST SHOCK -100.0 for 1s",
      "       Resulting multipliers: MAGIC  x10.00, SHOCK  x2.00",
      "       You swing <MELEE$aetherius>",
      "03.400 You hit with <MELEE$aetherius>",
      "       Replaced SHOCK DMG 54.0 with 60.0 for 1s",
      "       Replaced DRAIN LIFE 150.0 with 166.7 for 1s",
      "       The bear hp drained [74.2/330]",
      "       Replaced RESIST MAGIC -900.0 with -1000.0 for 1s",
      "       Replaced RESIST SHOCK -100.0 for 1s",
      "       Resulting multipliers: MAGIC  x11.00, SHOCK  x2.00",
      "04.400 Expired <MELEE$aetherius> SHOCK DMG",
      "       Expired <MELEE$aetherius> DRAIN LIFE",
      "       The bear hp restored [180.8/330]",
      "       Expired <MELEE$aetherius> RESIST MAGIC",
      "       Expired <MELEE$aetherius> RESIST SHOCK",
      "The bear has survived 149.2 damage (180.8 hp left).",
      "       Damage by effect:",
      "           <MELEE$aetherius> SHOCK DMG: 149.16",
      "       Total damage wasted due to overlap:",
      "       <MELEE$aetherius> SHOCK DMG",
      "             3.0 * 0.72s =    2.16",
      "            12.0 * 0.60s =    7.20",
      "            18.0 * 0.72s =   12.96",
      "            24.0 * 0.60s =   14.40",
      "            30.0 * 0.72s =   21.60",
      "            36.0 * 0.60s =   21.60",
      "            42.0 * 0.72s =   30.24",
      "            48.0 * 0.60s =   28.80",
      "            54.0 * 0.72s =   38.88",
      "       Grand total: 177.84",
      "-----"
    );
  }

  @Test
  void forgotToFortifyDestructionAgain() {
    sendInput(
      "difficulty 100",
      "enemy 330 20rfr :bear",
      "$divine_justice_apprentice",
      "$divine_justice_expert",
      "$aetherius",
      "go"
    );

    assertOutputSegment(
      "Difficulty slider has been set to <100.0>",
      "You face the bear (330 hp)",
      "FROST  x0.80",
      "[#1] Next hit: <SPELL$divine_justice_apprentice> {RESIST MAGIC -100 for 6s + RESIST SHOCK -100 for 6s + RESIST POISON -100 for 6s}",
      "[#2] Next hit: <SPELL$divine_justice_expert> {RESIST MAGIC -100 for 6s + RESIST SHOCK -100 for 6s + RESIST POISON -100 for 6s}",
      "[#3] Next hit: <MELEE$aetherius> {SHOCK DMG 18 for 1s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s + RESIST SHOCK -100 for 1s}",
      "00.000 You begin equipped with <MELEE$aetherius>",
      "       You cast <SPELL$divine_justice_apprentice>",
      "00.410 You hit with <SPELL$divine_justice_apprentice>",
      "       Applied RESIST MAGIC -100.0 for 6s",
      "       Applied RESIST SHOCK -100.0 for 6s",
      "       Applied RESIST POISON -100.0 for 6s",
      "       Resulting multipliers: MAGIC  x2.00, SHOCK  x2.00, POISON x2.00",
      "01.140 You cast <SPELL$divine_justice_expert>",
      "01.550 You hit with <SPELL$divine_justice_expert>",
      "       Applied RESIST MAGIC -200.0 for 6s",
      "       Applied RESIST SHOCK -200.0 for 6s",
      "       Applied RESIST POISON -200.0 for 6s",
      "       Resulting multipliers: MAGIC  x4.00, SHOCK  x4.00, POISON x4.00",
      "       You swing <MELEE$aetherius>",
      "01.950 You hit with <MELEE$aetherius>",
      "       Applied SHOCK DMG 48.0 for 1s",
      "       Applied DRAIN LIFE 66.7 for 1s",
      "       The bear hp drained [263.3/330]",
      "       Applied RESIST MAGIC -400.0 for 1s",
      "       Applied RESIST SHOCK -400.0 for 1s",
      "       Resulting multipliers: MAGIC  x8.00, SHOCK  x8.00",
      "02.950 Expired <MELEE$aetherius> SHOCK DMG",
      "       Expired <MELEE$aetherius> DRAIN LIFE",
      "       The bear hp restored [282.0/330]",
      "       Expired <MELEE$aetherius> RESIST MAGIC",
      "       Expired <MELEE$aetherius> RESIST SHOCK",
      "06.410 Expired <SPELL$divine_justice_apprentice> RESIST MAGIC",
      "       Expired <SPELL$divine_justice_apprentice> RESIST SHOCK",
      "       Expired <SPELL$divine_justice_apprentice> RESIST POISON",
      "07.550 Expired <SPELL$divine_justice_expert> RESIST MAGIC",
      "       Expired <SPELL$divine_justice_expert> RESIST SHOCK",
      "       Expired <SPELL$divine_justice_expert> RESIST POISON",
      "The bear has survived 48.0 damage (282.0 hp left).",
      "       Damage by effect:",
      "           <MELEE$aetherius> SHOCK DMG: 48.00",
      "-----"
    );
  }

  @Test
  void jackOfAllTrades() {
    sendInput(
      "enemy 100",
      "+m 10m :dagger",
      "+b 10m :bow",
      "+p 10m :poison",
      "$dagger",
      "$dagger",
      "+s 10m :spell",
      "$bow",
      "$spell",
      "+st 10m :staff",
      "$staff",
      "$dagger",
      "$dagger",
      "$staff",
      "go"
    );

    assertOutputSegment(
      "You face the enemy (100 hp)",
      "[#1] Next hit: <MELEE$dagger> {MAGIC DMG 10 for 1s}",
      "[#2] Next hit: <ARROW> {NO EFFECTS} + <BOW$bow> {MAGIC DMG 10 for 1s}",
      "[#3] Next hit: <MELEE> {NO EFFECTS} + <POISON$poison> {MAGIC DMG 10 for 1s}",
      "[#4] Next hit: <MELEE$dagger> {MAGIC DMG 10 for 1s}",
      "[#5] Next hit: <MELEE$dagger> {MAGIC DMG 10 for 1s}",
      "[#6] Next hit: <SPELL$spell> {MAGIC DMG 10 for 1s}",
      "[#7] Next hit: <ARROW> {NO EFFECTS} + <BOW$bow> {MAGIC DMG 10 for 1s}",
      "[#8] Next hit: <SPELL$spell> {MAGIC DMG 10 for 1s}",
      "[#9] Next hit: <STAFF$staff> {MAGIC DMG 10 for 1s}",
      "[#10] Next hit: <STAFF$staff> {MAGIC DMG 10 for 1s}",
      "[#11] Next hit: <MELEE$dagger> {MAGIC DMG 10 for 1s}",
      "[#12] Next hit: <MELEE$dagger> {MAGIC DMG 10 for 1s}",
      "[#13] Next hit: <STAFF$staff> {MAGIC DMG 10 for 1s}",
      "00.000 You begin equipped with <MELEE$dagger>",
      "       You swing <MELEE$dagger>",
      "00.400 You hit with <MELEE$dagger>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "00.800 You begin to swap your weapon.",
      "01.400 Expired <MELEE$dagger> MAGIC DMG",
      "01.861 You equip <BOW$bow>",
      "       You aim <ARROW> + <BOW$bow>",
      "03.442 You hit with <ARROW> + <BOW$bow>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "04.102 You begin to swap your weapon.",
      "04.442 Expired <BOW$bow> MAGIC DMG",
      "04.802 You equip <MELEE>",
      "       You swing <MELEE> + <POISON$poison>",
      "05.202 You hit with <MELEE> + <POISON$poison>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "05.602 You begin to swap your weapon.",
      "06.202 Expired (1)<POISON$poison> MAGIC DMG",
      "06.302 You equip <MELEE$dagger>",
      "       You swing <MELEE$dagger>",
      "06.702 You hit with <MELEE$dagger>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "       You swing <MELEE$dagger>",
      "06.982 You hit with <MELEE$dagger>",
      "       Replaced MAGIC DMG 10.0 for 1s",
      "07.562 You cast <SPELL$spell>",
      "07.972 You hit with <SPELL$spell>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "07.982 Expired <MELEE$dagger> MAGIC DMG",
      "08.702 You begin to swap your weapon.",
      "08.972 Expired <SPELL$spell> MAGIC DMG",
      "09.763 You equip <BOW$bow>",
      "       You aim <ARROW> + <BOW$bow>",
      "11.344 You hit with <ARROW> + <BOW$bow>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "12.004 You cast <SPELL$spell>",
      "12.344 Expired <BOW$bow> MAGIC DMG",
      "12.414 You hit with <SPELL$spell>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "13.144 You begin to swap your weapon.",
      "13.414 Expired <SPELL$spell> MAGIC DMG",
      "14.315 You equip <STAFF$staff>",
      "       You invoke <STAFF$staff>",
      "14.845 You hit with <STAFF$staff>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "15.475 You invoke <STAFF$staff>",
      "15.845 Expired <STAFF$staff> MAGIC DMG",
      "16.005 You hit with <STAFF$staff>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "16.635 You begin to swap your weapon.",
      "17.005 Expired <STAFF$staff> MAGIC DMG",
      "17.335 You equip <MELEE$dagger>",
      "       You swing <MELEE$dagger>",
      "17.735 You hit with <MELEE$dagger>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "       You swing <MELEE$dagger>",
      "18.015 You hit with <MELEE$dagger>",
      "       Replaced MAGIC DMG 10.0 for 1s",
      "18.455 The enemy has died. Breakdown:",
      "           <MELEE$dagger> MAGIC DMG: 30.00",
      "           <BOW$bow> MAGIC DMG: 20.00",
      "           (1)<POISON$poison> MAGIC DMG: 10.00",
      "           <SPELL$spell> MAGIC DMG: 20.00",
      "           <STAFF$staff> MAGIC DMG: 20.00",
      "18.595 You begin to swap your weapon.",
      "19.015 Expired <MELEE$dagger> MAGIC DMG",
      "19.766 You equip <STAFF$staff>",
      "       You invoke <STAFF$staff>",
      "20.296 You hit with <STAFF$staff>",
      "       Applied MAGIC DMG 10.0 for 1s",
      "21.296 Expired <STAFF$staff> MAGIC DMG",
      "The enemy took a total of 115.6 damage (15.6 overkill).",
      "       Overkill by effect:",
      "           <MELEE$dagger> MAGIC DMG: 5.60",
      "           <STAFF$staff> MAGIC DMG: 10.00",
      "       Total damage wasted due to overlap:",
      "       <MELEE$dagger> MAGIC DMG",
      "            10.0 * 0.72s =    7.20",
      "            10.0 * 0.72s =    7.20",
      "       Grand total: 14.40",
      "-----"
    );
  }

  @Test
  void holdOnWaitAMinute() {
    sendInput("enemy 100", "wait 1", "+m 50m", "$1", "go");

    assertOutputSegment(
      "You face the enemy (100 hp)",
      "You will wait at least 1.00s between hits",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#2] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "00.000 You begin equipped with <MELEE$1>",
      "       You swing <MELEE$1>",
      "00.400 You hit with <MELEE$1>",
      "       Applied MAGIC DMG 50.0 for 1s",
      "       You wait for 0.60s",
      "01.000 You swing <MELEE$1>",
      "01.400 Expired <MELEE$1> MAGIC DMG",
      "       You hit with <MELEE$1>",
      "       Applied MAGIC DMG 50.0 for 1s",
      "02.400 The enemy has died. Breakdown:",
      "           <MELEE$1> MAGIC DMG: 100.00",
      "       Expired <MELEE$1> MAGIC DMG",
      "The enemy took a total of 100.0 damage (0.0 overkill).",
      "-----"
    );
  }

  @Test
  void timeDoesNotJustWait() {
    sendInput("wait");

    assertOutput("Bad input: Cannot parse wait time <>");
  }

  @Test
  void noTimeToWait() {
    sendInput("enemy 100", "wait 1", "+m 50m", "+m 50m", "go");

    assertOutputSegment(
      "You face the enemy (100 hp)",
      "You will wait at least 1.00s between hits",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#2] Next hit: <MELEE$2> {MAGIC DMG 50 for 1s}",
      "00.000 You begin equipped with <MELEE$1>",
      "       You swing <MELEE$1>",
      "00.400 You hit with <MELEE$1>",
      "       Applied MAGIC DMG 50.0 for 1s",
      "00.800 You begin to swap your weapon.",
      "01.400 Expired <MELEE$1> MAGIC DMG",
      "01.500 You equip <MELEE$2>",
      "       You swing <MELEE$2>",
      "01.900 You hit with <MELEE$2>",
      "       Applied MAGIC DMG 50.0 for 1s",
      "02.900 The enemy has died. Breakdown:",
      "           <MELEE$1> MAGIC DMG: 50.00",
      "           <MELEE$2> MAGIC DMG: 50.00",
      "       Expired <MELEE$2> MAGIC DMG",
      "The enemy took a total of 100.0 damage (0.0 overkill).",
      "-----"
    );
  }

  @Test
  void comboBreaker___________________NOT() {
    sendInput("enemy 100", "wait 0.75", "+m 50m", "$1", "go");

    assertOutputSegment(
      "You face the enemy (100 hp)",
      "You will wait at least 0.75s between hits",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#2] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "00.000 You begin equipped with <MELEE$1>",
      "       You swing <MELEE$1>",
      "00.400 You hit with <MELEE$1>",
      "       Applied MAGIC DMG 50.0 for 1s",
      "       You wait for 0.35s",
      "00.750 You swing <MELEE$1>",
      "01.030 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "02.030 Expired <MELEE$1> MAGIC DMG",
      "The enemy has survived 81.5 damage (18.5 hp left).",
      "       Damage by effect:",
      "           <MELEE$1> MAGIC DMG: 81.50",
      "       Total damage wasted due to overlap:",
      "       <MELEE$1> MAGIC DMG",
      "            50.0 * 0.37s =   18.50",
      "       Grand total: 18.50",
      "-----"
    );
  }

  @Test
  void okActualComboBreakerThisTime() {
    sendInput("enemy 100", "wait 0.85", "+m 50m", "$1", "go");

    assertOutputSegment(
      "You face the enemy (100 hp)",
      "You will wait at least 0.85s between hits",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#2] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "00.000 You begin equipped with <MELEE$1>",
      "       You swing <MELEE$1>",
      "00.400 You hit with <MELEE$1>",
      "       Applied MAGIC DMG 50.0 for 1s",
      "       You wait for 0.45s",
      "00.850 You swing <MELEE$1>",
      "01.250 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "02.250 Expired <MELEE$1> MAGIC DMG",
      "The enemy has survived 92.5 damage (7.5 hp left).",
      "       Damage by effect:",
      "           <MELEE$1> MAGIC DMG: 92.50",
      "       Total damage wasted due to overlap:",
      "       <MELEE$1> MAGIC DMG",
      "            50.0 * 0.15s =    7.50",
      "       Grand total: 7.50",
      "-----"
    );
  }


  @Test
  void stopTheMadman() {
    sendInput("enemy 100", "+m 50m", "#1 x20", "go");

    assertOutputSegment(
      "You face the enemy (100 hp)",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "[#1] Next hit: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "00.000 You begin equipped with <MELEE$1>",
      "       You swing <MELEE$1>",
      "00.400 You hit with <MELEE$1>",
      "       Applied MAGIC DMG 50.0 for 1s",
      "       You swing <MELEE$1>",
      "00.680 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "       You swing <MELEE$1>",
      "01.080 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "       You swing <MELEE$1>",
      "01.360 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "       You swing <MELEE$1>",
      "01.760 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "       You swing <MELEE$1>",
      "02.040 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "       You swing <MELEE$1>",
      "02.400 The enemy has died. Breakdown:",
      "           <MELEE$1> MAGIC DMG: 100.00",
      "02.440 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "       You swing <MELEE$1>",
      "02.720 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "       You swing <MELEE$1>",
      "03.120 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "       You swing <MELEE$1>",
      "03.400 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "       You swing <MELEE$1>",
      "03.800 You hit with <MELEE$1>",
      "       Replaced MAGIC DMG 50.0 for 1s",
      "       You stop beating the dead enemy",
      "04.800 Expired <MELEE$1> MAGIC DMG",
      "Performed 11 hits out of total 21 prepared",
      "Skipped: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "Skipped: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "Skipped: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "Skipped: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "Skipped: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "Skipped: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "Skipped: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "Skipped: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "Skipped: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "Skipped: <MELEE$1> {MAGIC DMG 50 for 1s}",
      "The enemy took a total of 220.0 damage (120.0 overkill).",
      "       Overkill by effect:",
      "           <MELEE$1> MAGIC DMG: 120.00",
      "       Total damage wasted due to overlap:",
      "       <MELEE$1> MAGIC DMG",
      "            50.0 * 0.72s =   36.00",
      "            50.0 * 0.60s =   30.00",
      "            50.0 * 0.72s =   36.00",
      "            50.0 * 0.60s =   30.00",
      "            50.0 * 0.72s =   36.00",
      "            50.0 * 0.60s =   30.00",
      "            50.0 * 0.72s =   36.00",
      "            50.0 * 0.60s =   30.00",
      "            50.0 * 0.72s =   36.00",
      "            50.0 * 0.60s =   30.00",
      "       Grand total: 330.00",
      "-----"
    );
  }

  @Test
  void badTimeToBeWearingArmor() {
    sendInput("enemy 60", "+s 100wm5s", "spell_effect 94", "$1", "+s 10m", "+m 10m", "go");

    assertOutputSegment(
      "You face the enemy (60 hp)",
      "[#1] Next hit: <SPELL$1> {RESIST MAGIC -100 for 5s}",
      "Spell effectiveness has been set to <94>",
      "[#2] Next hit: <SPELL$1> {RESIST MAGIC -94* for 5s}",
      "[#3] Next hit: <SPELL$2> {MAGIC DMG 9* for 1s}",
      "[#4] Next hit: <MELEE$3> {MAGIC DMG 10 for 1s}",
      "00.000 You begin equipped with <MELEE$3>",
      "       You cast <SPELL$1>",
      "00.410 You hit with <SPELL$1>",
      "       Applied RESIST MAGIC -94.0 for 5s",
      "       Resulting multipliers: MAGIC  x1.94",
      "01.140 You cast <SPELL$1>",
      "01.550 You hit with <SPELL$1>",
      "       Replaced RESIST MAGIC -94.0 with -182.4 for 5s",
      "       Resulting multipliers: MAGIC  x2.82",
      "02.280 You cast <SPELL$2>",
      "02.690 You hit with <SPELL$2>",
      "       Applied MAGIC DMG 25.4 for 1s",
      "       You swing <MELEE$3>",
      "03.090 You hit with <MELEE$3>",
      "       Applied MAGIC DMG 28.2 for 1s",
      "03.690 Expired <SPELL$2> MAGIC DMG",
      "04.090 Expired <MELEE$3> MAGIC DMG",
      "06.550 Expired <SPELL$1> RESIST MAGIC",
      "The enemy has survived 53.6 damage (6.4 hp left).",
      "       Damage by effect:",
      "           <SPELL$2> MAGIC DMG: 25.41",
      "           <MELEE$3> MAGIC DMG: 28.24",
      "-----"
    );
  }

  @Test
  void imTired() {
    sendInput(
      "enemy 10000",
      "+s 100wf5s 100ws5s 100wp5s 100wm5s :weakness1",
      "$weakness1 :weakness2",
      "+p 8m30s 9f37s 9s37s",
      "go"
    );

    assertOutputSegment(
      "You face the enemy (10000 hp)",
      "[#1] Next hit: <SPELL$weakness1> {RESIST FIRE -100 for 5s + RESIST SHOCK -100 for 5s + RESIST POISON -100 for 5s + RESIST MAGIC -100 for 5s}",
      "[#2] Next hit: <SPELL$weakness2> {RESIST FIRE -100 for 5s + RESIST SHOCK -100 for 5s + RESIST POISON -100 for 5s + RESIST MAGIC -100 for 5s}",
      "[#3] Next hit: <MELEE> {NO EFFECTS} + <POISON$1> {MAGIC DMG 8 for 30s + FIRE DMG 9 for 37s + SHOCK DMG 9 for 37s}",
      "00.000 You begin equipped with <MELEE>",
      "       You cast <SPELL$weakness1>",
      "00.410 You hit with <SPELL$weakness1>",
      "       Applied RESIST FIRE -100.0 for 5s",
      "       Applied RESIST SHOCK -100.0 for 5s",
      "       Applied RESIST POISON -100.0 for 5s",
      "       Applied RESIST MAGIC -100.0 for 5s",
      "       Resulting multipliers: FIRE   x2.00, SHOCK  x2.00, POISON x2.00, MAGIC  x2.00",
      "01.140 You cast <SPELL$weakness2>",
      "01.550 You hit with <SPELL$weakness2>",
      "       Applied RESIST FIRE -200.0 for 5s",
      "       Applied RESIST SHOCK -200.0 for 5s",
      "       Applied RESIST POISON -200.0 for 5s",
      "       Applied RESIST MAGIC -200.0 for 5s",
      "       Resulting multipliers: FIRE   x4.00, SHOCK  x4.00, POISON x4.00, MAGIC  x4.00",
      "       You swing <MELEE> + <POISON$1>",
      "01.950 You hit with <MELEE> + <POISON$1>",
      "       Applied MAGIC DMG 128.0 for 30s",
      "       Applied FIRE DMG 144.0 for 37s",
      "       Applied SHOCK DMG 144.0 for 37s",
      "05.410 Expired <SPELL$weakness1> RESIST FIRE",
      "       Expired <SPELL$weakness1> RESIST SHOCK",
      "       Expired <SPELL$weakness1> RESIST POISON",
      "       Expired <SPELL$weakness1> RESIST MAGIC",
      "06.550 Expired <SPELL$weakness2> RESIST FIRE",
      "       Expired <SPELL$weakness2> RESIST SHOCK",
      "       Expired <SPELL$weakness2> RESIST POISON",
      "       Expired <SPELL$weakness2> RESIST MAGIC",
      "25.989 The enemy has died. Breakdown:",
      "           (1)<POISON$1> MAGIC DMG: 3076.99",
      "           (1)<POISON$1> FIRE DMG: 3461.62",
      "           (1)<POISON$1> SHOCK DMG: 3461.47",
      "31.950 Expired (1)<POISON$1> MAGIC DMG",
      "38.950 Expired (1)<POISON$1> FIRE DMG",
      "       Expired (1)<POISON$1> SHOCK DMG",
      "The enemy took a total of 14496.0 damage (4496.0 overkill).",
      "       Overkill by effect:",
      "           (1)<POISON$1> FIRE DMG: 1866.46",
      "           (1)<POISON$1> SHOCK DMG: 1866.53",
      "           (1)<POISON$1> MAGIC DMG: 763.01",
      "-----"
    );
  }

  @Test
  void parseMixed() {
    sendInput("parse mixed", "reload", "$enemies_explode", "+s 1f 1f");

    assertOutput(
      "Parse mode has been set to <MIXED>",
      "-----",
      "-----",
      "Caches have been reset.",
      "Warning: using original config files.",
      "If you changed settings or prepared files, use 'restart' instead.",
      "-----",
      "[#1] Next hit: <SPELL$enemies_explode> {FIRE DMG 5 for 5s + FIRE DMG 70 for 1s}",
      "Bad input: Effect types must be unique! Instead: <SPELL> {FIRE DMG 1 for 1s + FIRE DMG 1 for 1s}"
    );
  }

  @Test
  void parseLenient() {
    sendInput("parse lenient", "reload", "$enemies_explode", "+s 1f 1f");

    assertOutput(
      "Parse mode has been set to <LENIENT>",
      "-----",
      "-----",
      "Caches have been reset.",
      "Warning: using original config files.",
      "If you changed settings or prepared files, use 'restart' instead.",
      "-----",
      "[#1] Next hit: <SPELL$enemies_explode> {FIRE DMG 5 for 5s + FIRE DMG 70 for 1s}",
      "[#2] Next hit: <SPELL$1> {FIRE DMG 1 for 1s + FIRE DMG 1 for 1s}"
    );
  }

  @Test
  void parseStrict() {
    sendInput("parse strict", "reload", "$enemies_explode", "+s 1f 1f");

    assertOutput(
      "Parse mode has been set to <STRICT>",
      "-----",
      "Error in line: +s 5f5s 70f :enemies_explode",
      "Effect types must be unique! Instead: <SPELL$enemies_explode> {FIRE DMG 5 for 5s + FIRE DMG 70 for 1s}",
      "Error in line: +s 1s 1s :not_a_real_spell",
      "Effect types must be unique! Instead: <SPELL$not_a_real_spell> {SHOCK DMG 1 for 1s + SHOCK DMG 1 for 1s}",
      "-----",
      "Caches have been reset.",
      "Warning: using original config files.",
      "If you changed settings or prepared files, use 'restart' instead.",
      "-----",
      "Bad input: Nothing matches <enemies_explode>",
      "Bad input: Effect types must be unique! Instead: <SPELL> {FIRE DMG 1 for 1s + FIRE DMG 1 for 1s}"
    );
  }

  @Test
  void justAPassingFad() {
    sendInput("enemy 100", "+s 100wm0s 50d0s 50m0s", "go", "+s 50m0s", "go", "+s 50d0s", "go");

    assertOutputSegment(
      "You face the enemy (100 hp)",
      "[#1] Next hit: <SPELL$1> {RESIST MAGIC -100 (instant) + DRAIN LIFE 50 (instant) + MAGIC DMG 50 (instant)}",
      "00.000 You cast <SPELL$1>",
      "00.410 You hit with <SPELL$1>",
      "       Applied RESIST MAGIC -100.0 (instant)",
      "       Applied DRAIN LIFE 50.0 (instant)",
      "       The enemy hp drained [50.0/100]",
      "       Applied MAGIC DMG 50.0 (instant)",
      "       The enemy has died. Breakdown:",
      "           <SPELL$1> DRAIN LIFE: 50.00",
      "           <SPELL$1> MAGIC DMG: 50.00",
      "The enemy took a total of 100.0 damage (0.0 overkill).",
      "-----",
      "You face the enemy (100 hp)",
      "[#2] Next hit: <SPELL$2> {MAGIC DMG 50 (instant)}",
      "00.000 You cast <SPELL$2>",
      "00.410 You hit with <SPELL$2>",
      "       Applied MAGIC DMG 50.0 (instant)",
      "The enemy has survived 50.0 damage (50.0 hp left).",
      "       Damage by effect:",
      "           <SPELL$2> MAGIC DMG: 50.00",
      "-----",
      "You face the enemy (100 hp)",
      "[#3] Next hit: <SPELL$3> {DRAIN LIFE 50 (instant)}",
      "00.000 You cast <SPELL$3>",
      "00.410 You hit with <SPELL$3>",
      "       Applied DRAIN LIFE 50.0 (instant)",
      "       The enemy hp drained [50.0/100]",
      "       The enemy hp restored [100.0/100]",
      "The enemy has survived 0.0 damage (100.0 hp left).",
      "-----"
    );
  }

  @Test
  void limp() {
    sendInput("enemy 100 10d 10m0s");

    assertOutput("You face the enemy (80 hp)");
  }

  @Test
  void unlimitedPower() {
    sendInput("$nordic", "$nordic", "$nordic");

    assertOutput(
      "[#1] Next hit: <POWER$nordic_frost> {FROST DMG 50 for 1s}",
      "[#2] Next hit: <POWER$nordic_frost> {FROST DMG 50 for 1s}",
      "[#3] Next hit: <POWER$nordic_frost> {FROST DMG 50 for 1s}"
    );
  }

  @Test
  void powerEffectiveness() {
    sendInput("spell_effect 50", "$nordic");

    assertOutput(
      "Spell effectiveness has been set to <50>",
      "[#1] Next hit: <POWER$nordic_frost> {FROST DMG 50 for 1s}"
    );
  }

  private void sendInput(String... lines) {
    List<String> inputLines = new ArrayList<>();
    Collections.addAll(inputLines, lines);
    inputLines.add("quit");
    input = inputLines.iterator();

    calc.run();
  }

  private void assertOutput(String... lines) {
    assertThat(outputLines()).containsExactly(lines);
  }

  private void assertOutputSegment(String... lines) {
    assertThat(outputLines().limit(lines.length)).containsExactly(lines);
  }

  private Stream<String> outputLines() {
    String allOutput = String.join("", output);
    String[] lines = split(allOutput, System.lineSeparator());
    return Stream.of(lines);
  }

}
