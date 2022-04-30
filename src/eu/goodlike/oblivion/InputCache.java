package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.StructureException;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Cache for results of parsing some input.
 */
public final class InputCache<T> {

  /**
   * Checks if the label is suitable for this cache.
   * If so, returns it.
   * If not, replaces it with a suitable dynamic digit reference.
   * <p/>
   * This check does not verify if the label is already in use as reference.
   * You will have to try to put an object into the cache to find out.
   */
  public String ensureRef(String label) {
    return isBlank(label) ? nextRef() : label;
  }

  /**
   * Puts the value into this cache.
   *
   * @return value and its generated reference as name
   */
  public NamedValue<T> put(T value) {
    return put(null, value);
  }

  /**
   * Puts the value into this cache under given label or a suitable replacement.
   *
   * @return value and its label as name
   * @throws StructureException if the label is already taken
   */
  public NamedValue<T> put(String label, T value) {
    String ref = ensureRef(label);
    if (cache.putIfAbsent(ref, value) != null) {
      throw new StructureException("Name already in use", ref);
    }
    return new InputCache.Entry<>(ref, value);
  }

  /**
   * Returns value by matching the given prefix to all references in this cache.
   * For multiple matches, the returned value is alphabetic.
   *
   * @throws StructureException if no ref matched the prefix
   */
  public NamedValue<T> getCached(String prefix) {
    String ref = Parse.firstMatch(prefix, cache.keySet())
      .orElseThrow(() -> new StructureException("Nothing matches", prefix));

    T value = cache.get(ref);
    return new InputCache.Entry<>(ref, value);
  }

  /**
   * Clears the cache and resets the dynamic reference counter.
   * Then parses all given files as resources containing prepared data.
   *
   * @throws IllegalStateException if any files were given, but this cache has no parser factory
   */
  public void reset(String... prepFiles) {
    cache.clear();
    counter.set(0);

    for (String prepFile : prepFiles) {
      parseResource(prepFile);
    }
  }

  /**
   * Creates an input cache without a parser factory.
   * This cache is for entities without any prepared data.
   */
  public InputCache() {
    this(null);
  }

  public InputCache(Function<String, Parse.Input<T>> parserFactory) {
    this.parserFactory = parserFactory;

    this.cache = new TreeMap<>();
    this.counter = new AtomicInteger(0);
  }

  private final Function<String, Parse.Input<T>> parserFactory;

  private final Map<String, T> cache;
  private final AtomicInteger counter;

  private String nextRef() {
    return String.valueOf(counter.incrementAndGet());
  }

  private void parseResource(String prepFile) {
    ensureParserFactory();
    try (InputStream file = InputCache.class.getClassLoader().getResourceAsStream(prepFile)) {
      if (file != null) {
        parse(file);
        return;
      }
    } catch (Exception e) {
      Write.line("Exception: " + e);
    }
    Write.line("Failed to load prepared file '" + prepFile + "'. Please check config directory or settings.");
    Write.line("State:");
    for (Map.Entry<String, T> e : cache.entrySet()) {
      Write.line(e.getKey() + ": " + e.getValue());
    }
  }

  private void ensureParserFactory() {
    if (parserFactory == null) {
      throw new IllegalStateException("Cannot parse prepared file: no parser factory has been provided for this cache.");
    }
  }

  private void parse(InputStream file) {
    new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8))
      .lines()
      .filter(StringUtils::isNotBlank)
      .filter(line -> !line.startsWith("#"))
      .forEach(this::parse);
  }

  private void parse(String line) {
    parserFactory.apply(line).thenCache();
  }

  private static final class Entry<T> implements NamedValue<T> {
    @Override
    public String getName() {
      return ref;
    }

    @Override
    public T getValue() {
      return value;
    }

    public Entry(String ref, T value) {
      this.ref = ref;
      this.value = value;
    }

    private final String ref;
    private final T value;
  }

}
