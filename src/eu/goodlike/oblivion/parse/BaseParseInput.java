package eu.goodlike.oblivion.parse;

import eu.goodlike.oblivion.Parse;

/**
 * Common logic for parsers as named values.
 * Ensures the object is parsed exactly once, unless an error occurs.
 */
public abstract class BaseParseInput<T> implements Parse.Input<T> {

  protected abstract T parse();

  @Override
  public final String getName() {
    return label;
  }

  @Override
  public final T getValue() {
    if (result == null) {
      result = parse();
    }
    return result;
  }

  protected String label;
  private T result;

}
