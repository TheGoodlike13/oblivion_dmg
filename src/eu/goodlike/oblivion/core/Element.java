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
   * @throws StructureException if damage is negative
   */
  public EffectText damage(int dmg) {
    StructureException.throwOnNegativeDamage(dmg);
    return new EffectText(this, damageElement, dmg);
  }

  public Element(String elementName) {
    this.elementName = elementName;
    this.resistElement = new Resist.OfFactor(this);
    this.damageElement = new Damage.OfElement(this);
  }

  private final String elementName;
  private final Resist.Type resistElement;
  private final Damage.Type damageElement;

  @Override
  public String toString() {
    return elementName;
  }

}
