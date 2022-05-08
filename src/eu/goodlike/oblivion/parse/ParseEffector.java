package eu.goodlike.oblivion.parse;

import eu.goodlike.oblivion.NamedValue;
import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.StructureException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static eu.goodlike.oblivion.Global.EFFECTORS;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Parses effector input.
 * Accepts any amount of inputs.
 * Treats them as a single effector.
 * <p/>
 * Inputs with ':' prefix are treated as labels.
 * Only last label is considered, others ignored.
 * This input is optional.
 * <p/>
 * Inputs with '+' prefix are treated as categories.
 * Only last category is considered, others ignored.
 * This input is required.
 * In practice, there should be only a single category at the beginning of input.
 * <p/>
 * All other inputs are treated as effector effects.
 * They must be parsable.
 * <p/>
 * Cached nameless effectors will receive a name from the cache.
 * Thus both the label and the effector can be different as a result of {@link #thenCache}.
 */
public final class ParseEffector extends BaseParseInput<Effector> {

  @Override
  protected Effector parse() {
    Category<?> category = Parse.category(this.source);
    List<EffectText> effects = Parse.effects(this.effects);
    return category.create(label, effects);
  }

  @Override
  public NamedValue<Effector> thenCache() {
    return EFFECTORS.put(label, getValue(), Effector::copy);
  }

  public ParseEffector(String input) {
    this(Parse.line(input));
  }

  public ParseEffector(String[] inputs) {
    this(Stream.of(inputs));
  }

  public ParseEffector(Stream<String> inputs) {
    inputs.forEach(this::identify);

    if (source == null) {
      throw new StructureException("Missing category param", isBlank(label) ? effects : label);
    }
  }

  private String source;
  private final List<String> effects = new ArrayList<>();

  private void identify(String input) {
    if (input.startsWith(":")) {
      label = input.substring(1);
    }
    else if (input.startsWith("+")) {
      source = input.substring(1);
    }
    else {
      effects.add(input);
    }
  }

}
