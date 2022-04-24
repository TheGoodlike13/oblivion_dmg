package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.StructureException;

public final class SetEnemy extends BaseCommand {

  @Override
  protected void performTask() {
    double hp = parseHp();
    arena.setEnemy(new Enemy(hp));
    Write.line("Today you'll be hitting an enemy with " + hp + " hp.");
  }

  private double parseHp() {
    String hp = input(1);
    try {
      return Double.parseDouble(hp);
    }
    catch (NumberFormatException e) {
      throw new StructureException("Cannot parse enemy hp <" + hp + ">", e);
    }
  }

}
