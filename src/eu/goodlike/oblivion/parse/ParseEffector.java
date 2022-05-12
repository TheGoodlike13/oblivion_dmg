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
import static eu.goodlike.oblivion.Global.Settings.PARSE_MODE;
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

  /**
   * Leniency for accepting duplicate effect types in an effector.
   * <p/>
   * In-game it is not possible to create spells with duplicate effect types.
   * However, some pre-built spells *cough* Enemies Explode *cough* do have this uncanny property.
   * As a result, the code itself allows duplicate effects.
   * To avoid confusion when parsing user input, an appropriate mode should be selected.
   */
  public enum Mode {
    LENIENT,  // allows duplicates for both files and users
    MIXED,    // allows duplicates for files, but not for users
    STRICT;   // forbids duplicates for both files and users

    public boolean demandsUniqueTypesFor(boolean isPrivileged) {
      return this == STRICT || this == MIXED && !isPrivileged;
    }
  }

  @Override
  protected Effector parse() {
    Category<?> category = Parse.category(this.category);
    List<EffectText> effects = Parse.effects(this.effects);
    Effector effector = category.create(label, effects);
    if (PARSE_MODE.demandsUniqueTypesFor(isPrivileged)) {
      StructureException.throwOnDuplicateEffectTypes(effector);
    }
    return effector;
  }

  @Override
  public NamedValue<Effector> thenCache() {
    return EFFECTORS.put(label, getValue(), Effector::copy);
  }

  public static ParseEffector forFile(String input) {
    return new ParseEffector(true, Stream.of(Parse.line(input)));
  }

  public static ParseEffector forUser(Stream<String> inputs) {
    return new ParseEffector(false, inputs);
  }

  private ParseEffector(boolean isPrivileged, Stream<String> inputs) {
    this.isPrivileged = isPrivileged;

    inputs.forEach(this::identify);

    if (category == null) {
      throw new StructureException("Missing category param", isBlank(label) ? effects : label);
    }
  }

  private final boolean isPrivileged;

  private String category;
  private final List<String> effects = new ArrayList<>();

  private void identify(String input) {
    if (input.startsWith(":")) {
      label = input.substring(1);
    }
    else if (input.startsWith("+")) {
      category = input.substring(1);
    }
    else {
      effects.add(input);
    }
  }

}
