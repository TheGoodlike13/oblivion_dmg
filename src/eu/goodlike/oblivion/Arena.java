package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Hit;

import java.util.ArrayList;
import java.util.List;

import static eu.goodlike.oblivion.Global.Settings.TICK;

public final class Arena {

  public void reset() {
    hits = new ArrayList<>();
    label = null;
    enemy = null;
  }

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

  public Arena() {
    reset();
  }

  private List<Hit> hits;
  private String label;
  private Enemy enemy;

  private double duration;

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
    performHits();
    awaitEffectExpiration();
    writeObituary();
    refresh();
  }

  private void performHits() {
    for (Hit hit : hits) {
      enemy.hit(hit);
      combatLog("You hit with " + hit);

      checkEnemyStatus();
    }
  }

  private void awaitEffectExpiration() {
    while (enemy.isAlive() && enemy.isAffected()) {
      enemy.tick();
      duration += TICK;

      checkEnemyStatus();
    }

    duration += enemy.resolve();
    combatLog("All effects have expired.");
  }

  private void writeObituary() {
    if (enemy.isAlive()) {
      Write.line(String.format("The %s has survived %.1f damage (%.1f hp left).", label, enemy.damageTaken(), enemy.healthRemaining()));
    }
    else {
      Write.line(String.format("The %s took a total of %.1f damage (%.1f overkill).", label, enemy.damageTaken(), enemy.overkill()));
    }
  }

  private void checkEnemyStatus() {
    if (!enemy.isAlive()) {
      combatLog("The " + label + " has died.");
    }
  }

  private void combatLog(String text) {
    Write.line(String.format("%06.3f " + text, duration));
  }

  private void refresh() {
    Write.separator();

    hits = new ArrayList<>();
    enemy.resurrect();
    announceOpponent();
  }

  private void announceOpponent() {
    Write.line("You face the " + label + " (" + enemy.healthRemaining() + " hp).");
    writeResists();
  }

  private void writeResists() {
    for (Factor factor : Factor.ALL) {
      double multiplier = enemy.getMultiplier(factor);
      if (multiplier != 1) {
        Write.line(String.format("%-6s x%.2f", factor, multiplier));
      }
    }
  }

}
