package eu.goodlike.oblivion.command;

import static eu.goodlike.oblivion.OblivionSpellStackingCalculator.ITS_ALL_OVER;

public final class ItsAllOver extends BaseCommand {

  @Override
  protected void performTask() {
    ITS_ALL_OVER = true;
  }

}
