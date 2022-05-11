package eu.goodlike.oblivion;

import com.google.common.collect.ImmutableList;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.HitPattern;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.StructureException;
import eu.goodlike.oblivion.parse.ParseEffector;
import eu.goodlike.oblivion.parse.ParseEnemy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.split;

/**
 * Various objects that are accessible globally to simplify APIs.
 */
public final class Global {

  /**
   * Initializes all global values.
   * Loads all configuration files.
   * Repeat calls to this method will act like a reset, unless file contents have changed.
   * <p/>
   * Note: files must change in the classpath.
   * Full restart is required to update config files.
   */
  public static void initializeEverything() {
    Settings.load();
    WRITER = System.out::print;
    THE_ARENA.reset();
    reloadCaches();
    ITS_ALL_OVER = false;
  }

  /**
   * Clears the caches and reloads their configuration from files.
   * Repeat calls to this method will act like a cache reset, unless file contents have changed.
   * <p/>
   * Note: files must change in the classpath.
   * Full restart is required to update config files.
   */
  public static void reloadCaches() {
    ENEMIES.reset(Settings.PREPARED_ENEMIES);
    EFFECTORS.reset(Settings.PREPARED_ITEMS, Settings.PREPARED_SPELLS);
    HITS.reset();
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
  public static final InputCache<Effector> EFFECTORS = new InputCache<>(ParseEffector::forFile);
  public static final InputCache<Hit> HITS = new InputCache<>();
  public static final List<InputCache<?>> CACHES = ImmutableList.of(ENEMIES, EFFECTORS, HITS);

  /**
   * Flag which determines if the application should exit or not.
   */
  public static boolean ITS_ALL_OVER = false;

  /**
   * Configurable values for the application.
   * Default values provided for reference, but they are immediately overwritten by {@link #load}.
   * See settings.properties for actual values.
   * Commands can allow modifying some on them on the fly.
   */
  public static final class Settings {
    static {
      load();
    }

    /**
     * Player level.
     * Modifies hit points for leveled enemies.
     * Must be a positive integer.
     */
    public static int LEVEL = 30;

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
     * Multiplier for spell magnitude.
     * Must be a positive integer.
     * Unlike other multipliers, this one adjusts the magnitude at {@link EffectText} level.
     * This means the magnitude must remain an integer, and will be rounded down.
     * <p/>
     * Unlike in-game, spell effect descriptions will be updated and marked as modified by this
     * effectiveness (unless it is exactly 100).
     */
    public static int EFFECTIVENESS = 100;

    /**
     * The leniency of parsing for effectors.
     * <p/>
     * See {@link ParseEffector.Mode} for details.
     */
    public static ParseEffector.Mode PARSE_MODE = ParseEffector.Mode.MIXED;

    /**
     * Tick rate in seconds.
     * Must be a positive double.
     */
    public static double TICK = 0.001;

    /**
     * How long to continue attacking after enemy has died in seconds.
     * Must be a positive double or zero.
     */
    public static double RAMPAGE = 1;

    /**
     * Config files containing prepared entities which can be parsed and stored into caches for future use.
     */
    public static String PREPARED_ENEMIES = "prepared_enemies.txt";
    public static String PREPARED_ITEMS = "prepared_items.txt";
    public static String PREPARED_SPELLS = "prepared_spells.txt";

    /**
     * Time it takes to swap to a particular type of weapon in seconds.
     * Must be a positive double.
     */
    public static double SWAP_MELEE = 0.7;
    public static double SWAP_BOW = 1.06;
    public static double SWAP_STAFF = 1.17;

    /**
     * Patterns for various types of attacks.
     * The values for both time to hit and cooldown must be positive doubles.
     * There is no limit for number of combos, but they must have both time to hit and a cooldown value.
     */
    public static HitPattern STRIKE = new Hit.Combo(0.4, 0.58).combo(0.28, 0.4);
    public static HitPattern SHOOT = new Hit.Combo(1.58, 0.66);
    public static HitPattern EMIT = new Hit.Combo(0.53, 0.63);
    public static HitPattern CAST = new Hit.Combo(0.41, 0.73);

    /**
     * Loads the settings from the settings.properties file.
     * Repeated calls will act like a reset, unless the file has changed.
     */
    public static void load() {
      Properties properties = new Properties();

      try (InputStream settings = Settings.class.getClassLoader().getResourceAsStream(SETTINGS_FILE)) {
        if (settings == null) {
          throw new IllegalStateException("Cannot find '" + SETTINGS_FILE + "' on the classpath.");
        }
        properties.load(settings);
      }
      catch (IOException e) {
        throw new IllegalStateException("File '" + SETTINGS_FILE + "' could not be read as properties!");
      }

      LEVEL = StructureException.natOrThrow(properties.getProperty("level"), "player level");
      DIFFICULTY = StructureException.doubleOrThrow(properties.getProperty("difficulty"), "difficulty setting");
      EFFECTIVENESS = StructureException.natOrThrow(properties.getProperty("effectiveness"), "spell effectiveness");
      PARSE_MODE = Parse.mode(properties.getProperty("parse.mode"));

      TICK = StructureException.positiveOrThrow(properties.getProperty("tick"), "tick setting");
      RAMPAGE = StructureException.positiveOrZeroOrThrow(properties.getProperty("rampage"), "rampage setting");

      PREPARED_ENEMIES = properties.getProperty("prepared.enemies");
      PREPARED_ITEMS = properties.getProperty("prepared.items");
      PREPARED_SPELLS = properties.getProperty("prepared.spells");

      SWAP_MELEE = StructureException.positiveOrThrow(properties.getProperty("melee.swap"), "melee swap time");
      SWAP_BOW = StructureException.positiveOrThrow(properties.getProperty("bow.swap"), "bow swap time");
      SWAP_STAFF = StructureException.positiveOrThrow(properties.getProperty("staff.swap"), "staff swap time");

      STRIKE = loadCombo(properties, "melee.combo");
      SHOOT = loadCombo(properties, "bow.combo");
      EMIT = loadCombo(properties, "staff.combo");
      CAST = loadCombo(properties, "spell.combo");
    }

    private static HitPattern loadCombo(Properties properties, String comboProperty) {
      String combos = properties.getProperty(comboProperty);

      HitPattern.Builder builder = Hit.Combo.builder();
      for (String combo : split(combos, ";")) {
        String[] durations = split(combo.trim(), "_");
        if (durations.length != 2) {
          throw new StructureException("Invalid " + comboProperty + " config", combos);
        }
        double timeToHit = StructureException.doubleOrThrow(durations[0], comboProperty + " time to hit");
        double cooldown = StructureException.doubleOrThrow(durations[1], comboProperty + " cooldown");
        builder.combo(timeToHit, cooldown);
      }
      return builder.build();
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
