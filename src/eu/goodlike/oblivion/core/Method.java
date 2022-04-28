package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.Global.Settings;

import static eu.goodlike.oblivion.Global.Settings.DIFFICULTY;

/**
 * Describes how the influence of the effects is delivered.
 * There are 2 methods: {@link #POISON} and {@link #MAGIC}.
 * {@link #MAGIC} in particular includes not only spells, but also enchanted items, arrows, staffs, etc.
 */
public interface Method extends Factor {

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

}
