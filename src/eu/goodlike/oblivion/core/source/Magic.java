package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.effect.Drain;

import java.util.List;

public final class Magic extends Element implements Method, Source {

  public EffectText drain(double hp) {
    return new EffectText(this, Drain.TYPE, hp);
  }

  @Override
  public Carrier create(List<EffectText> effects) {
    return new Carrier(this, MAGIC, effects);
  }

  @Override
  public String toString() {
    return "SPELL";
  }

  public static final Magic INSTANCE = new Magic();

}
