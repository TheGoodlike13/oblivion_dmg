package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Global;
import eu.goodlike.oblivion.Write;

/**
 * Reloads all caches.
 */
public final class Reload extends BaseCommand {

  @Override
  protected void performTask() {
    Write.separator();
    Global.reloadCaches();
    Write.separator();
    Write.line("Caches have been reset.");
    Write.line("Warning: using original config files.");
    Write.line("If you changed settings or prepared files, use 'restart' instead.");
    Write.separator();
  }

}
