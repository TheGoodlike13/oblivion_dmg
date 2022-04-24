package eu.goodlike.oblivion;

import com.google.common.annotations.VisibleForTesting;
import eu.goodlike.oblivion.core.Enemy;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class OblivionSpellStackingCalculator {

  public static void main(String... args) {
    try (Scanner scanner = new Scanner(System.in)) {
      OblivionSpellStackingCalculator c = new OblivionSpellStackingCalculator(scanner::nextLine, System.out::print);
      c.intro();
      c.run();
    }
  }

  public void run() {
    read();
    String command = lastInput(0);

    if ("enemy".startsWith(command)) {
      String hpStr = lastInput(1);
      try {
        double d = Double.parseDouble(hpStr);
        enemy = new Enemy(d);
        write("Today you'll be hitting an enemy with " + d + " hp.");
      }
      catch (NumberFormatException e) {
        write("Bad input: cannot parse enemy hp <" + hpStr + ">");
      }
    }
    else if (!"quit".startsWith(command)) {
      write("No idea what <" + lastInput() + "> is supposed to mean.");
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

  private String[] lastInput;

  private final Supplier<String> input;
  private final Consumer<String> output;

  private String lastInput() {
    return String.join(" ", lastInput);
  }

  private String lastInput(int index) {
    return index >= lastInput.length ? "" : lastInput[index];
  }

  private void read() {
    do {
      output.accept(">> ");
      lastInput = StringUtils.split(input.get().trim().toLowerCase(), ' ');
    } while (lastInput.length == 0);
  }

  private void write(String line) {
    output.accept(line + System.lineSeparator());
  }

}
