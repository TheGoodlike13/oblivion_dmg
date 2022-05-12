package eu.goodlike.oblivion;

import eu.goodlike.oblivion.command.EnqueueHit;
import eu.goodlike.oblivion.command.RepeatHit;

import java.util.Scanner;
import java.util.function.Supplier;

import static eu.goodlike.oblivion.Global.ITS_ALL_OVER;
import static eu.goodlike.oblivion.Global.Settings.DIFFICULTY;
import static eu.goodlike.oblivion.Global.Settings.EFFECTIVENESS;
import static eu.goodlike.oblivion.Global.Settings.LEVEL;
import static eu.goodlike.oblivion.Global.Settings.PARSE_MODE;

public final class SpellStackingCalculator {

  public static void main(String... args) {
    Global.initializeEverything();
    writeIntro();
    writeSettings();

    try (Scanner scanner = new Scanner(System.in)) {
      new SpellStackingCalculator(scanner::nextLine).run();
    }
  }

  public static void writeIntro() {
    Write.separator();
    Write.line("Welcome to Oblivion Spell Stacking Calculator!");
    Write.line("Please select an enemy, cast some spells or perform attacks and GO!");
    Write.line("You can quit any time ;)");
    Write.separator();
  }

  public static void writeSettings() {
    Write.line("Configurable settings:");
    Write.line("Player level: " + LEVEL);
    Write.line("Difficulty slider: " + DIFFICULTY);
    Write.line("Spell effectiveness: " + EFFECTIVENESS);
    Write.line("Parse mode: " + PARSE_MODE);
    Write.separator();
  }

  public void run() {
    while (!ITS_ALL_OVER) {
      nextCommand().execute();
    }
  }

  public SpellStackingCalculator(Supplier<String> reader) {
    this.reader = reader;
  }

  private final Supplier<String> reader;

  private Command nextCommand() {
    String[] input;
    do {
      Write.inline(">> ");
      input = Parse.line(reader.get());
    } while (input.length == 0);

    Command command = newCommand(input[0]);
    command.setParams(input);
    return command;
  }

  private Command newCommand(String input0) {
    if (input0.startsWith("+") || input0.startsWith("$")) {
      return new EnqueueHit();
    }
    if (input0.startsWith("#")) {
      return new RepeatHit();
    }
    return Parse.firstMatch(input0, Command.Name.class)
      .orElse(Command.Name.WHAT)
      .newCommand();
  }

}
