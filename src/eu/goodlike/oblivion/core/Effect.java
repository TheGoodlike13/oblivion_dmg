package eu.goodlike.oblivion.core;

public interface Effect {

  interface Id {
  }

  interface Type {
    Effect activate(double magnitude, double duration);
    default boolean affectsHp() {
      return true;
    }
  }

  void onApply(Target target);

  void onRemove(Target target);

  void onTick(Target target);

  boolean hasExpired();

}
