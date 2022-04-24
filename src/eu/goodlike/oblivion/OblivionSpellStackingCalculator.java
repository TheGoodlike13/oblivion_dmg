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
    String input = read();
    if (input.startsWith("enemy")) {
      String hpStr = StringUtils.substringAfter(input, "enemy ");
      double d = Double.parseDouble(hpStr);
      enemy = new Enemy(d);
      write("Today you'll be hitting an enemy with " + d + " hp.");
    }
    else if (!"quit".equals(input)) {
      write("No idea what <" + input + "> is supposed to mean.");
    }
  }

  public void intro() {
    write("Welcome!");
    write("Please select a target, cast some spells or perform attacks and GO!");
    write("You can quit any time ;)");
  }

  public OblivionSpellStackingCalculator(Supplier<String> input, Consumer<String> output) {
    this.input = input;
    this.output = output;
  }

  @VisibleForTesting
  Enemy enemy;

  private final Supplier<String> input;
  private final Consumer<String> output;

  private String read() {
    output.accept(">> ");
    return input.get().toLowerCase();
  }

  private void write(String line) {
    output.accept(line + System.lineSeparator());
  }

}
