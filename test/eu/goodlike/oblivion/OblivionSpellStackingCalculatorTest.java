package eu.goodlike.oblivion;

import eu.goodlike.oblivion.command.RepeatHit;
import eu.goodlike.oblivion.command.SetHit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static eu.goodlike.oblivion.Arena.THE_ARENA;
import static eu.goodlike.oblivion.OblivionSpellStackingCalculator.ITS_ALL_OVER;
import static org.apache.commons.lang3.StringUtils.split;
import static org.assertj.core.api.Assertions.assertThat;

class OblivionSpellStackingCalculatorTest implements Supplier<String>, Consumer<String> {

  private final OblivionSpellStackingCalculator calc = new OblivionSpellStackingCalculator(this);

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

  @BeforeEach
  void setup() {
    Write.WRITER = this;
    input = null;
    output = new ArrayList<>();
  }

  @AfterEach
  void tearDown() {
    Write.resetToFactory();
    ITS_ALL_OVER = false;
    THE_ARENA.reset();
    RepeatHit.invalidate();
    SetHit.invalidate();
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

    assertOutput("You face the enemy (1000.0 hp).");
  }

  @Test
  void whereHp() {
    sendInput("enemy");

    assertOutput("Bad input: Cannot parse enemy hp <>");
  }

  @Test
  void spaceMan() {
    sendInput("enemy        1000");

    assertOutput("You face the enemy (1000.0 hp).");
  }

  @Test
  void iAintAfraidOfNoTabs() {
    sendInput("enemy\t\t\t1000");

    assertOutput("You face the enemy (1000.0 hp).");
  }

  @Test
  void thisIsNotDarkSouls3() {
    sendInput("enemy 1001 999 1000");

    assertOutput("You face the enemy (1000.0 hp).");
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
      "<BOW$3> {MAGIC DMG 9999 for 1s} + " +
      "<ARROW$2> {FROST DMG 9999 for 1s} + " +
      "<POISON$1> {MAGIC DMG 9999 for 1s + FIRE DMG 9999 for 1s + SHOCK DMG 9999 for 1s}");
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
    sendInput("enemy @beeeetch 999", "+s 1000s", "go");

    assertOutputSegment(
      "You face the beeeetch (999.0 hp).",
      "[#1] Next hit: <SPELL$1> {SHOCK DMG 1000 for 1s}",
      "00.000 You hit with <SPELL$1> {SHOCK DMG 1000 for 1s}",
      "01.000 The beeeetch has died.",
      "01.000 All effects have expired."
    );
  }

  @Test
  void instakill() {
    sendInput("enemy 99", "+s 100d", "go");

    assertOutputSegment(
      "You face the enemy (99.0 hp).",
      "[#1] Next hit: <SPELL$1> {DRAIN LIFE 100 for 1s}",
      "00.000 You hit with <SPELL$1> {DRAIN LIFE 100 for 1s}",
      "00.000 The enemy has died.",
      "01.000 All effects have expired."
    );
  }

  @Test
  void definitionOfInsanity() {
    sendInput("enemy 10", "+s 10m", "go", "+s 10m", "go", "hit #1", "go");

    assertOutputSegment(
      "You face the enemy (10.0 hp).",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "00.000 You hit with <SPELL$1> {MAGIC DMG 10 for 1s}",
      "01.000 The enemy has died.",
      "01.000 All effects have expired.",
      "-----",
      "You face the enemy (10.0 hp).",
      "[#2] Next hit: <SPELL$2> {MAGIC DMG 10 for 1s}",
      "00.000 You hit with <SPELL$2> {MAGIC DMG 10 for 1s}",
      "01.000 The enemy has died.",
      "01.000 All effects have expired.",
      "-----",
      "You face the enemy (10.0 hp).",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "00.000 You hit with <SPELL$1> {MAGIC DMG 10 for 1s}",
      "01.000 The enemy has died.",
      "01.000 All effects have expired."
    );
  }

  @Test
  void watchYourHitting() {
    sendInput("hit #1");

    assertOutput("Bad input: No hit found <#1>");
  }

  @Test
  void thisAintNoDatabaseBoy() {
    sendInput("enemy 30", "+s 10m", "hit #1 #2", "go");

    assertOutputSegment(
      "You face the enemy (30.0 hp).",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "Bad input: No hit found <#2>",
      "00.000 You hit with <SPELL$1> {MAGIC DMG 10 for 1s}",
      "00.000 You hit with <SPELL$1> {MAGIC DMG 10 for 1s}",
      "01.000 All effects have expired."
    );
  }

  @Test
  void couldYouRepeatThat() {
    sendInput("+s 10m", "hit #1 x3");

    assertOutput(
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}"
    );
  }

  @Test
  void repeatWhat() {
    sendInput("hit x3");

    assertOutput("Bad input: No hit found <>");
  }

  @Test
  void whySoSerious() {
    sendInput("hit xD");

    assertOutput("Bad input: Cannot parse repeat count <d>");
  }

  @Test
  void howManyTimesOldMan() {
    sendInput("go", "enemy 10", "go", "reset", "+s 10m", "go");

    assertOutput(
      "You stare at the void.",
      "The void stares at you.",
      "How about casting some spells?",
      "You face the enemy (10.0 hp).",
      "You stare at the enemy.",
      "The enemy stares at you.",
      "How about casting some spells?",
      "-----",
      "Everything has been reset.",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "All your hits land on the wall.",
      "Good job.",
      "How about picking an enemy?"
    );
  }

  @Test
  void amnesia() {
    sendInput("+s 10m", "reset", "+s 20m");

    assertOutput(
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 10 for 1s}",
      "-----",
      "Everything has been reset.",
      "[#1] Next hit: <SPELL$1> {MAGIC DMG 20 for 1s}"
    );
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
    sendInput("+b 100d10s +a 10f5s @fire_arrow +p 10m", "$1 $fire_arrow +p 10s");

    assertOutput(
      "[#1] Next hit: <BOW$1> {DRAIN LIFE 100 for 10s} + <ARROW$fire_arrow> {FIRE DMG 10 for 5s} + <POISON$2> {MAGIC DMG 10 for 1s}",
      "[#2] Next hit: <BOW$1> {DRAIN LIFE 100 for 10s} + <ARROW$fire_arrow> {FIRE DMG 10 for 5s} + <POISON$3> {SHOCK DMG 10 for 1s}"
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
