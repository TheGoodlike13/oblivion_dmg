package eu.goodlike.oblivion.parse;

import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.StructureException;

import java.util.ArrayList;
import java.util.List;

import static eu.goodlike.oblivion.Command.Name.ENEMY;
import static eu.goodlike.oblivion.Global.CACHE;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Parses enemy input.
 * Ignores "enemy" command input if present.
 * Accepts any amount of inputs.
 * <p/>
 * Inputs with '@' prefix are treated as labels.
 * Only last label is considered, others ignored.
 * <p/>
 * First non-label input is treated as HP.
 * It must be a parsable double.
 * <p/>
 * All other non-label inputs are treated as passive permanent effects.
 * They must be parsable effects.
 * <p/>
 * TODO: move caching out?
 */
public final class EnemyParser {

  public String getLabel() {
    return label;
  }

  public Enemy parseEnemy() {
    double hp = StructureException.doubleOrThrow(this.hp, "enemy hp");
    List<EffectText> effects = Parse.effects(this.effects);

    Enemy enemy = new Enemy(hp, effects);
    if (!"enemy".equals(label)) {
      CACHE.put(label, enemy);
    }
    return enemy;
  }

  public EnemyParser(String[] input) {
    int start = ENEMY.matches(input[0]) ? 1 : 0;

    for (int i = start; i < input.length; i++) {
      identify(input[i]);
    }
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

}
