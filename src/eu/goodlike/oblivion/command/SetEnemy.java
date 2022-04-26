package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.StructureException;

import static eu.goodlike.oblivion.Arena.THE_ARENA;

public final class SetEnemy extends BaseCommand {

  @Override
  protected void performTask() {
    identifyArgs();
    double hp = parseHp();
    THE_ARENA.setEnemy(label, new Enemy(hp));
  }

  private void identifyArgs() {
    args().forEach(this::identify);
  }

  private void identify(String input) {
    if (input.startsWith("@")) {
      label = input.substring(1);
    }
    else {
      hp = input;
    }
  }

  private double parseHp() {
    try {
      return Double.parseDouble(hp);
    }
    catch (NumberFormatException e) {
      throw new StructureException("Cannot parse enemy hp <" + hp + ">", e);
    }
  }

  private String label = "an enemy";
  private String hp = "";

}
