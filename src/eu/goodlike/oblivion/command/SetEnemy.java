package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.NamedValue;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.parse.ParseEnemy;

import static eu.goodlike.oblivion.Global.ENEMIES;
import static eu.goodlike.oblivion.Global.THE_ARENA;

public final class SetEnemy extends BaseCommand {

  @Override
  protected void performTask() {
    THE_ARENA.setEnemy(fromInput());
  }

  private NamedValue<Enemy> fromInput() {
    return input(1).startsWith("$")
      ? ENEMIES.getCached(input(1).substring(1))
      : new ParseEnemy(inputs).thenCache();
  }

}
