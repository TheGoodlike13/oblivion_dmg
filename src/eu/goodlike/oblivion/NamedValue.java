package eu.goodlike.oblivion;

/**
 * Tuple of a value and its name.
 * Used in various contexts to return both.
 * <p/>
 * Both name and value returned by the methods should always be the same.
 */
public interface NamedValue<T> {

  String getName();
  T getValue();

}
