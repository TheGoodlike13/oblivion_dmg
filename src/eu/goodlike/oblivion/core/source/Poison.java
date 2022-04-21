package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.effect.Resist;

import java.util.Arrays;
import java.util.Iterator;

public final class Poison implements Method, Source {

  public static Poison INSTANCE;

  public static Poison getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Poison();
    }
    return INSTANCE;
  }

  @Override
  public EffectText resist(int magnitude) {
    return new EffectText(MAGIC, resistPoison, magnitude);
  }

  @Override
  public Carrier create(String name, EffectText... effects) {
    return new Carrier() {
      @Override
      public Source getSource() {
        return Poison.this;
      }

      @Override
      public String getName() {
        return name;
      }

      @Override
      public Method getMethod() {
        return Factor.POISON;
      }

      @Override
      public Effect.Id toId(EffectText effect) {
        return new HitId();
      }

      @Override
      public Iterator<EffectText> iterator() {
        return Arrays.asList(effects).iterator();
      }
    };
  }

  @Override
  public Effect.Id toId(String carrierName, Effect.Type type) {
    return new HitId();
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

  private static final class HitId implements Effect.Id {
  }

}
