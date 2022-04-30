package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;

/**
 * Default command for things that don't match any other commands.
 */
public final class ButWhatDoesThisMean extends BaseCommand {

  @Override
  protected void performTask() {
    Write.line("No idea what <" + input() + "> is supposed to mean.");
  }

}
