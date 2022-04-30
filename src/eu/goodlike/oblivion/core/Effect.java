package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.Global.Settings;

/**
 * Active effect that influences the target.
 * The exact effect depends on {@link Type}.
 * The magnitude of this effect is snapshot upon activation and can no longer be changed until the effect is removed.
 * The duration of this effect expires via ticking.
 * <p/>
 * Not to be confused with {@link EffectText}, which describes base values before any multipliers come into play.
 */
public interface Effect {

  /**
   * Marker interface to uniquely identify the active effect.
   * Ensures effects from the same carrier (e.g. spell) do not stack.
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
     * @param duration full duration of the new effect in secs
     * @return an active effect of this type with full duration
     */
    Effect activate(double magnitude, double duration);

    /**
     * @return true if this type of effect influences or damages hp, false otherwise
     */
    boolean affectsHp();
  }

  /**
   * If this effect is persistent (e.g. weakness to magic), adds the influence to the given target.
   * For specifics, refer to the {@link Type} of the effect.
   * <p/>
   * Should be called when the active effect is applied to the target.
   * <p/>
   * Undefined if target is null.
   */
  void onApply(Target target);

  /**
   * If this effect is persistent (e.g. weakness to magic), removes the influence from the given target.
   * For specifics, refer to the {@link Type} of the effect.
   * <p/>
   * Should be called when the active effect is removed from the target.
   * This method will NOT be called automatically upon expiration.
   * <p/>
   * Undefined if target is null.
   */
  void onRemove(Target target);

  /**
   * Reduces the duration of this active effect by a single {@link Settings#TICK}.
   * If this effect is continuous (e.g. damage over time), influences the target in proportion to the tick.
   * If the remaining duration is less than a tick, the influence is proportional to the remaining duration instead.
   * For specifics, refer to the {@link Type} of the effect.
   * <p/>
   * Should be called for every tick while the effect is active.
   * Has no effect if the effect is expired.
   * <p/>
   * Undefined if target is null.
   */
  void onTick(Target target);

  /**
   * @return false if this effect still has duration remaining, true otherwise
   */
  boolean hasExpired();

}
