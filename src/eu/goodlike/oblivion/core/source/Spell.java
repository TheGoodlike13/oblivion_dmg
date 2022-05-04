package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.Source;

import java.util.List;

import static eu.goodlike.oblivion.Global.Settings.CAST;
import static eu.goodlike.oblivion.core.Factor.MAGIC;

public final class Spell implements Source {

  @Override
  public Effector withNoEffect() {
    return noEffect;
  }

  @Override
  public Effector create(String label, List<EffectText> effects) {
    return new Effector(label, this, MAGIC, effects);
  }

  @Override
  public double timeToHit(int combo) {
    return CAST.timeToHit(combo);
  }

  @Override
  public double cooldown(int combo) {
    return CAST.cooldown(combo);
  }

  private final Effector noEffect = create();

  @Override
  public String toString() {
    return "SPELL";
  }

  @Override
  public String describeAction() {
    return "cast";
  }

}
