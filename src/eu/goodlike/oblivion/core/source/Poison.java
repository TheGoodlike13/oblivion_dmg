package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.effect.Resist;

import java.util.List;

public final class Poison implements Method, Source {

  @Override
  public EffectText resist(int magnitude) {
    return new EffectText(MAGIC, resistPoison, magnitude);
  }

  @Override
  public Carrier create(String name, List<EffectText> effects) {
    return new Carrier(this, name, Factor.POISON, HitId::new, effects);
  }

  @Override
  public double damageMultiplier() {
    return 1;
  }

  private final Resist.Type resistPoison = new Resist.OfFactor(this);

  @Override
  public String toString() {
    return "POISON";
  }

  public static Poison getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Poison();
    }
    return INSTANCE;
  }

  private static Poison INSTANCE;

  private static final class HitId implements Effect.Id {
    public HitId(String any, Effect.Type anyType) {
    }
  }

}
