package eu.goodlike.oblivion.core.effect;

import eu.goodlike.oblivion.core.Target;

public final class Drain extends BaseEffect {

  public static final Type TYPE = Drain::new;

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
