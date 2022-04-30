package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Global;
import eu.goodlike.oblivion.Write;

/**
 * Resets fucking everything.
 * <p/>
 * Use this command when you done fucked up.
 * Equivalent to restarting the application.
 */
public final class Reset extends BaseCommand {

  @Override
  protected void performTask() {
    Write.separator();
    Global.initializeEverything();
    Write.line("Everything has been reset.");
  }

}
