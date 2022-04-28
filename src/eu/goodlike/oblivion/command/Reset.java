package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Global;
import eu.goodlike.oblivion.Write;

public final class Reset extends BaseCommand {

  @Override
  protected void performTask() {
    Write.separator();
    Global.initializeEverything();
    Write.line("Everything has been reset.");
  }

}
