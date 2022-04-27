package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Enemy;
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
    this.label = label;
    this.enemy = enemy;
    announceOpponent();
  }

  public void addHit(Hit hit) {
    hits.add(hit);
    Write.line("Next hit: " + hit);
  }

  public void lowerTheGates() {
    duration = 0;

    for (Hit hit : hits) {
      enemy.hit(hit);
      combatLog("You hit with " + hit);

      checkEnemyStatus();
    }

    while (enemy.isAlive() && enemy.isAffected()) {
      enemy.tick();
      duration += Settings.TICK;

      checkEnemyStatus();
    }

    duration += enemy.resolve();
    combatLog("All effects have expired.");

    refresh();
  }

  public Arena() {
    reset();
  }

  private List<Hit> hits;
  private String label;
  private Enemy enemy;

  private double duration;

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
  }

}
