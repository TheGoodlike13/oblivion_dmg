package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Source;

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
      if (input.startsWith("@")) {
        label = input.substring(1);
      }
      else if (input.startsWith("$")) {
        consumeLastParsedSource();
        Carrier c = CARRIERS.get(input.substring(1));
        carriers.add(c);
      }
      else if (input.startsWith("+")) {
        consumeLastParsedSource();
        source = Parse.source(input.substring(1));
      }
      else {
        effects.add(Parse.effect(input));
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

  private void consumeLastParsedSource() {
    if (source != null) {
      Carrier carrier = source.create(effects);
      cache(carrier);
      carriers.add(carrier);
    }

    label = null;
    source = null;
    effects = new ArrayList<>();
  }

  private void cache(Carrier carrier) {
    String ref = isBlank(label) ? String.valueOf(COUNT.incrementAndGet()) : label;
    CARRIERS.put(ref, carrier);
  }

  // TODO: move this cache out (when we have more similar stuff)
  private static final Map<String, Carrier> CARRIERS = new HashMap<>();
  private static final AtomicInteger COUNT = new AtomicInteger(0);

}
