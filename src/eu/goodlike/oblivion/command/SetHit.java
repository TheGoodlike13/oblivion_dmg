package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.NamedValue;
import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.StructureException;
import eu.goodlike.oblivion.parse.ParseEffector;

import java.util.ArrayDeque;
import java.util.Deque;

import static eu.goodlike.oblivion.Global.EFFECTORS;
import static eu.goodlike.oblivion.Global.HITS;
import static eu.goodlike.oblivion.Global.THE_ARENA;

/**
 * Sets the next hit for the next calculation.
 * All the inputs are parsed as a list of effectors.
 * Args that start with '$' are treated as references to effectors in cache.
 * Args that start with '+' indicate a new effector.
 * All args following that will be parsed as a effector until the next one is indicated.
 * For details, see {@link ParseEffector}.
 */
public final class SetHit extends BaseCommand {

  @Override
  protected void performTask() {
    parseEffectors();
    Hit hit = hitFromParsedEffectors();
    THE_ARENA.addHit(HITS.put(hit));
  }

  private int cursor = -1;
  private int start = -1;
  private final Deque<Effector> effectors = new ArrayDeque<>();

  private void parseEffectors() {
    while (++cursor < inputs.length) {
      identify(inputs[cursor]);
    }
    parseNextEffectorIfAny();
  }

  private void identify(String input) {
    if (input.startsWith("$")) {
      parseNextEffectorIfAny();
      parseNextReference(input.substring(1));
    }
    else if (input.startsWith("+")) {
      parseNextEffectorIfAny();
      start = cursor;
    }
    else if (start < 0) {
      parseDangleBerries(input);
    }
  }

  private void parseNextReference(String ref) {
    effectors.add(EFFECTORS.getCached(ref).getValue());
  }

  private void parseDangleBerries(String input) {
    if (input.startsWith(":") && !effectors.isEmpty()) {
      String label = input.substring(1);
      Effector copy = EFFECTORS.put(label, effectors.removeLast(), Effector::copy).getValue();
      effectors.add(copy);
    }
    else {
      throw new StructureException("Dangling hit param", input);
    }
  }

  private void parseNextEffectorIfAny() {
    if (start >= 0) {
      NamedValue<Effector> effector = ParseEffector.forUser(inputs(start, cursor)).thenCache();
      effectors.add(effector.getValue());
    }

    start = -1;
  }

  private Hit hitFromParsedEffectors() {
    try {
      return new Hit(effectors);
    }
    catch (StructureException e) {
      effectors.forEach(this::writeForReference);
      throw e;
    }
  }

  private void writeForReference(Effector effector) {
    Write.line(effector.toString());
  }

}
