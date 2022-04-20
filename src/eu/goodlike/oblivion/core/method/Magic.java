package eu.goodlike.oblivion.core.method;

import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.effect.Drain;

import java.util.Objects;

public final class Magic extends Element implements Method {

  @Override
  public Effect.Id toId(Hit monoHit, Effect.Type type) {
    return new Magic.HitId(monoHit.toString(), type);
  }

  public EffectText drain(double hp) {
    return new EffectText(this, Drain.TYPE, hp);
  }

  private static final class HitId implements Effect.Id {
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
