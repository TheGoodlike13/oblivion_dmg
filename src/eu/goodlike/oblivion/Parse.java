package eu.goodlike.oblivion;

import com.google.common.collect.ImmutableList;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.StructureException;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.goodlike.oblivion.core.Factor.FIRE;
import static eu.goodlike.oblivion.core.Factor.FROST;
import static eu.goodlike.oblivion.core.Factor.MAGIC;
import static eu.goodlike.oblivion.core.Factor.POISON;
import static eu.goodlike.oblivion.core.Factor.SHOCK;
import static org.apache.commons.lang3.StringUtils.getCommonPrefix;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.substringAfter;

/**
 * Parses various inputs into usable objects.
 * Input must be a prefix of a name of a known object.
 * For example, POISON can be indicated by any of "p", "po", "POISON", etc.
 * <p/>
 * In cases where prefixes match, alphabetical order should take precedence.
 * For example: FROST can only be matched by "fr", as "f" will be matched by FIRE first.
 */
public final class Parse {

  public static Factor factor(String input) {
    return startsWithIgnoreCase(POISON.toString(), input)
      ? POISON
      : Parse.element(input);
  }

  public static Element element(String input) {
    return matchFirst(input, ELEMENTS)
      .orElseThrow(() -> new StructureException("Unknown factor/element reference", input));
  }

  public static Source source(String input) {
    return matchFirst(input, Source.ALL_IN_ORDER)
      .orElseThrow(() -> new StructureException("Unknown source reference", input));
  }

  /**
   * Parses effect input.
   * Example: 100d10s -> DRAIN LIFE 100 for 10 seconds.
   * <p/>
   * Expected format is (magnitude)(type)(optional duration).
   * <p/>Magnitude is a positive number.
   * <p/>Optional duration is a positive number followed by optional 's'.
   *     Default duration is 1s.
   * <p/>Type describes the effect, such as:
   *     1) Weakness or resist followed by a factor.
   *     2) 'Drain' (life).
   *     3) Element (damage).
   */
  public static EffectText effect(String input) {
    return new EffectParser(input).parse();
  }

  private Parse() {
    throw new AssertionError("Do not instantiate! Use static fields/methods!");
  }

  private static final List<Element> ELEMENTS = ImmutableList.of(MAGIC, FIRE, FROST, SHOCK);

  private static <T> Optional<T> matchFirst(String input, List<T> values) {
    for (T value : values) {
      if (startsWithIgnoreCase(value.toString(), input)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }

  private static final class EffectParser {
    public EffectText parse() {
      return getBaseEffect().forSecs(duration());
    }

    public EffectParser(String input) {
      this.matcher = EFFECT_PATTERN.matcher(input.toLowerCase());
      if (!matcher.matches()) {
        throw new StructureException("Invalid effect format", input);
      }
    }

    private final Matcher matcher;

    private EffectText getBaseEffect() {
      if (isWeakness()) {
        return Parse.factor(toMod("weakness")).weakness(magnitude());
      }

      if (isResist()) {
        return Parse.factor(toMod("resist")).resist(magnitude());
      }

      return isDrain()
        ? MAGIC.drain(magnitude())
        : Parse.element(type()).damage(magnitude());
    }

    private boolean isWeakness() {
      return type().startsWith("w");
    }

    private boolean isResist() {
      return type().startsWith("r");
    }

    private String type() {
      return matcher.group(2);
    }

    private String toMod(String modType) {
      String prefix = getCommonPrefix(modType, type());
      return substringAfter(type(), prefix);
    }

    private boolean isDrain() {
      return "drain".startsWith(type());
    }

    private int magnitude() {
      return Integer.parseInt(matcher.group(1));
    }

    private int duration() {
      String duration = matcher.group(3);
      return isBlank(duration) ? 1 : Integer.parseInt(duration);
    }

    private static final Pattern EFFECT_PATTERN = Pattern.compile("(\\d+)([a-zA-Z]+)(?:(\\d+)s?)?");
  }

}
