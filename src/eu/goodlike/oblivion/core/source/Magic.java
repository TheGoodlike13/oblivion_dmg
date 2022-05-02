package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.effect.Drain;

import java.util.List;

import static eu.goodlike.oblivion.Global.Settings.CAST;

public final class Magic extends Element implements Method, Source {

  public EffectText drain(int hp) {
    return new EffectText(this, drain(), hp);
  }

  public Effect.Type drain() {
    return Drain.TYPE;
  }

  @Override
  public Carrier withNoEffect() {
    return noEffect;
  }

  @Override
  public Carrier create(String label, List<EffectText> effects) {
    return new Carrier(label, this, MAGIC, effects);
  }

  @Override
  public double timeToHit(int combo) {
    return CAST.timeToHit(combo);
  }

  @Override
  public double cooldown(int combo) {
    return CAST.cooldown(combo);
  }

  public Magic(String name) {
    super(name);
  }

  private final Carrier noEffect = create();

  @Override
  public String describeAction() {
    return "cast";
  }

}
