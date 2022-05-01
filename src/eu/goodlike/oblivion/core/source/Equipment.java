package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Source;

import java.util.List;

import static eu.goodlike.oblivion.core.Factor.MAGIC;

public final class Equipment implements Source {

  @Override
  public Carrier withNoEffect() {
    return noEffect;
  }

  @Override
  public Carrier create(String label, List<EffectText> effects) {
    return new Carrier(label, this, MAGIC, effects);
  }

  @Override
  public boolean isWeapon() {
    return pattern != null;
  }

  @Override
  public boolean isPhysical() {
    return false;  // TODO: how to decide this?
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
    this(type, "", null, 0);
  }

  public Equipment(String type, String action, Hit.Pattern pattern, double timeToSwap) {
    this.type = type;
    this.action = action;
    this.pattern = pattern;
    this.timeToSwap = timeToSwap;
  }

  private final String type;
  private final String action;
  private final Hit.Pattern pattern;
  private final double timeToSwap;

  private final Carrier noEffect = create();

  @Override
  public String toString() {
    return type;
  }

  @Override
  public String describeAction() {
    return action;
  }

}
