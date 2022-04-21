package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class Carrier implements Iterable<EffectText>, Comparable<Carrier> {

  public interface IdStrategy {
    Effect.Id toId(String carrierName, Effect.Type type);
  }

  public Source getSource() {
    return source;
  }

  public String getName() {
    return name;
  }

  public Method getMethod() {
    return method;
  }

  public Effect.Id toId(EffectText effect) {
    return strategy.toId(name, effect.getType());
  }

  @Override
  public Iterator<EffectText> iterator() {
    return effects.iterator();
  }

  @Override
  public int compareTo(Carrier other) {
    return ORDER.compare(this, other);
  }

  public Carrier(Source source,
                 String name,
                 Method method,
                 IdStrategy strategy,
                 List<EffectText> effects) {
    this.source = source;
    this.name = name;
    this.method = method;
    this.strategy = strategy;
    this.effects = ImmutableList.copyOf(effects);
  }

  private final Source source;
  private final String name;
  private final Method method;
  private final Carrier.IdStrategy strategy;
  private final List<EffectText> effects;

  private static final Comparator<Carrier> ORDER = Comparator.comparing(Carrier::getSource);

}
