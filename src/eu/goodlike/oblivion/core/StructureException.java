package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

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

  public StructureException(String message) {
    super(message);
  }

  private static final Set<String> VALID_HITS = ImmutableSet.of(
    "MAGIC",
    "STAFF",
    "MELEE",
    "MELEE + POISON",
    "BOW + ARROW",
    "BOW + ARROW + POISON"
  );

}
