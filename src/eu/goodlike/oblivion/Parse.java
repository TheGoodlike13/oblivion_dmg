package eu.goodlike.oblivion;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.StructureException;
import eu.goodlike.oblivion.parse.ParseEffector;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static eu.goodlike.oblivion.core.Effector.Factory.ALL_IN_ORDER;
import static eu.goodlike.oblivion.core.Factor.FIRE;
import static eu.goodlike.oblivion.core.Factor.FROST;
import static eu.goodlike.oblivion.core.Factor.MAGIC;
import static eu.goodlike.oblivion.core.Factor.SHOCK;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.getCommonPrefix;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.gradle.internal.impldep.org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Parses various inputs into usable objects.
 * Input must be a prefix of a name of a known object.
 * For example, POISON can be indicated by any of "p", "po", "POISON", etc.
 * <p/>
 * In cases where prefixes match, alphabetical order should take precedence.
 * For example: FROST can only be matched by "fr", as "f" will be matched by FIRE first.
 */
public final class Parse {

  /**
   * Checks all the values in given order.
   * The first value whose {@link #toString} has the given prefix is returned, if any.
   */
  public static <T> Optional<T> firstMatch(String prefix, Iterable<T> values) {
    if (isNotBlank(prefix)) {
      for (T value : values) {
        if (startsWithIgnoreCase(value.toString(), prefix)) {
          return Optional.of(value);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Checks enums values in natural order.
   * The first value whose {@link Enum#name()} has the given prefix is returned, if any.
   * Both the name and prefix ignore all '_' symbols when matching.
   */
  public static <E extends Enum<E>> Optional<E> firstMatch(String prefix, Class<E> enumClass) {
    Iterable<EnumMatch<E>> values = Stream.of(enumClass.getEnumConstants()).map(EnumMatch::new)::iterator;
    return firstMatch(remove(prefix, "_"), values)
      .map(EnumMatch::getValue);
  }

  /**
   * Parses a line of generic user input.
   * It is split on regions of any whitespace.
   */
  public static String[] line(String input) {
    return split(input.trim().toLowerCase());
  }

  public static ParseEffector.Mode mode(String input) {
    return firstMatch(input, ParseEffector.Mode.class)
      .orElseThrow(() -> new StructureException("Unknown parse mode reference", input));
  }

  public static Factor factor(String input) {
    return firstMatch(input, Factor.ALL)
      .orElseThrow(() -> new StructureException("Unknown factor reference", input));
  }

  public static Element element(String input) {
    return firstMatch(input, ELEMENTS)
      .orElseThrow(() -> new StructureException("Unknown element reference", input));
  }

  public static Category<?> category(String input) {
    return firstMatch(input, ALL_IN_ORDER)
      .orElseThrow(() -> new StructureException("Unknown category reference", input));
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

  public static List<EffectText> effects(Iterable<String> effects) {
    return Streams.stream(effects).map(Parse::effect).collect(toList());
  }

  /**
   * Wrapper for parsing logic.
   * Takes input in some format & parses it into label and other params.
   * Finally, allows lazy parsing of value via {@link #getValue}.
   */
  public interface Input<T> extends NamedValue<T> {
    /**
     * Caches the result of parsing the input.
     * Returns the result of caching the value.
     * <p/>
     * As caches have their own labeling logic, the label and even the value may become different.
     */
    NamedValue<T> thenCache();
  }

  private Parse() {
    throw new AssertionError("Do not instantiate! Use static fields/methods!");
  }

  private static final List<Element> ELEMENTS = ImmutableList.of(MAGIC, FIRE, FROST, SHOCK);

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
      return isBlank(duration) ? DEFAULT_DURATION : Integer.parseInt(duration);
    }

    private static final Pattern EFFECT_PATTERN = Pattern.compile("(\\d+)([a-zA-Z]+)(?:(\\d+)s?)?");
    private static final int DEFAULT_DURATION = 1;
  }

  private static final class EnumMatch<E extends Enum<E>> {
    public E getValue() {
      return value;
    }

    public EnumMatch(E value) {
      this.value = value;
    }

    private final E value;

    @Override
    public String toString() {
      return remove(value.name(), '_');
    }
  }

}
