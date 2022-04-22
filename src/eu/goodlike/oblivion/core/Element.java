package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.core.effect.Damage;
import eu.goodlike.oblivion.core.effect.Resist;

/**
 * Describes types of damage.
 * There are 4 relevant element types: {@link #MAGIC}, {@link #FIRE}, {@link #FROST} and {@link #SHOCK}.
 */
public class Element implements Factor {

  @Override
  public EffectText resist(int pc) {
    return new EffectText(MAGIC, resistElement, pc);
  }

  /**
   * @return effect which does given damage of this element per second
   */
  public EffectText damage(int dmg) {
    return new EffectText(this, Damage.TYPE, dmg);
  }

  private final Resist.Type resistElement = new Resist.OfFactor(this);

}
