package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;

import static eu.goodlike.oblivion.Global.CACHES;

/**
 * Clears all references in args from all caches.
 * <p/>
 * This is an administrative command to help correct mistakes.
 * It shouldn't break anything, but may cause unexpected effects,
 * e.g. if numeric references are removed.
 * It's your responsibility to use it wisely!
 */
public final class JustForget extends BaseCommand {

  @Override
  protected void performTask() {
    args().map(this::removeSymbol).forEach(this::forget);
  }

  private String removeSymbol(String ref) {
    return ref.startsWith("$") ? ref.substring(1) : ref;
  }

  private void forget(String ref) {
    CACHES.forEach(cache -> cache.remove(ref));
    Write.line("All references to <" + ref + "> were removed from caches.");
  }

}
