package eu.goodlike.oblivion.command;

import static eu.goodlike.oblivion.Arena.THE_ARENA;

public final class TimeToGo extends BaseCommand {

  @Override
  protected void performTask() {
    THE_ARENA.lowerTheGates();
  }

}
