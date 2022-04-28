package eu.goodlike.oblivion;

import static eu.goodlike.oblivion.Global.WRITER;

public final class Write {

  public static void line(String line) {
    WRITER.accept(line + System.lineSeparator());
  }

  public static void inline(String s) {
    WRITER.accept(s);
  }

  public static void separator() {
    line("-----");
  }

  private Write() {
    throw new AssertionError("Do not instantiate! Use static fields/methods!");
  }

}
