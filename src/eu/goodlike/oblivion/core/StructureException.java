package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

import java.util.Set;

/**
 * Exception used to validate the structure of objects of the application.
 */
public final class StructureException extends RuntimeException {

  public static void throwOnInvalidHit(String hitTrace) {
    if (!VALID_HITS.contains(hitTrace)) {
      throw new StructureException("Invalid hit: " + hitTrace + "; expected one of " + VALID_HITS);
    }
  }

  public static void throwOnDuplicateEffectTypes(Iterable<EffectText> effects) {
    long uniqueTypeCount = Streams.stream(effects)
      .map(EffectText::getType)
      .distinct()
      .count();

    if (uniqueTypeCount < Iterables.size(effects)) {
      throw new StructureException("Effect types must be unique! Instead: " + effects);
    }
  }

  public static void throwOnNegativeDamage(double dmg) {
    if (dmg < 0) {
      throw new StructureException("Heals are not allowed!");
    }
  }

  public static void throwOnAlreadyDead(Enemy enemy) {
    if (!enemy.isAlive()) {
      throw new StructureException("Enemy cannot start off dead!");
    }
  }

  public static double doubleOrThrow(String input, String description) {
    try {
      return Double.parseDouble(input);
    }
    catch (NumberFormatException e) {
      throw new StructureException("Cannot parse " + description, input, e);
    }
  }

  public static double positiveOrThrow(String input, String description) {
    double d = doubleOrThrow(input, description);
    return positiveOrThrow(d, description);
  }

  public static double positiveOrZeroOrThrow(String input, String description) {
    double d = doubleOrThrow(input, description);
    return positiveOrZeroOrThrow(d, description);
  }

  public static double positiveOrThrow(double d, String description) {
    if (d <= 0) {
      throw new StructureException("Expected positive " + description + ", but was <" + d + ">");
    }
    return d;
  }

  public static double positiveOrZeroOrThrow(double d, String description) {
    if (d < 0) {
      throw new StructureException("Expected positive or zero " + description + ", but was <" + d + ">");
    }
    return d;
  }

  public static int intOrThrow(String input, String description) {
    try {
      return Integer.parseInt(input);
    }
    catch (NumberFormatException e) {
      throw new StructureException("Cannot parse " + description, input, e);
    }
  }

  public static int natOrThrow(String input, String description) {
    int i = intOrThrow(input, description);
    return natOrThrow(i, description);
  }

  public static int natOrZeroOrThrow(String input, String description) {
    int d = intOrThrow(input, description);
    return natOrZeroOrThrow(d, description);
  }

  public static int natOrThrow(int i, String description) {
    if (i <= 0) {
      throw new StructureException("Expected positive " + description + ", but was <" + i + ">");
    }
    return i;
  }

  public static int natOrZeroOrThrow(int i, String description) {
    if (i < 0) {
      throw new StructureException("Expected positive or zero " + description + ", but was <" + i + ">");
    }
    return i;
  }

  public StructureException(String message) {
    super(message);
  }

  public StructureException(String message, Throwable cause) {
    super(message, cause);
  }

  public StructureException(String parsingProblem, Object input) {
    this(parsingProblem + " <" + input + ">", null);
  }

  public StructureException(String parsingProblem, Object input, Throwable cause) {
    this(parsingProblem + " <" + input + ">", cause);
  }

  private static final Set<String> VALID_HITS = ImmutableSet.of(
    "SPELL",
    "POWER",
    "STAFF",
    "MELEE",
    "MELEE + POISON",
    "ARROW + BOW",
    "ARROW + POISON + BOW"
  );

}
