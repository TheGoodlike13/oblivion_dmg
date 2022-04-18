package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.core.effect.Damage;
import eu.goodlike.oblivion.core.effect.Resist;

public class Element implements Factor {

  @Override
  public EffectText resist(int magnitude) {
    return new EffectText(MAGIC, resistElement, magnitude);
  }

  public EffectText damage(int magnitude) {
    return new EffectText(this, Damage.TYPE, magnitude);
  }

  private final Resist.Type resistElement = new Resist.OfFactor(this);

}
