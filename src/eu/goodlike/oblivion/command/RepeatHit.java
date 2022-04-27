package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.StructureException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static eu.goodlike.oblivion.Arena.THE_ARENA;

public final class RepeatHit extends BaseCommand {

  public static void cache(Hit hit) {
    String ref = "#" + COUNT.incrementAndGet();
    HITS.put(ref, hit);
    Write.inline("Hit " + ref + ": ");
  }

  public static void invalidate() {
    HITS.clear();
    COUNT.set(0);
  }

  @Override
  protected void performTask() {
    args().forEach(this::addHit);
  }

  private void addHit(String ref) {
    Hit hit = HITS.get(ref);
    if (hit == null) {
      throw new StructureException("No such hit", ref);
    }
    Write.inline("Hit " + ref + ": ");
    THE_ARENA.addHit(hit);
  }

  private static final Map<String, Hit> HITS = new HashMap<>();
  private static final AtomicInteger COUNT = new AtomicInteger(0);

}
