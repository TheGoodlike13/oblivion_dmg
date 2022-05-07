package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.core.StructureException;

import static eu.goodlike.oblivion.Global.THE_ARENA;

public final class NowJustHoldOnACottonPickinMinute extends BaseCommand {

  @Override
  protected void performTask() {
    double waitTime = StructureException.positiveOrThrow(input(1), "wait time");
    THE_ARENA.addPause(waitTime);
  }

}
