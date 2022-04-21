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
  Magic MAGIC = Magic.getInstance();
  Element FIRE = new Element();
  Element FROST = new Element();
  Element SHOCK = new Element();

  /**
   * @return magical effect which applies a weakness to this factor with given magnitude
   */
  default EffectText weakness(int magnitude) {
    return resist(-magnitude);
  }

  /**
   * @return magical effect which applies a resistance to this factor with given magnitude
   */
  EffectText resist(int magnitude);

}
