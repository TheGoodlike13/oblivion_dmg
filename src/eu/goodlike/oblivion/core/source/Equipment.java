package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.HitPattern;
import eu.goodlike.oblivion.core.Source;

import java.util.List;

import static eu.goodlike.oblivion.core.Factor.MAGIC;

public final class Equipment implements Source {

  @Override
  public Effector withNoEffect() {
    return noEffect;
  }

  @Override
  public Effector create(String label, List<EffectText> effects) {
    return new Effector(label, this, MAGIC, effects);
  }

  @Override
  public boolean isWeapon() {
    return pattern != null;
  }

  @Override
  public boolean isPhysical() {
    return isPhysical;
  }

  @Override
  public double timeToSwap() {
    return timeToSwap;
  }

  @Override
  public double timeToHit(int combo) {
    return pattern.timeToHit(combo);
  }

  @Override
  public double cooldown(int combo) {
    return pattern.cooldown(combo);
  }

  public Equipment(String type) {
    this(type, "", null, 0, true);
  }

  public Equipment(String type, String action, HitPattern pattern, double timeToSwap, boolean isPhysical) {
    this.type = type;
    this.action = action;
    this.pattern = pattern;
    this.timeToSwap = timeToSwap;
    this.isPhysical = isPhysical;
  }

  private final String type;
  private final String action;
  private final HitPattern pattern;
  private final double timeToSwap;
  private final boolean isPhysical;

  private final Effector noEffect = create();

  @Override
  public String toString() {
    return type;
  }

  @Override
  public String describeAction() {
    return action;
  }

}
