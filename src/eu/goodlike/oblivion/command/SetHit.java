package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Source;

import java.util.ArrayList;
import java.util.List;

import static eu.goodlike.oblivion.Arena.THE_ARENA;

public final class SetHit extends BaseCommand {

  @Override
  protected void performTask() {
    for (String input : inputs) {
      if (input.startsWith("+")) {
        consumeLastParsedSource();
        source = Parse.source(input.substring(1));
        effects = new ArrayList<>();
      }
      else {
        effects.add(Parse.effect(input));
      }
    }
    consumeLastParsedSource();

    Hit hit = new Hit(carriers);
    THE_ARENA.addHit(hit);
  }

  private void consumeLastParsedSource() {
    if (source != null) {
      Carrier carrier = source.create(effects);
      carriers.add(carrier);
      source = null;
    }
  }

  private Source source;
  private List<EffectText> effects;
  private final List<Carrier> carriers = new ArrayList<>();

}
