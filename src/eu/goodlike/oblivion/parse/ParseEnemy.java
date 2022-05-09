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
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Parses enemy input.
 * Ignores "enemy" command input if present.
 * Accepts any amount of inputs.
 * <p/>
 * For inputs matching any condition, only the last one is considered, rest are ignored.
 * <p/>
 * Inputs with ':' prefix are treated as labels.
 * If it is missing, the enemy is given the default label 'enemy'.
 * <p/>
 * Inputs with '*' prefix are treated as level multipliers.
 * Only positive numbers are allowed.
 * If it is set, the enemy is considered to be leveled.
 * The HP value for the enemy will be treated as lowest possible level HP.
 * If it is missing, the enemy is not considered leveled and other level related inputs are ignored.
 * <p/>
 * Inputs with '[' prefix are treated as minimum level.
 * Only positive numbers are allowed.
 * It's only considered if level multiplier is also present.
 * If it is missing in that case, the enemy is given the default minimum level of 1.
 * <p/>
 * Inputs with ']' suffix are treated as maximum level.
 * Only levels higher than the minimum are allowed.
 * It's only considered if level multiplier is also present.
 * If it is missing in that case, the enemy is given the default maximum level of 2^31-1.
 * <p/>
 * First numeric input is treated as HP.
 * It must be a positive integer.
 * If the enemy is leveled, this is considered to be its HP at the lowest level.
 * <p/>
 * All other non-numeric inputs are treated as passive permanent effects.
 * They must be parsable.
 * <p/>
 * Enemies with default name 'enemy' is never cached.
 */
public final class ParseEnemy extends BaseParseInput<Enemy> {

  @Override
  protected Enemy parse() {
    int hp = StructureException.natOrThrow(this.hp, "enemy hp");
    List<EffectText> effects = Parse.effects(this.effects);
    Enemy enemy = new Enemy(hp, effects);
    if (isBlank(levelMultiplier)) {
      return enemy;
    }

    int levelMultiplier = StructureException.intOrThrow(this.levelMultiplier, "level multiplier");
    int minLevel = StructureException.intOrThrow(this.minLevel, "min level");
    int maxLevel = StructureException.intOrThrow(this.maxLevel, "max level");
    return enemy.setLeveled(levelMultiplier, minLevel, maxLevel);
  }

  @Override
  public NamedValue<Enemy> thenCache() {
    return "enemy".equals(label)
      ? this
      : ENEMIES.put(label, getValue());
  }

  public ParseEnemy(String input) {
    this(Parse.line(input));
  }

  public ParseEnemy(String[] input) {
    this.label = "enemy";

    int start = ENEMY.matches(input[0]) ? 1 : 0;
    for (int i = start; i < input.length; i++) {
      identify(input[i]);
    }
  }

  private String hp = "";
  private String levelMultiplier = "";
  private String minLevel = "1";
  private String maxLevel = String.valueOf(Integer.MAX_VALUE);
  private final List<String> effects = new ArrayList<>();

  private void identify(String input) {
    if (input.startsWith(":")) {
      label = input.substring(1);
    }
    else if (input.startsWith("*")) {
      levelMultiplier = input.substring(1);
    }
    else if (input.startsWith("[")) {
      minLevel = input.substring(1);
    }
    else if (input.endsWith("]")) {
      maxLevel = input.substring(0, input.length() - 1);
    }
    else if (isNumeric(input) && isBlank(hp)) {
      hp = input;
    }
    else {
      effects.add(input);
    }
  }

}
