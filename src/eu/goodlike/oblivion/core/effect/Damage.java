package eu.goodlike.oblivion.core.effect;

import eu.goodlike.oblivion.core.Target;

public final class Damage extends BaseEffect {

  public static final Type TYPE = Damage::new;

  @Override
  public void onApply(Target target) {
  }

  @Override
  public void onRemove(Target target) {
  }

  @Override
  protected void onEffectiveTick(Target target, double tick) {
    target.damage(tick * magnitude);
  }

  public Damage(double magnitude, double duration) {
    super(magnitude, duration);
  }

}
