package eu.goodlike.oblivion.parse;

import eu.goodlike.oblivion.NamedValue;
import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.StructureException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static eu.goodlike.oblivion.Global.CARRIERS;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Parses carrier input.
 * Accepts any amount of inputs.
 * Treats them as a single carrier.
 * <p/>
 * Inputs with ':' prefix are treated as labels.
 * Only last label is considered, others ignored.
 * This input is optional.
 * <p/>
 * Inputs with '+' prefix are treated as sources.
 * Only last source is considered, others ignored.
 * This input is required.
 * In practice, there should be only a single source at the beginning of input.
 * <p/>
 * All other inputs are treated as carrier effects.
 * They must be parsable.
 * <p/>
 * Cached nameless carrier will receive a name from the cache.
 * Thus both the label and the carrier can be different as a result of {@link #thenCache}.
 */
public final class ParseCarrier extends BaseParseInput<Carrier> {

  @Override
  protected Carrier parse() {
    Source source = Parse.source(this.source);
    List<EffectText> effects = Parse.effects(this.effects);
    return source.create(label, effects);
  }

  @Override
  public NamedValue<Carrier> thenCache() {
    String ref = CARRIERS.ensureRef(label);
    Carrier carrier = getValue().copy(ref);
    return CARRIERS.put(ref, carrier);
  }

  public ParseCarrier(String input) {
    this(Parse.line(input));
  }

  public ParseCarrier(String[] inputs) {
    this(Stream.of(inputs));
  }

  public ParseCarrier(Stream<String> inputs) {
    inputs.forEach(this::identify);

    if (source == null) {
      throw new StructureException("Missing source param", isBlank(label) ? effects : label);
    }
  }

  private String source;
  private final List<String> effects = new ArrayList<>();

  private void identify(String input) {
    if (input.startsWith("+")) {
      source = input.substring(1);
    }
    else if (input.startsWith(":")) {
      label = input.substring(1);
    }
    else {
      effects.add(input);
    }
  }

}
