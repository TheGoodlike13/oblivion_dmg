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
    return new Carrier(this, name, MAGIC, UniquePerSpellPerType::new, effects);
  }

  @Override
  public String toString() {
    return "SPELL";
  }

  public static final Magic INSTANCE = new Magic();

  private static final class UniquePerSpellPerType implements Effect.Id {
    private UniquePerSpellPerType(Carrier carrier, EffectText effect) {
      this.spellName = carrier.getName();
      this.effectType = effect.getType();
    }

    private final String spellName;
    private final Effect.Type effectType;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      UniquePerSpellPerType other = (UniquePerSpellPerType)o;
      return Objects.equals(spellName, other.spellName)
        && Objects.equals(effectType, other.effectType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(spellName, effectType);
    }
  }

}
