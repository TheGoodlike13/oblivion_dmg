package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.NamedValue;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.parse.AsEnemy;

import static eu.goodlike.oblivion.Arena.THE_ARENA;
import static eu.goodlike.oblivion.Global.ENEMIES;

public final class SetEnemy extends BaseCommand {

  @Override
  protected void performTask() {
    if (input(1).startsWith("$")) {
      findEnemyByRef(input(1).substring(1));
    }
    else {
      parseNewEnemy();
    }
    THE_ARENA.setEnemy(label, enemy);
  }

  private String label;
  private Enemy enemy;

  private void findEnemyByRef(String ref) {
    label = ref;
    enemy = ENEMIES.get(ref);
  }

  private void parseNewEnemy() {
    NamedValue<Enemy> e = new AsEnemy(inputs).inCache();
    label = e.getName();
    enemy = e.getValue();
  }

}
