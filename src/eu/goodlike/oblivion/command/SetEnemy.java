package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Cache;
import eu.goodlike.oblivion.Global;
import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.StructureException;

import java.util.ArrayList;
import java.util.List;

import static eu.goodlike.oblivion.Arena.THE_ARENA;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

public final class SetEnemy extends BaseCommand {

  public static void invalidate() {
    CACHE.reset(SetEnemy::parseEnemy, Global.Settings.PREPARED_ENEMIES);
  }

  @Override
  protected void performTask() {
    if (input(1).startsWith("$")) {
      String ref = input(1).substring(1);
      Enemy enemy = CACHE.get(ref);
      THE_ARENA.setEnemy(ref, enemy);
      return;
    }

    Enemy enemy = parseEnemy();
    THE_ARENA.setEnemy(label, enemy);
  }

  private Enemy parseEnemy() {
    args().forEach(this::identify);
    double hp = StructureException.doubleOrThrow(this.hp, "enemy hp");
    List<EffectText> effects = this.effects.stream().map(Parse::effect).collect(toList());
    Enemy enemy = new Enemy(hp, effects);
    if (!"enemy".equals(label)) {
      CACHE.put(label, enemy);
    }
    return enemy;
  }

  private String label = "enemy";
  private String hp = "";
  private final List<String> effects = new ArrayList<>();

  private void identify(String input) {
    if (input.startsWith("@")) {
      label = input.substring(1);
    }
    else if (isBlank(hp)) {
      hp = input;
    }
    else {
      effects.add(input);
    }
  }

  private static final Cache<Enemy> CACHE = new Cache<>("");

  private static void parseEnemy(String line) {
    String[] inputs = split("enemy " + line.trim().toLowerCase());
    SetEnemy setEnemy = new SetEnemy();
    setEnemy.setParams(inputs);
    setEnemy.parseEnemy();
  }

}
