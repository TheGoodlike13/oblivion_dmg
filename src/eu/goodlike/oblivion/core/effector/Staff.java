package eu.goodlike.oblivion.core.effector;

import eu.goodlike.oblivion.core.Armament;
import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;

import static eu.goodlike.oblivion.Global.Settings.EMIT;
import static eu.goodlike.oblivion.Global.Settings.SWAP_STAFF;
import static eu.goodlike.oblivion.core.Effector.Factory.STAFF;

public final class Staff extends BaseEffector implements Armament {

  @Override
  public boolean isEquipment() {
    return true;
  }

  @Override
  public boolean isPhysical() {
    return false;
  }

  @Override
  public double timeToSwap() {
    return SWAP_STAFF;
  }

  @Override
  public double timeToHit(int combo) {
    return EMIT.timeToHit(combo);
  }

  @Override
  public double cooldown(int combo) {
    return EMIT.cooldown(combo);
  }

  @Override
  public Category<?> getCategory() {
    return STAFF;
  }

  @Override
  public Effector copy(String label) {
    return new Staff(label, this);
  }

  public Staff(String label, Iterable<EffectText> effects) {
    super(label, effects);
  }

  @Override
  public String describeAction() {
    return "invoke";
  }

}
