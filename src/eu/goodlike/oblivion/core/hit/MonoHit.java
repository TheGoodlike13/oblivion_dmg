package eu.goodlike.oblivion.core.hit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Method;

import java.util.Iterator;
import java.util.List;

public final class MonoHit implements Hit {

  @Override
  public Method getMethod(EffectText effect) {
    return Iterables.contains(this, effect) ? method : null;
  }

  @Override
  public Effect.Id getId(EffectText effect) {
    return Iterables.contains(this, effect) ? method.toId(this, effect.getType()) : null;
  }

  @Override
  public Iterator<EffectText> iterator() {
    return effects.iterator();
  }

  public MonoHit(String sourceName, Method method, List<EffectText> effects) {
    this.sourceName = sourceName;
    this.method = method;
    this.effects = ImmutableList.copyOf(effects);
  }

  private final String sourceName;
  private final Method method;
  private final List<EffectText> effects;

  @Override
  public String toString() {
    return sourceName;
  }

}
