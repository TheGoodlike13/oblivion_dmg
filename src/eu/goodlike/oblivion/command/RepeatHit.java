package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Cache;
import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.StructureException;

import java.util.stream.IntStream;

import static eu.goodlike.oblivion.Arena.THE_ARENA;

public final class RepeatHit extends BaseCommand {

  public static void cache(Hit hit) {
    String ref = CACHE.put(hit);
    writeRef(ref);
  }

  public static void invalidate() {
    CACHE.reset(null);
  }

  @Override
  protected void performTask() {
    args().forEach(this::addOrRepeat);
  }

  private String lastRef = "#";

  private void addOrRepeat(String ref) {
    if (ref.startsWith("x")) {
      int times = StructureException.intOrThrow(ref.substring(1), "repeat count");
      repeatLastHit(times);
    }
    else {
      lastRef = ref;
      addHit();
    }
  }

  private void repeatLastHit(int times) {
    IntStream.range(1, times).forEach(any -> addHit());
  }

  private void addHit() {
    Hit hit = CACHE.get(lastRef.substring(1));
    writeRef(lastRef.substring(1));
    THE_ARENA.addHit(hit);
  }

  private static void writeRef(String ref) {
    Write.inline("[#" + ref + "] ");
  }

  // TODO: move this cache out (when we have more similar stuff)
  private static final Cache<Hit> CACHE = new Cache<>();

}
