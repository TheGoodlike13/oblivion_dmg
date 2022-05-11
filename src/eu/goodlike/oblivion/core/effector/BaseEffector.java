package eu.goodlike.oblivion.core.effector;

import com.google.common.collect.ImmutableList;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;

import java.util.Iterator;
import java.util.List;

/**
 * The base class for all effectors.
 * Ensures that effects cannot be referenced accidentally.
 * Because iterator can be overridden, you should always iterate over the effector, even internally.
 */
public abstract class BaseEffector implements Effector {

  // most effectors can be simply iterated over, only SPELL is different
  @Override
  public Iterator<EffectText> iterator() {
    return effects.iterator();
  }

  protected BaseEffector(Iterable<EffectText> effects) {
    this.effects = ImmutableList.copyOf(effects);
  }

  private final List<EffectText> effects;

}
