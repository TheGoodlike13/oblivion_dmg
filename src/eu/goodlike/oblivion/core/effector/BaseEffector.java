package eu.goodlike.oblivion.core.effector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.StructureException;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static eu.goodlike.oblivion.core.Factor.MAGIC;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Common base class for effectors.
 * Defines reasonable defaults, but allows most of them to be overridden for special cases.
 */
public abstract class BaseEffector implements Effector {

  // most methods of delivery are MAGIC, only POISON is different
  public Method getMethod() {
    return MAGIC;
  }

  // most effectors do not allow stacking of effects, only POISON is different
  public Effect.Id toId(EffectText effect) {
    return new UniquePerType(this, effect.getType());
  }

  public final Effector copy() {
    return copy(label);
  }

  public final String getName() {
    return "<" + getCategory() + getRef() + ">";
  }

  // most effectors can be simply iterated over, only SPELL is different
  @Override
  public Iterator<EffectText> iterator() {
    return effects.iterator();
  }

  protected BaseEffector(String label, Iterable<EffectText> effects) {
    this.label = label;
    this.effects = ImmutableList.copyOf(effects);

    StructureException.throwOnDuplicateEffectTypes(this.effects);
  }

  private final String label;
  private final List<EffectText> effects;

  @Override
  public final String toString() {
    return getName() + " {" + getEffects() + "}";
  }

  private String getRef() {
    return isBlank(label) ? "" : "$" + label;
  }

  private String getEffects() {
    if (effects.isEmpty()) {
      return "NO EFFECTS";
    }

    return Streams.stream(this)
      .map(EffectText::toString)
      .collect(joining(" + "));
  }

  private static final class UniquePerType implements Effect.Id {
    @Override
    public Effect.Type getType() {
      return type;
    }

    public UniquePerType(BaseEffector effector, Effect.Type type) {
      this.effector = effector;
      this.type = type;
    }

    private final BaseEffector effector;
    private final Effect.Type type;

    @Override
    public String toString() {
      return effector.getName() + " " + getType();
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
