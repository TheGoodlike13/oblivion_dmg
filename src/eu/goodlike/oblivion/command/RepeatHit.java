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

  private String lastRef;
  private Hit lastHit;

  private void addOrRepeat(String ref) {
    if (ref.startsWith("x")) {
      int times = parseInt(ref.substring(1));
      repeatLastHit(times);
    }
    else {
      findAndAdd(ref);
    }
  }

  private void repeatLastHit(int times) {
    if (lastHit == null) {
      throw new StructureException("Nothing to repeat", times);
    }
    IntStream.range(1, times).forEach(any -> addHit(lastRef));
  }

  private void findAndAdd(String ref) {
    lastRef = ref;
    lastHit = HITS.get(ref);
    if (lastHit == null) {
      throw new StructureException("No such hit", ref);
    }
    addHit(ref);
  }

  private void addHit(String ref) {
    writeRef(ref);
    THE_ARENA.addHit(lastHit);
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

  private static final Map<String, Hit> HITS = new HashMap<>();
  private static final AtomicInteger COUNT = new AtomicInteger(0);

}
