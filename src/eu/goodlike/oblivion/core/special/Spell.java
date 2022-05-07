package eu.goodlike.oblivion.core.special;

import eu.goodlike.oblivion.core.Armament;
import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.effector.BaseEffector;

import java.util.Iterator;

import static eu.goodlike.oblivion.Global.Settings.CAST;

public final class Spell extends Category<Spell.Cast> {

  public Spell() {
    super("SPELL", Cast::new);
  }

  public static final class Cast extends BaseEffector implements Armament {
    @Override
    public boolean isEquipment() {
      return false;
    }

    @Override
    public boolean isPhysical() {
      return false;
    }

    @Override
    public double timeToSwap() {
      return 0;
    }

    @Override
    public double timeToHit(int combo) {
      return CAST.timeToHit(combo);
    }

    @Override
    public double cooldown(int combo) {
      return CAST.cooldown(combo);
    }

    @Override
    public Category<?> getCategory() {
      return SPELL;
    }

    @Override
    public Effector copy(String label) {
      return new Cast(label, this);
    }

    @Override
    public Iterator<EffectText> iterator() {
      return super.iterator();
    }

    public Cast(String label, Iterable<EffectText> effects) {
      super(label, effects);
    }

    @Override
    public String describeAction() {
      return "cast";
    }
  }

}
