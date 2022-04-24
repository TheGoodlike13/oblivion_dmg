package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.Hit;

import java.util.ArrayList;
import java.util.List;

public final class Arena {

  public void setEnemy(Enemy enemy) {
    this.enemy = enemy;
  }

  public void addHit(Hit hit) {
    hits.add(hit);
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
