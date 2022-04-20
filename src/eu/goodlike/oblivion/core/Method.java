package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.core.hit.MonoHit;
import eu.goodlike.oblivion.global.Settings;

import java.util.Arrays;
import java.util.List;

import static eu.goodlike.oblivion.global.Settings.DIFFICULTY;

/**
 * Describes how the influence of the effects is delivered.
 * There are 2 methods: {@link #POISON} and {@link #MAGIC}.
 * {@link #MAGIC} in particular includes not only spells, but also enchanted items, arrows, staffs, etc.
 */
public interface Method extends Factor {

  /**
   * This method is useful for testing.
   * Use {@link #hit(String, List)} for dynamic purposes.
   *
   * @param name name of the source of the hit, e.g. spell name; may be used to determine uniqueness of effects
   * @param effects effects to combine into a single hit using this method
   * @return a hit (or part of it) which will be delivered using this method
   */
  default Hit hit(String name, EffectText... effects) {
    return hit(name, Arrays.asList(effects));
  }

  /**
   * @param name name of the source of the hit, e.g. spell name; may be used to determine uniqueness of effects
   * @param effects effects to combine into a single hit using this method
   * @return a hit (or part of it) which will be delivered using this method
   */
  default Hit hit(String name, List<EffectText> effects) {
    return new MonoHit(name, this, effects);
  }

  /**
   * All damage done by the player is affected by the in-game {@link Settings#DIFFICULTY}.
   * However, some methods of dealing damage are not considered as done by player, such as
   * summoned creatures or {@link #POISON}.
   * Such methods should override the multiplier and set it to always 1.
   *
   * @return amount of times to multiply the damage upon effect activation
   */
  default double damageMultiplier() {
    double delta = DIFFICULTY / 10 - 5;
    double timesEasier = Math.max(1, 1 - delta);
    double timesHarder = Math.max(1, 1 + delta);
    return timesEasier / timesHarder;
  }

  Effect.Id toId(Hit exactHit, Effect.Type type);

}
