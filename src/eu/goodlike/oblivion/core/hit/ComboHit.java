package eu.goodlike.oblivion.core.hit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Method;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

public final class ComboHit implements Hit {

  @Override
  public Method getMethod(EffectText effect) {
    return get("method", hit -> hit.getMethod(effect));
  }

  @Override
  public Effect.Id getId(EffectText effect) {
    return get("effect id", hit -> hit.getId(effect));
  }

  @Override
  public Iterator<EffectText> iterator() {
    return Iterables.concat(hits).iterator();
  }

  public ComboHit(Hit... hits) {
    this(Arrays.asList(hits));
  }

  public ComboHit(List<Hit> hits) {
    this.hits = ImmutableList.copyOf(hits);
  }

  private final List<Hit> hits;

  private <T> T get(String valueDesc, Function<Hit, T> hitMapper) {
    return hits.stream()
      .map(hitMapper)
      .filter(Objects::nonNull)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("Cannot get " + valueDesc + ": effect is not from this hit!"));
  }

  @Override
  public String toString() {
    return hits.stream()
      .map(Hit::toString)
      .collect(joining(" + "));
  }

}
