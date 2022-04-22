package eu.goodlike.oblivion.core;

import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Entity which carries multiple effects to be applied on hit.
 * In-game this would be a single weapon, arrow, spell or anything else with multiple effects.
 * Carriers can be combined to create actual {@link Hit}s, like {@link Source#BOW} + {@link Source#ARROW}.
 * <p/>
 * This class should only be extended to impose limits by {@link #equals(Object)}, if needed.
 * This can allow to identify duplicate carriers which otherwise would not be allowed.
 */
public class Carrier implements Iterable<EffectText>, Comparable<Carrier> {

  /**
   * Strategy for determining uniqueness of an effect.
   */
  public interface IdStrategy {
    /**
     * When hitting a target multiple times with the same carrier (e.g. spell), effects do not stack, but override
     * each other instead (usually).
     * The id from this method allows us to identify effects which should be overriden rather than stacked.
     *
     * @param carrier carrier which produced the given effect
     * @param effect effect which we must uniquely identify
     * @return id which uniquely identifies the given effect
     */
    Effect.Id toId(Carrier carrier, EffectText effect);
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
    return strategy.toId(this, effect);
  }

  public Carrier copy(String copyName) {
    return new Carrier(source, copyName, method, strategy, effects);
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
