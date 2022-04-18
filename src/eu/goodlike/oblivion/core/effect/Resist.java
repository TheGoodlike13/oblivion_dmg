package eu.goodlike.oblivion.core.effect;

import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Target;

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
  }

  @Override
  public void onApply(Target target) {
    target.modifyResist(resist.factor, magnitude);
  }

  @Override
  public void onRemove(Target target) {
    target.modifyResist(resist.factor, -magnitude);
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
