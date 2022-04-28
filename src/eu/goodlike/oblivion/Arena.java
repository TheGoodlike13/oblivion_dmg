package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Hit;

import java.util.ArrayList;
import java.util.List;

public final class Arena {

  public static final Arena THE_ARENA = new Arena();

  public void reset() {
    hits = new ArrayList<>();
    label = null;
    enemy = null;
  }

  public void setEnemy(String label, Enemy enemy) {
    this.label = label.replace('_', ' ');
    this.enemy = enemy;
    announceOpponent();
  }

  public void addHit(Hit hit) {
    hits.add(hit);
    Write.line("Next hit: " + hit);
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
      duration += Settings.TICK;

      checkEnemyStatus();
    }

    duration += enemy.resolve();
    combatLog("All effects have expired.");
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
    printResists();
  }

  private void printResists() {
    for (Factor factor : Factor.ALL) {
      double multiplier = enemy.getMultiplier(factor);
      if (multiplier != 1) {
        Write.line(String.format("%-6s x%.2f", factor, multiplier));
      }
    }
  }

}
