package eu.goodlike.oblivion.command;

import com.google.common.collect.ImmutableMap;
import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.StructureException;
import eu.goodlike.oblivion.global.Write;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.goodlike.oblivion.core.Source.ARROW;
import static eu.goodlike.oblivion.core.Source.BOW;
import static eu.goodlike.oblivion.core.Source.MELEE;
import static eu.goodlike.oblivion.core.Source.POISON;
import static eu.goodlike.oblivion.core.Source.SPELL;
import static eu.goodlike.oblivion.core.Source.STAFF;

public final class SetHit extends BaseCommand {

  @Override
  protected void performTask() {
    Source source = parseSource();
    Carrier carrier = source.create(parseEffects());
    Hit hit = new Hit(carrier);
    arena.addHit(hit);
    Write.line("Hit #1: " + hit);
  }

  private Source parseSource() {
    String sourceInput = input(0).substring(1);
    for (Map.Entry<String, Source> e : SOURCES.entrySet()) {
      if (StringUtils.startsWithIgnoreCase(e.getKey(), sourceInput)) {
        return e.getValue();
      }
    }
    throw new StructureException("Unknown source reference", sourceInput);
  }

  private List<EffectText> parseEffects() {
    List<EffectText> effects = new ArrayList<>();

    for (String input : inputs.subList(1, inputs.size())) {
      Matcher matcher = EFFECT_PATTERN.matcher(input);
      if (!matcher.matches()) {
        throw new StructureException("Invalid effect format", input);
      }

      EffectText effect = null;

      int magnitude = Integer.parseInt(matcher.group(1));

      String desc = matcher.group(2);
      // TODO: resist
      if (StringUtils.startsWithIgnoreCase(desc, "w") || StringUtils.startsWithIgnoreCase(desc, "r")) {
        Factor factor = null;

        String prefix = StringUtils.getCommonPrefix("weakness", desc);
        String f = StringUtils.substringAfter(desc, prefix);
        if (StringUtils.startsWithIgnoreCase("POISON", f)) {
          factor = Factor.POISON;
        }
        else {
          for (Map.Entry<String, Element> e : ELEMENTS.entrySet()) {
            if (StringUtils.startsWithIgnoreCase(e.getKey(), f)) {
              factor = e.getValue();
            }
          }

          if (factor == null) {
            throw new StructureException("Cannot parse weakness factor", f);
          }
        }

        effect = factor.weakness(magnitude);
      }
      else if (StringUtils.startsWithIgnoreCase("DRAIN", desc)) {
        effect = Factor.MAGIC.drain(magnitude);
      }
      else {
        for (Map.Entry<String, Element> e : ELEMENTS.entrySet()) {
          if (StringUtils.startsWithIgnoreCase(e.getKey(), desc)) {
            effect = e.getValue().damage(magnitude);
          }
        }

        if (effect == null) {
          throw new StructureException("Cannot parse effect type", desc);
        }
      }

      String duration = matcher.group(3);
      if (StringUtils.isNotBlank(duration)) {
        effect = effect.forSecs(Integer.parseInt(duration));
      }

      effects.add(effect);
    }

    return effects;
  }

  private static final Map<String, Source> SOURCES = ImmutableMap.of(
    "ARROW", ARROW,
    "BOW", BOW,
    "MELEE", MELEE,
    "POISON", POISON,
    "SPELL", SPELL,
    "STAFF", STAFF
  );

  private static final Map<String, Element> ELEMENTS = ImmutableMap.of(
    "FIRE", Factor.FIRE,
    "FROST", Factor.FROST,
    "MAGIC", Factor.MAGIC,
    "SHOCK", Factor.SHOCK
  );

  private static final Pattern EFFECT_PATTERN = Pattern.compile("(\\d+)([a-zA-Z]+)(?:(\\d+)s?)?");

}
