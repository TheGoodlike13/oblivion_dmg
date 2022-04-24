package eu.goodlike.oblivion;

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
    write("You entered: " + read());
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
