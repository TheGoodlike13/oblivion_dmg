package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.core.effect.Damage;
import eu.goodlike.oblivion.core.effect.Resist;

/**
 * {@link Factor}s which have damage types associated with them.
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
