package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.core.StructureException;

import java.util.stream.IntStream;

import static eu.goodlike.oblivion.Arena.THE_ARENA;
import static eu.goodlike.oblivion.Global.HITS;

public final class RepeatHit extends BaseCommand {

  @Override
  protected void performTask() {
    args().forEach(this::addOrRepeat);
  }

  private String lastRef = "";

  private void addOrRepeat(String ref) {
    if (ref.startsWith("x")) {
      int times = StructureException.intOrThrow(ref.substring(1), "repeat count");
      repeatLastHit(times);
    }
    else if (ref.startsWith("#")) {
      lastRef = ref.substring(1);
      addHit();
    }
    else {
      throw new StructureException("Unexpected param", ref);
    }
  }

  private void repeatLastHit(int times) {
    IntStream.range(1, times).forEach(any -> addHit());
  }

  private void addHit() {
    THE_ARENA.addHit(HITS.get(lastRef));
  }

}
