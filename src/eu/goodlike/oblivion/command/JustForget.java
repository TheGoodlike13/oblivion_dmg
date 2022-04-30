package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;

import static eu.goodlike.oblivion.Global.CACHES;

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
