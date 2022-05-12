package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.StructureException;

import static eu.goodlike.oblivion.Global.Settings.DIFFICULTY;

/**
 * Sets the in-game difficulty slider.
 * This affects damage done to enemies.
 */
public final class SetDifficulty extends BaseCommand {

  @Override
  protected void performTask() {
    double difficulty = StructureException.doubleOrThrow(input(1), "difficulty setting");
    DIFFICULTY = difficulty;
    Write.line("Difficulty slider has been set to <" + difficulty + ">");
  }

}
