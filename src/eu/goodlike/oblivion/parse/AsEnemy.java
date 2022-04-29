package eu.goodlike.oblivion.parse;

import eu.goodlike.oblivion.NamedValue;
import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.StructureException;

import java.util.ArrayList;
import java.util.List;

import static eu.goodlike.oblivion.Command.Name.ENEMY;
import static eu.goodlike.oblivion.Global.ENEMIES;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Parses enemy input.
 * Ignores "enemy" command input if present.
 * Accepts any amount of inputs.
 * <p/>
 * Inputs with '@' prefix are treated as labels.
 * Only last label is considered, others ignored.
 * If it is missing, the enemy is given the default label 'enemy'.
 * <p/>
 * First non-label input is treated as HP.
 * It must be a parsable double.
 * <p/>
 * All other non-label inputs are treated as passive permanent effects.
 * They must be parsable.
 * <p/>
 * Enemies with default name 'enemy' is never cached.
 */
public final class AsEnemy extends BaseParseInput<Enemy> {

  @Override
  protected Enemy parse() {
    double hp = StructureException.doubleOrThrow(this.hp, "enemy hp");
    List<EffectText> effects = Parse.effects(this.effects);
    return new Enemy(hp, effects);
  }

  @Override
  public NamedValue<Enemy> inCache() {
    return "enemy".equals(label)
      ? this
      : ENEMIES.put(label, getValue());
  }

  public AsEnemy(String input) {
    this(Parse.line(input));
  }

  public AsEnemy(String[] input) {
    this.label = "enemy";

    int start = ENEMY.matches(input[0]) ? 1 : 0;
    for (int i = start; i < input.length; i++) {
      identify(input[i]);
    }
  }

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
