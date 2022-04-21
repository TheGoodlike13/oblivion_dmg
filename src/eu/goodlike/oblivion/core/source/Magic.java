package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.effect.Drain;

import java.util.List;
import java.util.Objects;

public final class Magic extends Element implements Method, Source {

  public EffectText drain(double hp) {
    return new EffectText(this, Drain.TYPE, hp);
  }

  @Override
  public Carrier create(String name, List<EffectText> effects) {
    return new Carrier(this, name, MAGIC, Magic.HitId::new, effects);
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
