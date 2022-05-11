package eu.goodlike.oblivion.core.effector;

import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;

import static eu.goodlike.oblivion.core.Effector.Factory.ARROW;

public final class Arrow extends EffectorSkeleton {

  @Override
  public Category<?> getCategory() {
    return ARROW;
  }

  @Override
  public Effector copy(String label) {
    return new Arrow(label, this);
  }

  public Arrow(String label, Iterable<EffectText> effects) {
    super(label, effects);
  }

}
