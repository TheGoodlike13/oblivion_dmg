package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Cache;
import eu.goodlike.oblivion.Global;
import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.StructureException;

import java.util.ArrayList;
import java.util.List;

import static eu.goodlike.oblivion.Arena.THE_ARENA;
import static org.apache.commons.lang3.StringUtils.split;

public final class SetHit extends BaseCommand {

  public static void invalidate() {
    CACHE.reset(SetHit::parseCarrier, Global.Settings.PREPARED_ITEMS, Global.Settings.PREPARED_SPELLS);
  }

  @Override
  protected void performTask() {
    parseCarriers();

    Hit hit = new Hit(carriers);
    RepeatHit.cache(hit);
    THE_ARENA.addHit(hit);
  }

  private String label;
  private Source source;
  private List<EffectText> effects;
  private final List<Carrier> carriers = new ArrayList<>();

  private void parseCarriers() {
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
  }

  private void parseNextReference(String input) {
    String ref = input.substring(1);
    Carrier c = CACHE.get(ref);
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
    return CACHE.put(label, ref -> source.create(ref, effects));
  }

  // TODO: move this cache out (when we have more similar stuff)
  private static final Cache<Carrier> CACHE = new Cache<>();

  private static void parseCarrier(String line) {
    String[] inputs = split(line.trim().toLowerCase());
    SetHit setHit = new SetHit();
    setHit.setParams(inputs);
    setHit.parseCarriers();
  }

}
