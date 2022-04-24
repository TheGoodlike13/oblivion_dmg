package eu.goodlike.oblivion;

import com.google.common.collect.ImmutableMap;
import eu.goodlike.oblivion.command.BaseCommand;
import eu.goodlike.oblivion.command.ButWhatDoesThisMean;
import eu.goodlike.oblivion.command.SetEnemy;
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
      nextCommand().execute();
    }
  }

  public void intro() {
    write("Welcome to Oblivion spell stacking calculator!");
    write("Please select an enemy, cast some spells or perform attacks and GO!");
    write("You can quit any time ;)");
  }

  public OblivionSpellStackingCalculator(Supplier<String> reader, Consumer<String> writer) {
    this.reader = reader;
    this.writer = writer;
  }

  private boolean itsAllOverCalculator = false;

  private final Supplier<String> reader;
  private final Consumer<String> writer;

  private final Arena arena = new Arena();

  private final Map<Command.Name, Supplier<BaseCommand>> commands = ImmutableMap.of(
    ENEMY, SetEnemy::new,
    QUIT, Quit::new
  );

  private BaseCommand nextCommand() {
    String[] input;
    do {
      writer.accept(">> ");
      input = StringUtils.split(reader.get().trim().toLowerCase(), ' ');
    } while (input.length == 0);

    BaseCommand command = Command.Name.find(input[0])
      .map(commands::get)
      .orElse(ButWhatDoesThisMean::new)
      .get();

    command.setParams(input);
    command.setArena(arena);
    command.setWriter(this::write);

    return command;
  }

  private void write(String line) {
    writer.accept(line + System.lineSeparator());
  }

  private final class Quit extends BaseCommand {
    @Override
    protected void performTask() {
      itsAllOverCalculator = true;
    }
  }

}
