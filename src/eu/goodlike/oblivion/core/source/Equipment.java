package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Source;

import java.util.List;
import java.util.Objects;

import static eu.goodlike.oblivion.core.Factor.MAGIC;

public final class Equipment implements Source {

  @Override
  public Carrier create(String name, List<EffectText> effects) {
    return new Carrier(this, name, MAGIC, UniquePerUnitPerType::new, effects);
  }

  public Equipment(String sourceName) {
    this.sourceName = sourceName;
  }

  private final String sourceName;

  @Override
  public String toString() {
    return sourceName;
  }

  private static final class UniquePerUnitPerType implements Effect.Id {
    private UniquePerUnitPerType(Carrier carrier, EffectText effect) {
      this.carrier = carrier;
      this.effectType = effect.getType();
    }

    private final Carrier carrier;
    private final Effect.Type effectType;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      UniquePerUnitPerType other = (UniquePerUnitPerType)o;
      return Objects.equals(carrier, other.carrier)
        && Objects.equals(effectType, other.effectType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(carrier, effectType);
    }
  }

}
