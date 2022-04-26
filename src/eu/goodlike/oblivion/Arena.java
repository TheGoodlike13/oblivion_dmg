package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.global.Settings;
import eu.goodlike.oblivion.global.Write;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.capitalize;

public final class Arena {

  public void setEnemy(String label, Enemy enemy) {
    this.label = label;
    this.enemy = enemy;
    announceOpponent();
  }

  public void addHit(Hit hit) {
    hits.add(hit);
    Write.line("Hit #" + hits.size() + ": " + hit);
  }

  public void lowerTheGates() {
    duration = 0;

    for (Hit hit : hits) {
      enemy.hit(hit);
      combatLog("You perform " + hit);
    }

    while (enemy.isAffected()) {
      enemy.tick();
      duration += Settings.TICK;

      if (!enemy.isAlive()) {
        combatLog(capitalize(label) + " has died.");
        break;
      }
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

  private void combatLog(String text) {
    Write.line(String.format("%06.3f " + text, duration));
  }

  private void reset() {
    hits = new ArrayList<>();
    label = null;
    enemy = null;
  }

  private void refresh() {
    Write.line("-----");

    hits = new ArrayList<>();
    enemy.resurrect();
    announceOpponent();
  }

  private void announceOpponent() {
    Write.line("Today you'll be hitting " + label + " with " + enemy.healthRemaining() + " hp.");
  }

}
