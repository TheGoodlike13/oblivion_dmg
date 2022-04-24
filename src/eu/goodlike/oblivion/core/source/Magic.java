package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.effect.Drain;

import java.util.List;

public final class Magic extends Element implements Method, Source {

  public EffectText drain(int hp) {
    return new EffectText(this, Drain.TYPE, hp);
  }

  @Override
  public Carrier create(List<EffectText> effects) {
    return new Carrier(this, MAGIC, effects);
  }

  public Magic() {
    super("MAGIC");
  }

  /**
   * Similar to {@link Poison#getInstance()}, this is necessary to prevent NULL references.
   */
  public static final Magic INSTANCE = new Magic();

}
