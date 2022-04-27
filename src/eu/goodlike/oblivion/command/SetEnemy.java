package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.StructureException;

import static eu.goodlike.oblivion.Arena.THE_ARENA;

public final class SetEnemy extends BaseCommand {

  @Override
  protected void performTask() {
    args().forEach(this::identify);
    double hp = StructureException.doubleOrThrow(this.hp, "enemy hp");
    THE_ARENA.setEnemy(label, new Enemy(hp));
  }

  private String label = "enemy";
  private String hp = "";

  private void identify(String input) {
    if (input.startsWith("@")) {
      label = input.substring(1);
    }
    else {
      hp = input;
    }
  }

}
