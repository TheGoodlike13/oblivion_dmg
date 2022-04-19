package eu.goodlike.oblivion.core;

/**
 * Active effect that influences or damages the target. The magnitude of this effect is snapshot upon activation and
 * can no longer be influenced until the effect is removed. The duration of this effect expires via ticking.
 * <p/>
 * Not to be confused with {@link EffectText}, which describes base values before any multipliers come into play.
 */
public interface Effect {

  /**
   * Marker interface to uniquely identify the active effect. Ensures effects from the same source (e.g. spell) do not
   * stack.
   */
  interface Id {
  }

  /**
   * Categorizes the effects and the way they affect the target.
   * <p/>
   * Can be considered a factory for active effects.
   */
  interface Type {
    /**
     * @param magnitude effective magnitude of the new effect, after all multipliers are taken into account
     * @param duration full duration of the new effect
     * @return an active effect of this type with full duration
     */
    Effect activate(double magnitude, double duration);

    /**
     * @return true if this type of effect influences or damages hp, false otherwise
     */
    default boolean affectsHp() {
      return true;
    }
  }

  void onApply(Target target);

  void onRemove(Target target);

  void onTick(Target target);

  boolean hasExpired();

}
