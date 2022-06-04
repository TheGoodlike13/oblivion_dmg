package eu.goodlike.oblivion.core.effector;

import com.google.common.collect.Streams;
import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;

import java.util.Iterator;

import static eu.goodlike.oblivion.core.Effector.Factory.SPELL;

public final class Spell extends Power {

  @Override
  public Category<?> getCategory() {
    return SPELL;
  }

  @Override
  public Effector copy(String label) {
    return new Spell(label, this);
  }

  @Override
  public Iterator<EffectText> iterator() {
    return Streams.stream(super.iterator()).map(EffectText::scale).iterator();
  }

  public Spell(String label, Iterable<EffectText> effects) {
    super(label, effects);
  }

}
