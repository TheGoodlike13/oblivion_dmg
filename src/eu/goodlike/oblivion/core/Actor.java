package eu.goodlike.oblivion.core;

public interface Actor {

  default double getMultiplier(Factor factor) {
    return 1;
  }

}
