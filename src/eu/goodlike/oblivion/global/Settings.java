package eu.goodlike.oblivion.global;

public final class Settings {

  public static double TICK = 0.01;
  public static double DIFFICULTY = 50;

  public static void resetToFactory() {
    TICK = 0.01;
    DIFFICULTY = 50;
  }

  private Settings() {
    throw new AssertionError("Do not instantiate! Use static fields/methods!");
  }

}
