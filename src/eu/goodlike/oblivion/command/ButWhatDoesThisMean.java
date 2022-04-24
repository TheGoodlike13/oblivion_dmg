package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.global.Write;

public final class ButWhatDoesThisMean extends BaseCommand {

  @Override
  protected void performTask() {
    Write.line("No idea what <" + input() + "> is supposed to mean.");
  }

}
