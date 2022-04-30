package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.StructureException;

import static eu.goodlike.oblivion.Global.Settings.LEVEL;

public final class SetLevel extends BaseCommand {

  @Override
  protected void performTask() {
    int level = StructureException.natOrThrow(input(1), "player level");
    LEVEL = level;
    Write.line("Player level has been set to <" + level + ">.");
  }

}
