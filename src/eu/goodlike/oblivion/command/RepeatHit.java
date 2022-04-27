package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.StructureException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static eu.goodlike.oblivion.Arena.THE_ARENA;

public final class RepeatHit extends BaseCommand {

  public static void cache(Hit hit) {
    String ref = "#" + COUNT.incrementAndGet();
    HITS.put(ref, hit);
    writeRef(ref);
  }

  public static void invalidate() {
    HITS.clear();
    COUNT.set(0);
  }

  @Override
  protected void performTask() {
    args().forEach(this::addOrRepeat);
  }

  private String lastRef = "";

  private void addOrRepeat(String ref) {
    if (ref.startsWith("x")) {
      int times = parseInt(ref.substring(1));
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
    Hit hit = HITS.get(lastRef);
    if (hit == null) {
      throw new StructureException("No hit found", lastRef);
    }
    writeRef(lastRef);
    THE_ARENA.addHit(hit);
  }

  private static void writeRef(String ref) {
    Write.inline("[" + ref + "] ");
  }

  private int parseInt(String count) {
    try {
      return Integer.parseInt(count);
    }
    catch (NumberFormatException e) {
      throw new StructureException("Cannot parse repeat count", count, e);
    }
  }

  // TODO: move this cache out (when we have more similar stuff)
  private static final Map<String, Hit> HITS = new HashMap<>();
  private static final AtomicInteger COUNT = new AtomicInteger(0);

}
