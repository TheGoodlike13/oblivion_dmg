package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.NamedValue;
import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.StructureException;
import eu.goodlike.oblivion.parse.ParseCarrier;

import java.util.ArrayList;
import java.util.List;

import static eu.goodlike.oblivion.Global.CARRIERS;
import static eu.goodlike.oblivion.Global.HITS;
import static eu.goodlike.oblivion.Global.THE_ARENA;

public final class SetHit extends BaseCommand {

  @Override
  protected void performTask() {
    parseCarriers();
    Hit hit = new Hit(carriers);
    THE_ARENA.addHit(HITS.put(hit));
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
    carriers.add(CARRIERS.getCached(ref).getValue());
  }

  private void ensureNoDangleBerries(String input) {
    if (start == -1) {
      throw new StructureException("Dangling hit param", input);
    }
  }

  private void parseNextCarrierIfAny() {
    if (start >= 0) {
      NamedValue<Carrier> carrier = new ParseCarrier(inputs(start, cursor)).thenCache();
      carriers.add(carrier.getValue());
    }

    start = -1;
  }

}
