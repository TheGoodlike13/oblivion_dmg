package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableSet;

import java.util.List;
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

  public static void throwOnDuplicateEffectTypes(List<EffectText> effects) {
    long uniqueTypeCount = effects.stream()
      .map(EffectText::getType)
      .distinct()
      .count();

    if (uniqueTypeCount < effects.size()) {
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

  public static int intOrThrow(String input, String description) {
    try {
      return Integer.parseInt(input);
    }
    catch (NumberFormatException e) {
      throw new StructureException("Cannot parse " + description, input, e);
    }
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
    "STAFF",
    "MELEE",
    "MELEE + POISON",
    "BOW + ARROW",
    "BOW + ARROW + POISON"
  );

}
