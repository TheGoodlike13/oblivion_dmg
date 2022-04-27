package eu.goodlike.oblivion;

import eu.goodlike.oblivion.command.RepeatHit;
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
  void thisIsNotDarkSouls3() {
    sendInput("enemy 1000 999 1001");

    assertOutput("You face the enemy (1001.0 hp).");
  }

  @Test
  void nowThatsALotOfDamage() {
    sendInput("+s 100m10s 100d 100wm");

    assertOutput("Hit #1: SPELL {MAGIC DMG 100 for 10s + DRAIN LIFE 100 for 1s + RESIST MAGIC -100 for 1s}");
  }

  @Test
  void bigFuckingHit() {
    sendInput("+p 9999m 9999f 9999s +a 9999fr +b 9999m");

    assertOutput("Hit #1: " +
      "BOW {MAGIC DMG 9999 for 1s} + " +
      "ARROW {FROST DMG 9999 for 1s} + " +
      "POISON {MAGIC DMG 9999 for 1s + FIRE DMG 9999 for 1s + SHOCK DMG 9999 for 1s}");
  }

  @Test
  void sliceAndDice() {
    sendInput("+m 1f", "+m 1f");

    assertOutput("Hit #1: MELEE {FIRE DMG 1 for 1s}", "Hit #2: MELEE {FIRE DMG 1 for 1s}");
  }

  @Test
  void iToldYouToTakeHisStaff() {
    sendInput("+st 9999s");

    assertOutput("Hit #1: STAFF {SHOCK DMG 9999 for 1s}");
  }

  @Test
  void letsKillDaHo() {
    sendInput("enemy @beeeetch 999", "+s 1000s", "go");

    assertOutputSegment(
      "You face the beeeetch (999.0 hp).",
      "Hit #1: SPELL {SHOCK DMG 1000 for 1s}",
      "00.000 You hit with SPELL {SHOCK DMG 1000 for 1s}",
      "01.000 The beeeetch has died.",
      "01.000 All effects have expired."
    );
  }

  @Test
  void instakill() {
    sendInput("enemy 99", "+s 100d", "go");

    assertOutputSegment(
      "You face the enemy (99.0 hp).",
      "Hit #1: SPELL {DRAIN LIFE 100 for 1s}",
      "00.000 You hit with SPELL {DRAIN LIFE 100 for 1s}",
      "00.000 The enemy has died.",
      "01.000 All effects have expired."
    );
  }

  @Test
  void definitionOfInsanity() {
    sendInput("enemy 10", "+s 10m", "go", "+s 10m", "go", "hit #1", "go");

    assertOutputSegment(
      "You face the enemy (10.0 hp).",
      "Hit #1: SPELL {MAGIC DMG 10 for 1s}",
      "00.000 You hit with SPELL {MAGIC DMG 10 for 1s}",
      "01.000 The enemy has died.",
      "01.000 All effects have expired.",
      "-----",
      "You face the enemy (10.0 hp).",
      "Hit #2: SPELL {MAGIC DMG 10 for 1s}",
      "00.000 You hit with SPELL {MAGIC DMG 10 for 1s}",
      "01.000 The enemy has died.",
      "01.000 All effects have expired.",
      "-----",
      "You face the enemy (10.0 hp).",
      "Hit #1: SPELL {MAGIC DMG 10 for 1s}",
      "00.000 You hit with SPELL {MAGIC DMG 10 for 1s}",
      "01.000 The enemy has died.",
      "01.000 All effects have expired."
    );
  }

  @Test
  void watchYourHitting() {
    sendInput("hit #1");

    assertOutput("Bad input: No such hit <#1>");
  }

  @Test
  void thisAintNoDatabaseBoy() {
    sendInput("enemy 30", "+s 10m", "hit #1 #2", "go");

    assertOutputSegment(
      "You face the enemy (30.0 hp).",
      "Hit #1: SPELL {MAGIC DMG 10 for 1s}",
      "Hit #1: SPELL {MAGIC DMG 10 for 1s}",
      "Bad input: No such hit <#2>",
      "00.000 You hit with SPELL {MAGIC DMG 10 for 1s}",
      "00.000 You hit with SPELL {MAGIC DMG 10 for 1s}",
      "01.000 All effects have expired."
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
