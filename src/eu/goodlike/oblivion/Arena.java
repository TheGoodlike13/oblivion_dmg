package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.global.Write;

import java.util.ArrayList;
import java.util.List;

public final class Arena {

  public void setEnemy(String label, Enemy enemy) {
    this.enemy = enemy;
    Write.line("Today you'll be hitting " + label + " with " + enemy.healthRemaining() + " hp.");
  }

  public void addHit(Hit hit) {
    hits.add(hit);
    Write.line("Hit #" + hits.size() + ": " + hit);
  }

  public void lowerTheGates() {
    refresh();
  }

  public Arena() {
    reset();
  }

  private Enemy enemy;
  private List<Hit> hits;

  private void reset() {
    enemy = null;
    hits = new ArrayList<>();
  }

  private void refresh() {
    enemy.resurrect();
    hits = new ArrayList<>();
  }

}
