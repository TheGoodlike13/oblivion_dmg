package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.OblivionSpellStackingCalculator;

public final class Quit extends BaseCommand {

  @Override
  protected void performTask() {
    OblivionSpellStackingCalculator.ITS_ALL_OVER = true;
  }

}
