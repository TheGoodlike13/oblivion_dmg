package eu.goodlike.oblivion.core;

public interface Target {

  default double getMultiplier(Factor factor) {
    return 1;
  }

  void modifyResist(Factor factor, double magnitude);

  void damage(double dmg);

  void drain(double hp);

}
