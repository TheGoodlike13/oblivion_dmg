package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.StructureException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static eu.goodlike.oblivion.Arena.THE_ARENA;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class SetHit extends BaseCommand {

  public static void invalidate() {
    CARRIERS.clear();
    COUNT.set(0);
  }

  @Override
  protected void performTask() {
    for (String input : inputs) {
      if (input.startsWith("$")) {
        consumeLastParsedSource();
        parseNextReference(input);
      }
      else if (input.startsWith("+")) {
        consumeLastParsedSource();
        source = Parse.source(input.substring(1));
      }
      else {
        ensureNoDangleBerries(input);
        parseSimpleParam(input);
      }
    }
    consumeLastParsedSource();

    Hit hit = new Hit(carriers);
    RepeatHit.cache(hit);
    THE_ARENA.addHit(hit);
  }

  private String label;
  private Source source;
  private List<EffectText> effects;
  private final List<Carrier> carriers = new ArrayList<>();

  private void parseNextReference(String input) {
    String ref = input.substring(1);
    Carrier c = CARRIERS.get(ref);
    if (c == null) {
      throw new StructureException("Nothing with name", ref);
    }
    carriers.add(c);
  }

  private void ensureNoDangleBerries(String input) {
    if (source == null) {
      throw new StructureException("Dangling hit param", input);
    }
  }

  private void parseSimpleParam(String input) {
    if (input.startsWith("@")) {
      label = input.substring(1);
    }
    else {
      effects.add(Parse.effect(input));
    }
  }

  private void consumeLastParsedSource() {
    if (source != null) {
      Carrier carrier = createAndCache();
      carriers.add(carrier);
    }

    label = null;
    source = null;
    effects = new ArrayList<>();
  }

  private Carrier createAndCache() {
    String ref = isBlank(label) ? String.valueOf(COUNT.incrementAndGet()) : label;
    Carrier carrier = source.create(ref, effects);
    if (CARRIERS.putIfAbsent(ref, carrier) != null) {
      throw new StructureException("Name already in use", ref);
    }
    return carrier;
  }

  // TODO: move this cache out (when we have more similar stuff)
  private static final Map<String, Carrier> CARRIERS = new HashMap<>();
  private static final AtomicInteger COUNT = new AtomicInteger(0);

}
