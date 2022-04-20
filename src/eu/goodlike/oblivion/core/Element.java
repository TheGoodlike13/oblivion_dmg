package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.core.effect.Damage;
import eu.goodlike.oblivion.core.effect.Resist;

/**
 * Describes types of damage.
 * There are 4 relevant element types: {@link #MAGIC}, {@link #FIRE}, {@link #FROST} and {@link #SHOCK}.
 */
public class Element implements Factor {

  @Override
  public EffectText resist(int magnitude) {
    return new EffectText(MAGIC, resistElement, magnitude);
  }

  /**
   * @return effect which does given magnitude as amount of damage of this element per second
   */
  public EffectText damage(int magnitude) {
    return new EffectText(this, Damage.TYPE, magnitude);
  }

  private final Resist.Type resistElement = new Resist.OfFactor(this);

}
