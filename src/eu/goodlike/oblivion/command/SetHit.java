package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.NamedValue;
import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.StructureException;
import eu.goodlike.oblivion.parse.AsCarrier;

import java.util.ArrayList;
import java.util.List;

import static eu.goodlike.oblivion.Arena.THE_ARENA;
import static eu.goodlike.oblivion.Global.CARRIERS;

public final class SetHit extends BaseCommand {

  @Override
  protected void performTask() {
    parseCarriers();

    Hit hit = new Hit(carriers);
    RepeatHit.cache(hit);
    THE_ARENA.addHit(hit);
  }

  private int cursor = -1;
  private int start = -1;
  private final List<Carrier> carriers = new ArrayList<>();

  private void parseCarriers() {
    while (++cursor < inputs.length) {
      identifyInput(inputs[cursor]);
    }
    parseNextCarrierIfAny();
  }

  private void identifyInput(String input) {
    if (input.startsWith("$")) {
      parseNextCarrierIfAny();
      parseNextReference(input.substring(1));
    }
    else if (input.startsWith("+")) {
      parseNextCarrierIfAny();
      start = cursor;
    }
    else {
      ensureNoDangleBerries(input);
    }
  }

  private void parseNextReference(String ref) {
    carriers.add(CARRIERS.get(ref));
  }

  private void ensureNoDangleBerries(String input) {
    if (start == -1) {
      throw new StructureException("Dangling hit param", input);
    }
  }

  private void parseNextCarrierIfAny() {
    if (start >= 0) {
      NamedValue<Carrier> carrier = new AsCarrier(inputs(start, cursor)).inCache();
      carriers.add(carrier.getValue());
    }

    start = -1;
  }

}
