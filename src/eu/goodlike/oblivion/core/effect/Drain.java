package eu.goodlike.oblivion.core.effect;

import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.Target;

public final class Drain extends BaseEffect {

  public static final Type TYPE = new Type() {
    @Override
    public Effect activate(double magnitude, double duration) {
      return new Drain(magnitude, duration);
    }

    @Override
    public boolean affectsHp() {
      return true;
    }

    @Override
    public String toString() {
      return "DRAIN LIFE";
    }
  };

  @Override
  public void onApply(Target target) {
    target.drain(magnitude);
  }

  @Override
  public void onRemove(Target target) {
    target.drain(-magnitude);
  }

  @Override
  protected void onEffectiveTick(Target target, double tick) {
  }

  public Drain(double magnitude, double duration) {
    super(magnitude, duration);
  }

}
