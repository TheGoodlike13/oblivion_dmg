package eu.goodlike.oblivion.core.effect;

import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Target;

import java.util.Objects;

public final class Resist extends BaseEffect {

  public static final class OfFactor implements Type {
    @Override
    public Effect activate(double magnitude, double duration) {
      return new Resist(magnitude, duration, this);
    }

    @Override
    public boolean affectsHp() {
      return false;
    }

    public OfFactor(Factor factor) {
      this.factor = factor;
    }

    private final Factor factor;

    @Override
    public String toString() {
      return "RESIST " + factor;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      OfFactor ofFactor = (OfFactor)o;
      return Objects.equals(factor, ofFactor.factor);
    }

    @Override
    public int hashCode() {
      return Objects.hash(factor);
    }
  }

  @Override
  public void onApply(Target target) {
    target.modifyResist(resist.factor, effectiveMagnitude());
  }

  @Override
  public void onRemove(Target target) {
    target.modifyResist(resist.factor, -effectiveMagnitude());
  }

  @Override
  protected void onEffectiveTick(Target target, double tick) {
  }

  public Resist(double magnitude, double duration, Resist.OfFactor resist) {
    super(magnitude, duration);
    this.resist = resist;
  }

  private final Resist.OfFactor resist;

}
