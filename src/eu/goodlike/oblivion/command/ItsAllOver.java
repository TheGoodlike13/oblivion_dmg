package eu.goodlike.oblivion.command;

import static eu.goodlike.oblivion.Global.ITS_ALL_OVER;

public final class ItsAllOver extends BaseCommand {

  @Override
  protected void performTask() {
    ITS_ALL_OVER = true;
  }

}
