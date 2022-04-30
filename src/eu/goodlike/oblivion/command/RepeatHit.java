package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.core.StructureException;

import java.util.stream.IntStream;

import static eu.goodlike.oblivion.Global.HITS;
import static eu.goodlike.oblivion.Global.THE_ARENA;

/**
 * Convenience command which allows repeating previous hits, especially multiple times.
 * Any input with '#' prefix will be treated as reference to hit to add next.
 * Any input with 'x' prefix will be treated as a multiplier for previous hit in this command.
 * 'x1' does nothing, as the command will have already added the hit.
 * 'x0' will NOT undo the hit.
 * That's just stupid.
 * Use 'undo' command for that.
 */
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
    THE_ARENA.addHit(HITS.getCached(lastRef));
  }

}
