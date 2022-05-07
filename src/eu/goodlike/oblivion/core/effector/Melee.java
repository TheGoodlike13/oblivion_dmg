package eu.goodlike.oblivion.core.effector;

import eu.goodlike.oblivion.core.Armament;
import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;

import static eu.goodlike.oblivion.Global.Settings.STRIKE;
import static eu.goodlike.oblivion.Global.Settings.SWAP_MELEE;
import static eu.goodlike.oblivion.core.Effector.Factory.MELEE;

public final class Melee extends BaseEffector implements Armament {

  @Override
  public boolean isEquipment() {
    return true;
  }

  @Override
  public boolean isRigid() {
    return false;
  }

  @Override
  public double timeToSwap() {
    return SWAP_MELEE;
  }

  @Override
  public double timeToHit(int combo) {
    return STRIKE.timeToHit(combo);
  }

  @Override
  public double cooldown(int combo) {
    return STRIKE.cooldown(combo);
  }

  @Override
  public Category<?> getCategory() {
    return MELEE;
  }

  @Override
  public Effector copy(String label) {
    return new Melee(label, this);
  }

  public Melee(String label, Iterable<EffectText> effects) {
    super(label, effects);
  }

  @Override
  public String describeAction() {
    return "swing";
  }

}
