package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Source;

import java.util.List;

import static eu.goodlike.oblivion.core.Factor.MAGIC;

public final class Equipment implements Source {

  @Override
  public Carrier withNoEffect() {
    return noEffect;
  }

  @Override
  public Carrier create(String label, List<EffectText> effects) {
    return new Carrier(label, this, MAGIC, effects);
  }

  public Equipment(String type) {
    this.type = type;
  }

  private final String type;

  private final Carrier noEffect = create();

  @Override
  public String toString() {
    return type;
  }

}
