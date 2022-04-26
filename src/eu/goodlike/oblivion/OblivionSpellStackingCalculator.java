package eu.goodlike.oblivion;

import eu.goodlike.oblivion.command.SetHit;

import java.util.Scanner;
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.split;

public final class OblivionSpellStackingCalculator {

  public static boolean ITS_ALL_OVER = false;

  public static void main(String... args) {
    try (Scanner scanner = new Scanner(System.in)) {
      OblivionSpellStackingCalculator calc = new OblivionSpellStackingCalculator(scanner::nextLine);
      calc.intro();
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

  public OblivionSpellStackingCalculator(Supplier<String> reader) {
    this.reader = reader;
  }

  private final Supplier<String> reader;

  private Command nextCommand() {
    String[] input;
    do {
      Write.inline(">> ");
      input = split(reader.get().trim().toLowerCase(), ' ');
    } while (input.length == 0);

    Command command = newCommand(input[0]);
    command.setParams(input);
    return command;
  }

  private Command newCommand(String input0) {
    if (input0.startsWith("+")) {
      return new SetHit();
    }

    return Command.Name.find(input0).newCommand();
  }

}
