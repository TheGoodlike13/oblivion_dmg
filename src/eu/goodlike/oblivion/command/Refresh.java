package eu.goodlike.oblivion.command;

import static eu.goodlike.oblivion.Global.THE_ARENA;

public final class Refresh extends BaseCommand {

  @Override
  protected void performTask() {
    THE_ARENA.refresh();
  }

}
