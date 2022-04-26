package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.OblivionSpellStackingCalculator;

public final class ItsAllOver extends BaseCommand {

  @Override
  protected void performTask() {
    OblivionSpellStackingCalculator.ITS_ALL_OVER = true;
  }

}
