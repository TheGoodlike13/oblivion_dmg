package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Target;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static eu.goodlike.oblivion.Global.Settings.TICK;

/**
 * The calculator for using spells/attacks on an enemy.
 * <p/>
 * Writes down all inputs and outcomes.
 */
public final class Arena implements Enemy.Observer, Target {

  public void setEnemy(NamedValue<Enemy> enemy) {
    this.label = enemy.getName().replace('_', ' ');
    this.enemy = enemy.getValue();
    announceOpponent();
  }

  public void addHit(NamedValue<Hit> hit) {
    hits.add(hit.getValue());
    Write.line("[#" + hit.getName() + "] Next hit: " + hit.getValue());
  }

  public void removeLastHit() {
    if (hits.isEmpty()) {
      Write.line("No hits to remove.");
    }
    else {
      Hit removed = hits.remove(hits.size() - 1);
      Write.line("Removed hit: " + removed);
    }
  }

  public void lowerTheGates() {
    if (ready()) {
      fight();
    }
  }

  public void refresh() {
    Write.separator();

    hits = new ArrayList<>();
    totalDamage.clear();
    totalDamage2.clear();
    totalDamageDead.clear();
    nextDump = 0.25;
    if (enemy != null) {
      enemy.updateLevel();
      announceOpponent();
    }
  }

  public void reset() {
    hits = new ArrayList<>();
    label = null;
    enemy = null;
    totalDamage.clear();
    totalDamage2.clear();
    totalDamageDead.clear();
    nextDump = 0.25;
  }

  private Target actual;
  private Effect.Id id;
  private boolean isTicking;
  private boolean isExpired;

  private double nextDump = 0.25;

  private final Map<Effect.Id, Double> totalDamage = new LinkedHashMap<>();
  private final Map<Effect.Id, Double> totalDamage2 = new LinkedHashMap<>();
  private final Map<Effect.Id, Double> totalDamageDead = new LinkedHashMap<>();


  @Override
  public Target observing(Target actual) {
    this.isTicking = false;
    this.actual = actual;
    return this;
  }

  @Override
  public void tick() {
    this.isTicking = true;

    if (duration >= nextDump) {
      nextDump += 0.25;
      dump();
    }
    duration += TICK;
  }

  private void dump() {
    totalDamage.forEach((id, d) -> combatLog(String.format("Took %s %.2f", id, d)));
    totalDamage.clear();
  }

  @Override
  public void next(Effect.Id id) {
    this.isExpired = false;
    this.id = id;
  }

  @Override
  public void markExpired() {
    this.isExpired = true;
    if (isTicking) {
      combatLog("Expired " + id);
    }
  }

  @Override
  public void modifyResist(Factor factor, double percent) {
    actual.modifyResist(factor, percent);
    if (!isTicking) {
      combatLog(String.format("%s %s %.2f", isExpired ? "Replaced" : "Added", id, percent));
    }
  }

  @Override
  public void damage(double dmg) {
    if (enemy.isAlive()) {
      totalDamage.merge(id, dmg, Double::sum);
      totalDamage2.merge(id, dmg, Double::sum);
    }
    else {
      totalDamageDead.merge(id, dmg, Double::sum);
    }
    actual.damage(dmg);
    checkPossibleDeath();
  }

  @Override
  public void drain(double hp) {
    actual.drain(hp);
    if (!isTicking) {
      combatLog(String.format("%s %s %.2f", isExpired ? "Replaced" : "Added", id, hp));
      checkPossibleDeath();
    }
  }

  public Arena() {
    reset();
  }

  private List<Hit> hits;
  private String label;
  private Enemy enemy;

  private double duration;
  private boolean isConfirmedDead;

  private boolean ready() {
    if (hits.isEmpty()) {
      String beholdee = enemy == null ? "void" : label;
      Write.line("You stare at the " + beholdee + ".");
      Write.line("The " + beholdee + " stares at you.");
      Write.line("How about casting some spells?");
      return false;
    }
    if (enemy == null) {
      Write.line("All your hits land on the wall.");
      Write.line("Good job.");
      Write.line("How about picking an enemy?");
      return false;
    }
    return true;
  }

  private void fight() {
    duration = 0;
    isConfirmedDead = false;
    Write.line("Lower the gates!");

    try {
      performHits();
      awaitEffectExpiration();
      writeObituary();
    }
    finally {
      refresh();
    }
  }

  private void performHits() {
    for (Hit hit : hits) {
      enemy.hit(hit, this);
    }
  }

  private void awaitEffectExpiration() {
    while (enemy.isAlive() && enemy.isAffected()) {
      enemy.tick(this);
    }

    enemy.resolve(this);
  }

  private void writeObituary() {
    if (enemy.isAlive()) {
      Write.line("The %s has survived %.1f damage (%.1f hp left).", label, enemy.damageTaken(), enemy.healthRemaining());
    }
    else {
      Write.line("The %s took a total of %.1f damage (%.1f overkill).", label, enemy.damageTaken(), enemy.overkill());
    }
  }

  private void checkPossibleDeath() {
    if (!isConfirmedDead && !enemy.isAlive()) {
      isConfirmedDead = true;
      dump();
      combatLog("The " + label + " has died.");
    }
  }

  private void combatLog(String text) {
    Write.line("%06.3f %s", duration, text);
  }

  private void announceOpponent() {
    Write.line("You face the " + label + " (" + enemy.healthRemaining() + " hp).");
    writeResists();
  }

  private void writeResists() {
    for (Factor factor : Factor.ALL) {
      double multiplier = enemy.getMultiplier(factor);
      if (multiplier != 1) {
        Write.line("%-6s x%.2f", factor, multiplier);
      }
    }
  }

}
