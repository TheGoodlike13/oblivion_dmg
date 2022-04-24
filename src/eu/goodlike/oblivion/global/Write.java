package eu.goodlike.oblivion.global;

import java.util.function.Consumer;

/**
 * System.out that we can mock out.
 */
public final class Write {

  public static Consumer<String> WRITER = System.out::print;

  public static void line(String line) {
    WRITER.accept(line + System.lineSeparator());
  }

  public static void inline(String s) {
    WRITER.accept(s);
  }

  public static void resetToFactory() {
    WRITER = System.out::print;
  }

  private Write() {
    throw new AssertionError("Do not instantiate! Use static fields/methods!");
  }

}
