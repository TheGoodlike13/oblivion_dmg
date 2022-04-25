package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.global.Write;

import java.util.ArrayList;
import java.util.List;

public final class SetHit extends BaseCommand {

  @Override
  protected void performTask() {
    Source source = Parse.source(input(0).substring(1));
    Carrier carrier = source.create(parseEffects());
    Hit hit = new Hit(carrier);
    arena.addHit(hit);
    Write.line("Hit #1: " + hit);
  }

  private List<EffectText> parseEffects() {
    List<EffectText> effects = new ArrayList<>();

    for (String input : inputs.subList(1, inputs.size())) {
      effects.add(Parse.effect(input));
    }

    return effects;
  }

}
