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

  public StructureException(String message) {
    super(message);
  }

  public StructureException(String parsingProblem, String input) {
    super(parsingProblem + " <" + input + ">");
  }

  public StructureException(String message, Throwable cause) {
    super(message, cause);
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
