package eu.goodlike.oblivion.core.effector;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.StructureException;

import java.util.Objects;

import static eu.goodlike.oblivion.core.Factor.MAGIC;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Common base class for effectors.
 * Defines reasonable defaults, but allows most of them to be overridden for special cases.
 */
public abstract class EffectorSkeleton extends BaseEffector {

  // most methods of delivery are MAGIC, only POISON is different
  public Method getMethod() {
    return MAGIC;
  }

  // most effectors do not allow stacking of effects, only POISON is different
  public Effect.Id toId(EffectText effect) {
    return new UniquePerPosition(this, getIndex(effect), effect.getType());
  }

  protected final int getIndex(EffectText effect) {
    int index = Iterables.indexOf(this, effect::equals);
    if (index < 0) {
      throw new StructureException("Effect " + effect + " cannot be identified as part of " + getName(), getEffects());
    }
    return index;
  }

  public final Effector copy() {
    return copy(label);
  }

  public final String getName() {
    return "<" + getCategory() + getRef() + ">";
  }

  protected EffectorSkeleton(String label, Iterable<EffectText> effects) {
    super(effects);
    this.label = label;
  }

  private final String label;

  @Override
  public final String toString() {
    return getName() + " {" + getEffects() + "}";
  }

  private String getRef() {
    return isBlank(label) ? "" : "$" + label;
  }

  private String getEffects() {
    String effects = Streams.stream(this)
      .map(EffectText::toString)
      .collect(joining(" + "));

    return isBlank(effects) ? "NO EFFECTS" : effects;
  }

  private static final class UniquePerPosition implements Effect.Id {
    @Override
    public Effect.Type getType() {
      return type;
    }

    public UniquePerPosition(Effector effector, int index, Effect.Type type) {
      this.effector = effector;
      this.index = index;
      this.type = type;
    }

    private final Effector effector;
    private final int index;
    private final Effect.Type type;

    @Override
    public String toString() {
      return effector.getName() + " " + getType();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      UniquePerPosition other = (UniquePerPosition)o;
      return index == other.index && Objects.equals(effector, other.effector);
    }

    @Override
    public int hashCode() {
      return Objects.hash(effector, index);
    }
  }

}
