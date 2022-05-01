package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.StructureException;

import static eu.goodlike.oblivion.Global.Settings.DIFFICULTY;

public final class SetDifficulty extends BaseCommand {

  @Override
  protected void performTask() {
    double difficulty = StructureException.doubleOrThrow(input(1), "difficulty setting");
    DIFFICULTY = difficulty;
    Write.line("Difficulty slider has been set to <" + difficulty + ">");
  }

}
