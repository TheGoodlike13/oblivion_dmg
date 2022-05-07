package eu.goodlike.oblivion.core.method;

import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.effect.Drain;

public final class Magic extends Element implements Method {

  public EffectText drain(int hp) {
    return new EffectText(this, drain(), hp);
  }

  public Effect.Type drain() {
    return Drain.TYPE;
  }

  public Magic() {
    super("MAGIC");
  }

}
