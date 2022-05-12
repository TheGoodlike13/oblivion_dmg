package eu.goodlike.oblivion.command;

import static eu.goodlike.oblivion.Global.THE_ARENA;

/**
 * Clears the hit queue and refreshes selected enemy, if any.
 */
public final class Refresh extends BaseCommand {

  @Override
  protected void performTask() {
    THE_ARENA.refresh();
  }

}
