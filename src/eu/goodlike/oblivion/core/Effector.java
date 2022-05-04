package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Item or spell which carries multiple effects to be applied on hit.
 * This is equivalent to anything with effects that you can find in in-game menus and can be used against targets.
 * Examples include: weapons, arrows, spells, poisons.
 * Just like in-game, each effect must have a unique type.
 * <p/>
 * Effectors can be combined to create actual {@link Hit}s, like {@link Source#BOW} + {@link Source#ARROW}.
 * <p/>
 * The effects should be applied in iteration order.
 * <p/>
 * Effectors have a natural ordering that is consistent with their {@link Source}.
 * This ordering conveys how multiple effectors that belong to a single {@link Hit} should be applied.
 * {@link #equals)} is NOT consistent with this ordering!
 * Avoid {@link SortedSet} and similar!
 * <p/>
 * Two effectors should only be equal if they represent the exactly same item or spell.
 * Usually object identity is sufficient to achieve this.
 * <p/>
 * This class can be overridden to provide different strategies for uniquely identifying effects.
 * In such cases, {@link #copy(String)} should also be overridden!
 */
public class Effector implements Iterable<EffectText>, Comparable<Effector> {

  public final Source getSource() {
    return source;
  }

  public final Method getMethod() {
    return method;
  }

  /**
   * When hitting a target multiple times with the same effector (e.g. spell), effects do not stack, but override
   * each other instead (usually).
   * The id from this method allows us to identify effects which should be overridden rather than stacked.
   * <p/>
   * Result is undefined if the effect did not come from this effector.
   *
   * @param effect effect which we must uniquely identify
   * @return id which uniquely identifies the given effect
   */
  public Effect.Id toId(EffectText effect) {
    return new UniquePerType(this, effect.getType());
  }

  public final Effector copy() {
    return copy(label);
  }

  /**
   * Creates a copy of this effector with a different label. The effects of this effector and the copy will stack!
   */
  public Effector copy(String label) {
    return new Effector(label, source, method, effects);
  }

  public String getLabel() {
    return "<" + source + getRef() + ">";
  }

  @Override
  public final Iterator<EffectText> iterator() {
    return effects.iterator();
  }

  @Override
  public final int compareTo(Effector other) {
    return ORDER.compare(this, other);
  }

  public Effector(String label,
                  Source source,
                  Method method,
                  Iterable<EffectText> effects) {
    this.label = label;
    this.source = source;
    this.method = method;
    this.effects = ImmutableList.copyOf(effects);

    StructureException.throwOnDuplicateEffectTypes(this.effects);
  }

  private final String label;
  private final Source source;
  private final Method method;
  private final List<EffectText> effects;

  @Override
  public String toString() {
    return getLabel() + " " + getEffects();
  }

  private String getRef() {
    return isBlank(label) ? "" : "$" + label;
  }

  private String getEffects() {
    if (effects.isEmpty()) {
      return "{NO EFFECTS}";
    }

    return effects.stream()
      .map(EffectText::toString)
      .collect(joining(" + ", "{", "}"));
  }

  private static final Comparator<Effector> ORDER = Comparator.comparing(Effector::getSource);

  private static final class UniquePerType implements Effect.Id {
    @Override
    public Effect.Type getType() {
      return type;
    }

    public UniquePerType(Effector effector, Effect.Type type) {
      this.effector = effector;
      this.type = type;
    }

    private final Effector effector;
    private final Effect.Type type;

    @Override
    public String toString() {
      return effector.getLabel() + " " + getType();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      UniquePerType other = (UniquePerType)o;
      return Objects.equals(effector, other.effector)
        && Objects.equals(type, other.type);
    }

    @Override
    public int hashCode() {
      return Objects.hash(effector, type);
    }
  }

}
