package eu.goodlike.oblivion.core;

import com.google.common.collect.Streams;

import java.util.List;
import java.util.function.Predicate;

/**
 * Effector factory for one specific type of effectors.
 * There are a limited amount of such types and they can all be found under {@link Effector.Factory}.
 * <p/>
 * The categories have a natural ordering which defines how they should be processed as part of a single hit.
 */
public class Category<E extends Effector> implements Effector.Factory<E>, Comparable<Category>, Predicate<Effector> {

  /**
   * @return equivalent to {@link #create} with no params, but always returns the same instance
   */
  public E withNoEffect() {
    return noEffect;
  }

  @Override
  public E create(String label, List<EffectText> effects) {
    return factory.create(label, effects);
  }

  @Override
  public int compareTo(Category other) {
    return ALL_IN_ORDER.indexOf(this) - ALL_IN_ORDER.indexOf(other);
  }

  /**
   * @return true if the effector matches this category, false if it's of a different type
   */
  @Override
  public boolean test(Effector effector) {
    return noEffect.getClass().isInstance(effector);
  }

  /**
   * @return true if any of the given effectors are of this category, false otherwise
   */
  public boolean any(Iterable<? extends Effector> effectors) {
    return Streams.stream(effectors).anyMatch(this);
  }

  public Category(String label, Effector.Factory<E> factory) {
    this.label = label;
    this.factory = factory;
    this.noEffect = create();
  }

  private final String label;
  private final Effector.Factory<E> factory;
  private final E noEffect;

  @Override
  public String toString() {
    return label;
  }

}
