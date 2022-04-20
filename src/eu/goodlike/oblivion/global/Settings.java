package eu.goodlike.oblivion.global;

import eu.goodlike.oblivion.core.Method;

/**
 * Configurable values for the application.
 * Exposed globally to simplify certain APIs.
 */
public final class Settings {

  /**
   * Value of the difficulty slider in Oblivion in-game options.
   * Multiplies the damage a player can do (and take).
   * <p/>
   * To stay true to the game, values from 0 to 100 should be used.
   * Defaults to 50, which implies normal difficulty.
   * Lower values increase the damage multiplier, up to 6x at 0.
   * Higher values reduce the damage multiplier, down to (1/6)x at 100.
   * See {@link Method#damageMultiplier()} for formula.
   */
  public static double DIFFICULTY = 50;

  /**
   * Tick rate in seconds.
   */
  public static double TICK = 0.01;

  /**
   * Resets the settings to their default values.
   * Useful for testing.
   */
  public static void resetToFactory() {
    DIFFICULTY = 50;
    TICK = 0.01;
  }

  private Settings() {
    throw new AssertionError("Do not instantiate! Use static fields/methods!");
  }

}
