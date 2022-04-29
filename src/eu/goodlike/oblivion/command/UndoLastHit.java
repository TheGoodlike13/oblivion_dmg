package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.core.StructureException;

import java.util.stream.IntStream;

import static eu.goodlike.oblivion.Global.THE_ARENA;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class UndoLastHit extends BaseCommand {

  @Override
  protected void performTask() {
    tryParseTimes();
    IntStream.range(0, times).forEach(any -> THE_ARENA.removeLastHit());
  }

  private int times = 1;

  private void tryParseTimes() {
    String input = input(1);
    if (isNotBlank(input)) {
      times = StructureException.intOrThrow(input, "undo amount");
    }
  }

}
