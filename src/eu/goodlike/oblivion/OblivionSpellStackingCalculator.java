package eu.goodlike.oblivion;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.StructureException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static eu.goodlike.oblivion.Command.Name.ENEMY;
import static eu.goodlike.oblivion.Command.Name.QUIT;

public final class OblivionSpellStackingCalculator {

  public static void main(String... args) {
    try (Scanner scanner = new Scanner(System.in)) {
      OblivionSpellStackingCalculator c = new OblivionSpellStackingCalculator(scanner::nextLine, System.out::print);
      c.intro();
      c.run();
    }
  }

  public void run() {
    while (!itsAllOverCalculator) {
      read();
      lastCommand.execute();
    }
  }

  public void intro() {
    write("Welcome to Oblivion spell stacking calculator!");
    write("Please select a target, cast some spells or perform attacks and GO!");
    write("You can quit any time ;)");
  }

  public OblivionSpellStackingCalculator(Supplier<String> input, Consumer<String> output) {
    this.input = input;
    this.output = output;
  }

  @VisibleForTesting
  Enemy enemy;

  private Command lastCommand;
  private boolean itsAllOverCalculator = false;

  private final Supplier<String> input;
  private final Consumer<String> output;

  private final Map<Command.Name, Supplier<Command>> commands = ImmutableMap.of(
    ENEMY, NewEnemy::new,
    QUIT, Quit::new
  );

  private void read() {
    String[] input;
    do {
      output.accept(">> ");
      input = StringUtils.split(this.input.get().trim().toLowerCase(), ' ');
    } while (input.length == 0);

    String command = input[0];
    this.lastCommand = Command.Name.find(command)
      .map(commands::get)
      .orElse(ButWhatDoesThisMean::new)
      .get();

    lastCommand.setParams(input);
  }

  private void write(String line) {
    output.accept(line + System.lineSeparator());
  }

  private final class NewEnemy extends BaseCommand {
    @Override
    protected void performTask() {
      double hp = parseHp();
      enemy = new Enemy(hp);
      write("Today you'll be hitting an enemy with " + hp + " hp.");
    }

    private double parseHp() {
      String hp = input(1);
      try {
        return Double.parseDouble(hp);
      }
      catch (NumberFormatException e) {
        throw new StructureException("Cannot parse enemy hp <" + hp + ">", e);
      }
    }
  }

  private final class Quit extends BaseCommand {
    @Override
    protected void performTask() {
      itsAllOverCalculator = true;
    }
  }

  private final class ButWhatDoesThisMean extends BaseCommand {
    @Override
    protected void performTask() {
      write("No idea what <" + input() + "> is supposed to mean.");
    }
  }

  private abstract class BaseCommand extends Command {
    @Override
    protected void write(String line) {
      OblivionSpellStackingCalculator.this.write(line);
    }
  }

}
