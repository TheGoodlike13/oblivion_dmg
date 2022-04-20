package eu.goodlike.oblivion.core.method;

import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.effect.Resist;

public final class Poison implements Method {

  @Override
  public double damageMultiplier() {
    return 1;
  }

  @Override
  public EffectText resist(int magnitude) {
    return new EffectText(MAGIC, resistPoison, magnitude);
  }

  @Override
  public Effect.Id toId(Hit exactHit, Effect.Type type) {
    return new Poison.HitId();
  }

  private final Resist.Type resistPoison = new Resist.OfFactor(this);

  private static final class HitId implements Effect.Id {
  }

}
