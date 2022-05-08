package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.core.StructureException;

import static eu.goodlike.oblivion.Global.THE_ARENA;

/**
 * Ensures a delay between hits.
 * This delay is counted between the initiation of hits.
 * This means the time to hit or any subsequent cooldowns and swaps will be considered as part of delay.
 * <p/>
 * Delays can break combos if the effective delay (after taking into account the other values)
 * exceeds the cooldown of the last hit.
 */
public final class NowJustHoldOnACottonPickinMinute extends BaseCommand {

  @Override
  protected void performTask() {
    double waitTime = StructureException.positiveOrZeroOrThrow(input(1), "wait time");
    THE_ARENA.setPause(waitTime);
  }

}
