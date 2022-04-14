package eu.goodlike.oblivion.calc;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static eu.goodlike.oblivion.calc.Element.MAGIC;
import static eu.goodlike.oblivion.calc.SpellEffect.Type.DAMAGE;
import static eu.goodlike.oblivion.calc.SpellEffect.Type.MODIFY;

public final class Target {

  public BigDecimal healthRemaining() {
    return health <= 0 ? DEAD : format(health);
  }

  public BigDecimal damageTaken() {
    return format(maxHealth - health);
  }

  public void addModifier(Element.Modifier modifier) {
    modifiers.put(modifier.element(), modifier);
  }

  public void hit(double dmg, Element... elements) {
    double actualDmg = applyModifiers(dmg, elements);
    this.health -= actualDmg;
  }

  public void spellHit(int spellId, SpellEffect... effects) {
    ListMultimap<Element, Element.Modifier> spellModifiers = ArrayListMultimap.create();

    for (SpellEffect effect : effects) {
      if (DAMAGE == effect.type()) {
        hit(effect.magnitude(), MAGIC, effect.element());
      }
      if (MODIFY == effect.type()) {
        double effectiveMagnitude = applyModifiers(effect.magnitude(), MAGIC);
        Element.Modifier modifier = effect.element().new Modifier(effectiveMagnitude);

        Element.Modifier previousEffect = spellEffects.put(effect.toId(spellId), modifier);
        if (previousEffect != null) {
          modifiers.remove(effect.element(), previousEffect);
        }

        spellModifiers.put(effect.element(), modifier);
      }
    }

    modifiers.putAll(spellModifiers);
  }

  public Target(int maxHealth, Element.Modifier... baseModifiers) {
    this.maxHealth = maxHealth;
    this.health = maxHealth;

    this.modifiers = ArrayListMultimap.create();
    Stream.of(baseModifiers).forEach(this::addModifier);

    this.spellEffects = new HashMap<>();
  }

  private double applyModifiers(double magnitude, Element... elements) {
    return applyModifiers(magnitude, ImmutableSet.copyOf(elements));
  }

  private double applyModifiers(double magnitude, Set<Element> elements) {
    double modifiedMagnitude = magnitude;
    for (Element element : elements) {
      double percentEffect = Element.effect(modifiers.get(element));
      modifiedMagnitude *= percentEffect;
      modifiedMagnitude /= 100;
    }
    return modifiedMagnitude;
  }

  private double health;

  private final int maxHealth;
  private final ListMultimap<Element, Element.Modifier> modifiers;
  private final Map<SpellEffect.Id, Element.Modifier> spellEffects;

  private static BigDecimal format(double b) {
    return BigDecimal.valueOf(b).setScale(2, RoundingMode.UP);
  }

  private static final BigDecimal DEAD = format(0);

}
