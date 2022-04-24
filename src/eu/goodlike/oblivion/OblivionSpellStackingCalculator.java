package eu.goodlike.oblivion;

import com.google.common.collect.ImmutableMap;
import eu.goodlike.oblivion.command.BaseCommand;
import eu.goodlike.oblivion.command.ButWhatDoesThisMean;
import eu.goodlike.oblivion.command.SetEnemy;
import eu.goodlike.oblivion.command.SetHit;
import eu.goodlike.oblivion.global.Write;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Scanner;
import java.util.function.Supplier;

import static eu.goodlike.oblivion.Command.Name.ENEMY;
import static eu.goodlike.oblivion.Command.Name.QUIT;

public final class OblivionSpellStackingCalculator {

  public static void main(String... args) {
    try (Scanner scanner = new Scanner(System.in)) {
      OblivionSpellStackingCalculator calc = new OblivionSpellStackingCalculator(scanner::nextLine);
      calc.intro();
      calc.run();
    }
  }

  public void run() {
    while (!itsAllOverCalculator) {
      nextCommand().execute();
    }
  }

  public void intro() {
    Write.line("Welcome to Oblivion spell stacking calculator!");
    Write.line("Please select an enemy, cast some spells or perform attacks and GO!");
    Write.line("You can quit any time ;)");
  }

  public OblivionSpellStackingCalculator(Supplier<String> reader) {
    this.reader = reader;
  }

  private boolean itsAllOverCalculator = false;

  private final Supplier<String> reader;

  private final Arena arena = new Arena();

  // TODO: move to enum?
  private final Map<Command.Name, Supplier<Command>> commands = ImmutableMap.of(
    ENEMY, SetEnemy::new,
    QUIT, Quit::new
  );

  private Command nextCommand() {
    String[] input;
    do {
      Write.inline(">> ");
      input = StringUtils.split(reader.get().trim().toLowerCase(), ' ');
    } while (input.length == 0);

    if (input[0].startsWith("+")) {
      Command command = new SetHit();

      command.setParams(input);
      command.setArena(arena);

      return command;
    }

    Command command = Command.Name.find(input[0])
      .map(commands::get)
      .orElse(ButWhatDoesThisMean::new)
      .get();

    command.setParams(input);
    command.setArena(arena);

    return command;
  }

  private final class Quit extends BaseCommand {
    @Override
    protected void performTask() {
      itsAllOverCalculator = true;
    }
  }

}
