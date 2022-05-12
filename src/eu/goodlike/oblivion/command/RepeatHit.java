package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.core.StructureException;

import java.util.stream.IntStream;

import static eu.goodlike.oblivion.Global.HITS;
import static eu.goodlike.oblivion.Global.THE_ARENA;

/**
 * Convenience command which allows repeating previous hits, especially multiple times.
 * Any input with or without '#' prefix will be treated as reference to hit to add next.
 * Any input with 'x' prefix will be treated as a multiplier for previous hit in this command.
 * 'x1' does nothing, as the command will have already added the hit.
 * 'x0' will NOT undo the hit.
 * That's just stupid.
 * Use 'undo' command for that.
 */
public final class RepeatHit extends BaseCommand {

  @Override
  protected void performTask() {
    inputs().forEach(this::addOrRepeat);
  }

  private String lastRef = "";

  private void addOrRepeat(String ref) {
    if (ref.startsWith("x")) {
      int times = StructureException.intOrThrow(ref.substring(1), "repeat count");
      repeatLastHit(times);
    }
    else {
      lastRef = ref.startsWith("#") ? ref.substring(1) : ref;
      enqueueHit();
    }
  }

  private void repeatLastHit(int times) {
    IntStream.range(1, times).forEach(any -> enqueueHit());
  }

  private void enqueueHit() {
    THE_ARENA.enqueueHit(HITS.getCached(lastRef));
  }

}
