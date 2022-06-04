package eu.goodlike.oblivion.core.effector;

import eu.goodlike.oblivion.core.Armament;
import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;

import static eu.goodlike.oblivion.Global.Settings.CAST;
import static eu.goodlike.oblivion.core.Effector.Factory.POWER;

public class Power extends EffectorSkeleton implements Armament {

  @Override
  public final boolean isEquipment() {
    return false;
  }

  @Override
  public final boolean isRigid() {
    return true;
  }

  @Override
  public final double timeToSwap() {
    return 0;
  }

  @Override
  public final double timeToHit(int combo) {
    return CAST.timeToHit(combo);
  }

  @Override
  public final double cooldown(int combo) {
    return CAST.cooldown(combo);
  }

  @Override
  public Category<?> getCategory() {
    return POWER;
  }

  @Override
  public Effector copy(String label) {
    return new Power(label, this);
  }

  public Power(String label, Iterable<EffectText> effects) {
    super(label, effects);
  }

  @Override
  public final String describeAction() {
    return "cast";
  }

}
