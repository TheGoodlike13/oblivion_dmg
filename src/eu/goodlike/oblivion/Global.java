package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.StructureException;
import eu.goodlike.oblivion.parse.ParseCarrier;
import eu.goodlike.oblivion.parse.ParseEnemy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Various objects that are accessible globally to simplify APIs.
 */
public final class Global {

  /**
   * Initializes all global values.
   * Loads all configuration files.
   * Repeat calls to this method will act like a reset, unless file contents have changed.
   */
  public static void initializeEverything() {
    Settings.load();
    WRITER = System.out::print;
    THE_ARENA.reset();
    ENEMIES.reset(Settings.PREPARED_ENEMIES);
    CARRIERS.reset(Settings.PREPARED_ITEMS, Settings.PREPARED_SPELLS);
    HITS.reset();
    ITS_ALL_OVER = false;
  }

  /**
   * System.out that we can mock out.
   */
  public static Consumer<String> WRITER = System.out::print;

  /**
   * The place where the magic happens. Literally.
   */
  public static final Arena THE_ARENA = new Arena();

  /**
   * Caches which holds prepared entities as well as references created as part of parsing user input.
   */
  public static final InputCache<Enemy> ENEMIES = new InputCache<>(ParseEnemy::new);
  public static final InputCache<Carrier> CARRIERS = new InputCache<>(ParseCarrier::new);
  public static final InputCache<Hit> HITS = new InputCache<>();

  /**
   * Flag which determines if the application should exit or not.
   */
  public static boolean ITS_ALL_OVER = false;

  /**
   * Configurable values for the application.
   * Default values provided for reference, but they are immediately overwritten by {@link #load}.
   * See settings.properties for actual values.
   */
  public static final class Settings {
    static {
      load();
    }

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
    public static double TICK = 0.001;

    /**
     * Config files containing prepared entities which can be parsed and stored into caches for future use.
     */
    public static String PREPARED_ENEMIES = "prepared_enemies.txt";
    public static String PREPARED_ITEMS = "prepared_items.txt";
    public static String PREPARED_SPELLS = "prepared_spells.txt";

    /**
     * Loads the settings from the settings.properties file.
     * Repeated calls will act like a reset, unless the file has changed.
     */
    public static void load() {
      InputStream settings = Settings.class.getClassLoader().getResourceAsStream(SETTINGS_FILE);
      if (settings == null) {
        throw new IllegalStateException("Cannot find '" + SETTINGS_FILE + "' on the classpath.");
      }

      Properties properties = new Properties();
      try {
        properties.load(settings);
      }
      catch (IOException e) {
        throw new IllegalStateException("File '" + SETTINGS_FILE + "' could not be read as properties!");
      }

      DIFFICULTY = StructureException.intOrThrow(properties.getProperty("difficulty"), "difficulty setting");
      TICK = StructureException.doubleOrThrow(properties.getProperty("tick"), "tick setting");
      PREPARED_ENEMIES = properties.getProperty("prepared.enemies");
      PREPARED_ITEMS = properties.getProperty("prepared.items");
      PREPARED_SPELLS = properties.getProperty("prepared.spells");
    }

    private Settings() {
      throw new AssertionError("Do not instantiate! Use static fields/methods!");
    }

    private static final String SETTINGS_FILE = "settings.properties";
  }

  private Global() {
    throw new AssertionError("Do not instantiate! Use static fields/methods!");
  }

}
