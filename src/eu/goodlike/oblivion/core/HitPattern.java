package eu.goodlike.oblivion.core;

public interface HitPattern {

  double timeToHit(int combo);
  double cooldown(int combo);

}
