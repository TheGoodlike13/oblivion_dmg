package eu.goodlike.oblivion;

import eu.goodlike.oblivion.command.RepeatHit;
import eu.goodlike.oblivion.command.SetHit;

import java.util.Scanner;
import java.util.function.Supplier;

import static eu.goodlike.oblivion.Global.ITS_ALL_OVER;
import static eu.goodlike.oblivion.Global.Settings.DIFFICULTY;
import static eu.goodlike.oblivion.Global.Settings.EFFECTIVENESS;
import static eu.goodlike.oblivion.Global.Settings.LEVEL;

public final class SpellStackingCalculator {

  public static void main(String... args) {
    Global.initializeEverything();
    try (Scanner scanner = new Scanner(System.in)) {
      SpellStackingCalculator calc = new SpellStackingCalculator(scanner::nextLine);
      calc.intro();
      calc.settings();
      calc.run();
    }
  }

  public void run() {
    while (!ITS_ALL_OVER) {
      nextCommand().execute();
    }
  }

  public void intro() {
    Write.line("Welcome to Oblivion spell stacking calculator!");
    Write.line("Please select an enemy, cast some spells or perform attacks and GO!");
    Write.line("You can quit any time ;)");
    Write.separator();
  }

  public void settings() {
    Write.line("Configurable settings:");
    Write.line("Player level: " + LEVEL);
    Write.line("Difficulty slider: " + DIFFICULTY);
    Write.line("Spell effectiveness: " + EFFECTIVENESS);
    Write.separator();
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
      return new SetHit();
    }

    return input0.startsWith("#")
      ? new RepeatHit()
      : Command.Name.find(input0).newCommand();
  }

}
