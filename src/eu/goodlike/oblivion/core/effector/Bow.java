package eu.goodlike.oblivion.core.effector;

import eu.goodlike.oblivion.core.Armament;
import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;

import static eu.goodlike.oblivion.Global.Settings.SHOOT;
import static eu.goodlike.oblivion.Global.Settings.SWAP_BOW;
import static eu.goodlike.oblivion.core.Effector.Factory.BOW;

public final class Bow extends BaseEffector implements Armament {

  @Override
  public boolean isEquipment() {
    return true;
  }

  @Override
  public boolean isPhysical() {
    return true;
  }

  @Override
  public double timeToSwap() {
    return SWAP_BOW;
  }

  @Override
  public double timeToHit(int combo) {
    return SHOOT.timeToHit(combo);
  }

  @Override
  public double cooldown(int combo) {
    return SHOOT.cooldown(combo);
  }

  @Override
  public Category<?> getCategory() {
    return BOW;
  }

  @Override
  public Effector copy(String label) {
    return new Bow(label, this);
  }

  public Bow(String label, Iterable<EffectText> effects) {
    super(label, effects);
  }

  @Override
  public String describeAction() {
    return "aim";
  }

}
