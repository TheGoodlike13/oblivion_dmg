package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.core.source.Magic;
import eu.goodlike.oblivion.core.source.Poison;

/**
 * Innate property of effects which can be resisted.
 * All possible factors are defined in this interface.
 * Factors can be {@link Method}s, {@link Element}s or both.
 */
public interface Factor {

  Poison POISON = Poison.getInstance();
  Magic MAGIC = Magic.INSTANCE;
  Element FIRE = new Element("FIRE");
  Element FROST = new Element("FROST");
  Element SHOCK = new Element("SHOCK");

  /**
   * @param pc percent of weakness
   * @return magical effect which applies a weakness to this factor
   */
  default EffectText weakness(int pc) {
    return resist(-pc);
  }

  /**
   * @param pc percent of resistance
   * @return magical effect which applies a resistance to this factor
   */
  EffectText resist(int pc);

}
