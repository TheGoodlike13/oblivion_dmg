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
  public EffectText resist(int pc) {
    return new EffectText(MAGIC, resistPoison, pc);
  }

  @Override
  public Carrier create(List<EffectText> effects) {
    return new PoisonBottle(effects);
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

  private static final class PoisonBottle extends Carrier {
    @Override
    public Effect.Id toId(EffectText effect) {
      return new AlwaysUnique();
    }

    @Override
    public Carrier copy() {
      return new PoisonBottle(this);
    }

    public PoisonBottle(Iterable<EffectText> effects) {
      super(Source.POISON, Factor.POISON, effects);
    }
  }

  private static final class AlwaysUnique implements Effect.Id {
  }

}
