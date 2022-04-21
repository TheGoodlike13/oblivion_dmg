package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.effect.Drain;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public final class Magic extends Element implements Method, Source {

  public EffectText drain(double hp) {
    return new EffectText(this, Drain.TYPE, hp);
  }

  @Override
  public Carrier create(String name, EffectText... effects) {
    return new Carrier() {
      @Override
      public Source getSource() {
        return Magic.this;
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
        return new Magic.HitId(getName(), effect.getType());
      }

      @Override
      public Iterator<EffectText> iterator() {
        return Arrays.asList(effects).iterator();
      }
    };
  }

  @Override
  public String toString() {
    return "SPELL";
  }

  public static final Magic INSTANCE = new Magic();

  public static final class HitId implements Effect.Id {
    public HitId(String hitName, Effect.Type type) {
      this.hitName = hitName;
      this.type = type;
    }

    private final String hitName;
    private final Effect.Type type;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      HitId effectId = (HitId)o;
      return Objects.equals(hitName, effectId.hitName)
        && Objects.equals(type, effectId.type);
    }

    @Override
    public int hashCode() {
      return Objects.hash(hitName, type);
    }
  }

}
