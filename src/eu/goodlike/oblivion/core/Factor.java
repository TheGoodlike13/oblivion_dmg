package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.core.method.Magic;
import eu.goodlike.oblivion.core.method.Poison;

public interface Factor {

  Method POISON = new Poison();
  Magic MAGIC = new Magic();
  Element FIRE = new Element();
  Element FROST = new Element();
  Element LIGHTNING = new Element();

  default EffectText weakness(int magnitude) {
    return resist(-magnitude);
  }

  EffectText resist(int magnitude);

}
