package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

import static java.util.stream.Collectors.joining;

/**
 * Item or spell which carries multiple effects to be applied on hit.
 * This is equivalent to anything with effects that you can find in in-game menus and can be used against targets.
 * Examples include: weapons, arrows, spells, poisons.
 * Just like in-game, each effect must have a unique type.
 * <p/>
 * Carriers can be combined to create actual {@link Hit}s, like {@link Source#BOW} + {@link Source#ARROW}.
 * <p/>
 * The effects should be applied in iteration order.
 * <p/>
 * Carriers have a natural ordering that is consistent with their {@link Source}.
 * This ordering conveys how multiple carriers that belong to a single {@link Hit} should be applied.
 * {@link #equals(Object)} is NOT consistent with this ordering!
 * Avoid {@link SortedSet} and similar!
 * <p/>
 * This class can be overridden to provide different strategies for uniquely identifying effects.
 * In such cases, {@link #copy()} should also be overridden!
 */
public class Carrier implements Iterable<EffectText>, Comparable<Carrier> {

  public final Source getSource() {
    return source;
  }

  public final Method getMethod() {
    return method;
  }

  /**
   * When hitting a target multiple times with the same carrier (e.g. spell), effects do not stack, but override
   * each other instead (usually).
   * The id from this method allows us to identify effects which should be overridden rather than stacked.
   * <p/>
   * Result is undefined if the effect did not come from this carrier.
   *
   * @param effect effect which we must uniquely identify
   * @return id which uniquely identifies the given effect
   */
  public Effect.Id toId(EffectText effect) {
    return new UniquePerType(this, effect.getType());
  }

  /**
   * Creates a copy of this carrier. The effects of this carrier and the copy will likely stack!
   */
  public Carrier copy() {
    return new Carrier(source, method, effects);
  }

  @Override
  public final Iterator<EffectText> iterator() {
    return effects.iterator();
  }

  @Override
  public final int compareTo(Carrier other) {
    return ORDER.compare(this, other);
  }

  public Carrier(Source source,
                 Method method,
                 Iterable<EffectText> effects) {
    this.source = source;
    this.method = method;
    this.effects = ImmutableList.copyOf(effects);

    StructureException.throwOnDuplicateEffectTypes(this.effects);
  }

  private final Source source;
  private final Method method;
  private final List<EffectText> effects;

  @Override
  public String toString() {
    if (effects.isEmpty()) {
      return source + " {NO EFFECTS}";
    }

    return effects.stream()
      .map(EffectText::toString)
      .collect(joining(" + ", source + " {", "}"));
  }

  private static final Comparator<Carrier> ORDER = Comparator.comparing(Carrier::getSource);

  private static final class UniquePerType implements Effect.Id {
    public UniquePerType(Carrier carrier, Effect.Type effectType) {
      this.carrier = carrier;
      this.effectType = effectType;
    }

    private final Carrier carrier;
    private final Effect.Type effectType;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      UniquePerType other = (UniquePerType)o;
      return Objects.equals(carrier, other.carrier)
        && Objects.equals(effectType, other.effectType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(carrier, effectType);
    }
  }

}
