package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.StructureException;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class Cache<T> {

  public String put(T t) {
    return put(null, t);
  }

  public String put(String label, T t) {
    String ref = ensureRef(label);
    if (cache.putIfAbsent(ref, t) != null) {
      throw new StructureException("Name already in use", ref);
    }
    return ref;
  }

  public T put(String label, Function<? super String, ? extends T> refDependant) {
    String ref = ensureRef(label);
    T t = refDependant.apply(ref);
    if (cache.putIfAbsent(ref, t) != null) {
      throw new StructureException("Name already in use", ref);
    }
    return t;
  }

  public T get(String ref) {
    T t = cache.get(ref);
    if (t == null) {
      throw new StructureException("Nothing with name", ref);
    }
    return t;
  }

  public void reset(Consumer<String> parser, String... prepFiles) {
    cache.clear();
    counter.set(0);

    for (String prepFile : prepFiles) {
      init(prepFile, parser);
    }
  }

  private final Map<String, T> cache = new HashMap<>();
  private final AtomicInteger counter = new AtomicInteger(0);

  private String ensureRef(String label) {
    return isBlank(label) ? nextRef() : label;
  }

  private String nextRef() {
    return String.valueOf(counter.incrementAndGet());
  }

  private void init(String prepFile, Consumer<String> parser) {
    try (InputStream file = Cache.class.getClassLoader().getResourceAsStream(prepFile)) {
      if (file != null) {
        parse(file, parser);
        return;
      }
    } catch (IOException | UncheckedIOException e) {
      Write.line("Exception: " + e);
    }
    Write.line("Failed to load prepared file '" + prepFile + "'. Please check config directory or settings.");
  }

  private void parse(InputStream file, Consumer<String> parser) {
    new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8))
      .lines()
      .filter(StringUtils::isNotBlank)
      .filter(line -> !line.startsWith("#"))
      .forEach(parser);
  }

}
