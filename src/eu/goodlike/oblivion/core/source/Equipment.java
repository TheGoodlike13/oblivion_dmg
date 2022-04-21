package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.Source;

import java.util.Arrays;
import java.util.Iterator;

import static eu.goodlike.oblivion.core.Factor.MAGIC;

public final class Equipment implements Source {

  @Override
  public Carrier create(String name, EffectText... effects) {
    return new Carrier() {
      @Override
      public Source getSource() {
        return Equipment.this;
      }

      @Override
      public String getName() {
        return name;
      }

      @Override
      public Method getMethod() {
        return MAGIC;
      }

      @Override
      public Effect.Id toId(EffectText effect) {
        return new Magic.HitId(name, effect.getType());
      }

      @Override
      public Iterator<EffectText> iterator() {
        return Arrays.asList(effects).iterator();
      }
    };
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
