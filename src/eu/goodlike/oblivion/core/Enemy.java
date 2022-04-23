package eu.goodlike.oblivion.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static eu.goodlike.oblivion.core.Factor.MAGIC;
import static eu.goodlike.oblivion.core.Source.SPELL;
import static eu.goodlike.oblivion.global.Settings.TICK;

public final class Enemy implements Target {

  public double healthRemaining() {
    return Math.max(0, health);
  }

  public double damageTaken() {
    return maxHealth - health;
  }

  public boolean isAlive() {
    return health >= BASICALLY_DEAD;
  }

  public boolean isAffected() {
    return !activeEffects.isEmpty();
  }

  public void hit(EffectText... effects) {
    hit(SPELL, effects);
  }

  public void hit(Source source, EffectText... effects) {
    hit(source.create(effects));
  }

  public void hit(Carrier monoHit) {
    hit(new Hit(monoHit));
  }

  public void hit(Hit hit) {
    Dummy dummy = new Dummy();

    for (Carrier carrier : hit) {
      for (EffectText e : carrier) {
        Effect.Id id = carrier.toId(e);
        Effect effect = e.activate(carrier.getMethod(), this);

        Effect original = activeEffects.put(id, effect);
        if (original != null) {
          original.onRemove(this);
        }

        effect.onApply(dummy);
      }
    }

    dummy.applyTo(this);
  }

  public void tick() {
    Iterator<Map.Entry<Effect.Id, Effect>> i = activeEffects.entrySet().iterator();
    while (i.hasNext()) {
      Map.Entry<Effect.Id, Effect> next = i.next();
      Effect effect = next.getValue();

      effect.onTick(this);
      if (effect.hasExpired()) {
        i.remove();
        effect.onRemove(this);
      }
    }
  }

  public void tick(double seconds) {
    double total = 0;
    while (total < seconds) {
      tick();
      total += TICK;
    }
  }

  public void resolve() {
    while (isAffected()) {
      tick();
    }
  }

  @Override
  public double getMultiplier(Factor factor) {
    return Math.max(0, getWeakness(factor) / 100);
  }

  @Override
  public void modifyResist(Factor factor, double percent) {
    double currentPc = getWeakness(factor);
    weaknessPercent.put(factor, currentPc - percent);
  }

  @Override
  public void damage(double dmg) {
    health -= dmg;
  }

  @Override
  public void drain(double hp) {
    if (isAlive()) {
      health -= hp;
    }
  }

  public Enemy(double maxHealth, EffectText... baseEffects) {
    this.maxHealth = maxHealth;
    this.health = maxHealth;

    this.weaknessPercent = new HashMap<>();

    this.activeEffects = new HashMap<>();

    Dummy dummy = new Dummy();
    for (EffectText effect : baseEffects) {
      effect.activate(MAGIC, dummy).onApply(this);
    }
    dummy.applyTo(this);
  }

  private double maxHealth;
  private double health;

  private final Map<Factor, Double> weaknessPercent;

  private final Map<Effect.Id, Effect> activeEffects;

  private double getWeakness(Factor factor) {
    return weaknessPercent.computeIfAbsent(factor, any -> NO_WEAKNESS);
  }

  private static final double NO_WEAKNESS = 100;
  private static final double BASICALLY_DEAD = 0.005;

  private static final class Dummy implements Target {
    @Override
    public void modifyResist(Factor factor, double percent) {
      allMods.put(factor, percent);
    }

    @Override
    public void damage(double dmg) {
      totalDmg += dmg;
    }

    @Override
    public void drain(double hp) {
      totalDrain += hp;
    }

    public void applyTo(Target target) {
      allMods.forEach(target::modifyResist);
      target.damage(totalDmg);
      target.drain(totalDrain);
    }

    private final ListMultimap<Factor, Double> allMods = ArrayListMultimap.create();
    private double totalDmg = 0;
    private double totalDrain = 0;
  }

}
