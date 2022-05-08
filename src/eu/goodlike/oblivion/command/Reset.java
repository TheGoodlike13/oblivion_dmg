package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Global;
import eu.goodlike.oblivion.Write;

import static eu.goodlike.oblivion.SpellStackingCalculator.writeSettings;

/**
 * Resets fucking everything.
 * <p/>
 * Use this command when you done fucked up.
 * Almost equivalent to restarting the application.
 * However, does not refresh the project configuration (e.g. modifications to settings or prepared files).
 * To do that, use {@link Restart}.
 */
public final class Reset extends BaseCommand {

  @Override
  protected void performTask() {
    Write.separator();
    Global.initializeEverything();
    Write.line("Everything has been reset.");
    Write.line("Warning: using original config files.");
    Write.line("If you changed settings or prepared files, use 'restart' instead.");

    Write.separator();
    writeSettings();
  }

}
