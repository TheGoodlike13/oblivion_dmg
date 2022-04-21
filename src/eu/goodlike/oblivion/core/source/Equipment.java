package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Source;

import java.util.List;

import static eu.goodlike.oblivion.core.Factor.MAGIC;

public final class Equipment implements Source {

  @Override
  public Carrier create(String name, List<EffectText> effects) {
    return new Carrier(this, name, MAGIC, Magic.HitId::new, effects);
  }

  public Equipment(String sourceName) {
    this.sourceName = sourceName;
  }

  private final String sourceName;

  @Override
  public String toString() {
    return sourceName;
  }

}
