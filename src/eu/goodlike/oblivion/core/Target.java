package eu.goodlike.oblivion.core;

public interface Target {

  void modifyResist(Factor factor, double magnitude);

  void damage(double dmg);

  void drain(double hp);

}
