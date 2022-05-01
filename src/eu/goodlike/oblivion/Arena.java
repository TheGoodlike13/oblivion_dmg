package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Target;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static eu.goodlike.oblivion.Global.Settings.DUMPS;
import static eu.goodlike.oblivion.Global.Settings.TICK;
import static java.util.stream.Collectors.joining;

/**
 * The calculator for using spells/attacks on an enemy.
 * <p/>
 * Writes down all inputs and outcomes.
 */
public final class Arena {

  public void setEnemy(NamedValue<Enemy> enemy) {
    this.label = enemy.getName().replace('_', ' ');
    this.enemy = enemy.getValue();
    announceOpponent();
  }

  public void addHit(NamedValue<Hit> hit) {
    hits.add(hit.getValue());
    Write.line("[#" + hit.getName() + "] Next hit: " + hit.getValue());
  }

  public void removeLastHit() {
    if (hits.isEmpty()) {
      Write.line("No hits to remove.");
    }
    else {
      Hit removed = hits.remove(hits.size() - 1);
      Write.line("Removed hit: " + removed);
    }
  }

  public void lowerTheGates() {
    if (ready()) {
      fight();
    }
  }

  public void refresh() {
    Write.separator();

    play = new PlayByPlay();
    hits = new ArrayList<>();
    if (enemy != null) {
      enemy.updateLevel();
      announceOpponent();
    }
  }

  public void reset() {
    play = new PlayByPlay();
    hits = new ArrayList<>();
    label = null;
    enemy = null;
  }

  public Arena() {
    reset();
  }

  private PlayByPlay play;
  private List<Hit> hits;
  private String label;
  private Enemy enemy;

  private boolean ready() {
    if (hits.isEmpty()) {
      String beholdee = enemy == null ? "void" : label;
      Write.line("You stare at the " + beholdee + ".");
      Write.line("The " + beholdee + " stares at you.");
      Write.line("How about casting some spells?");
      return false;
    }
    if (enemy == null) {
      Write.line("All your hits land on the wall.");
      Write.line("Good job.");
      Write.line("How about picking an enemy?");
      return false;
    }
    return true;
  }

  private void fight() {
    try {
      performHits();
      awaitEffectExpiration();
      writeObituary();
    }
    finally {
      refresh();
    }
  }

  private void performHits() {
    for (Hit hit : hits) {
      enemy.hit(hit, play);
    }
  }

  private void awaitEffectExpiration() {
    while (enemy.isAlive() && enemy.isAffected()) {
      enemy.tick(play);
    }

    enemy.resolve(play);
  }

  private void writeObituary() {
    if (enemy.isAlive()) {
      Write.line("The %s has survived %.1f damage (%.1f hp left).", label, enemy.damageTaken(), enemy.healthRemaining());
    }
    else {
      Write.line("The %s took a total of %.1f damage (%.1f overkill).", label, enemy.damageTaken(), enemy.overkill());
    }
    play.writeTotals();
  }

  private void announceOpponent() {
    Write.line("You face the %s (%.0f hp).", label, enemy.healthRemaining());
    writeResists();
  }

  private void writeResists() {
    factorMods(Factor.ALL, false).forEach(Write::line);
  }

  private Stream<String> factorMods(List<Factor> factors, boolean includeAll) {
    return factors.stream()
      .map(factor -> factorMod(factor, includeAll))
      .filter(StringUtils::isNotBlank);
  }

  private String factorMod(Factor factor, boolean includeAll) {
    double multiplier = enemy.getMultiplier(factor);
    return !includeAll && multiplier == 1
      ? ""
      : String.format("%-6s x%.2f", factor, multiplier);
  }

  private final class PlayByPlay implements Enemy.Observer, Target {
    @Override
    public Target observing(Target actual) {
      dumpModifiedFactors();

      this.isTicking = false;
      this.actual = actual;
      return this;
    }

    @Override
    public void tick() {
      this.isTicking = true;

      if (duration >= nextDump) {
        nextDump += 1d / DUMPS;
        dumpNextChunkOfDamage();
      }
      duration += TICK;
    }

    @Override
    public void next(Effect.Id id) {
      this.isExpired = false;
      this.id = id;
    }

    @Override
    public void markExpired() {
      this.isExpired = true;
      if (isTicking) {
        combatLog("Expired " + id);
      }
    }

    @Override
    public void modifyResist(Factor factor, double percent) {
      actual.modifyResist(factor, percent);
      if (!isTicking) {
        combatLog(newEffect(percent));
      }
      modifiedFactors.add(factor);
    }

    @Override
    public void damage(double dmg) {
      if (enemy.isAlive()) {
        liveDamage.merge(id, dmg, Double::sum);
        totalDamage.merge(id, dmg, Double::sum);
      }
      else {
        totalOverkill.merge(id, dmg, Double::sum);
      }
      actual.damage(dmg);
      checkPossibleDeath();
    }

    @Override
    public void drain(double hp) {
      actual.drain(hp);
      if (isTicking) {
        if (!isDeathConfirmed) {
          writeEnemyHp("restored");
          totalDamage.remove(id);
        }
      }
      else {
        combatLog(newEffect(hp));
        if (!isDeathConfirmed) {
          writeEnemyHp("drained");
          totalDamage.put(id, hp);
        }
        checkPossibleDeath();
      }
    }

    @Override
    public void poke(double magnitude, double duration) {
      actual.poke(magnitude, duration);
      if (!isTicking) {
        combatLog(String.format("%s %s %.1f for %.0fs", newEffect(), id, magnitude, duration));
      }
    }

    public void dumpModifiedFactors() {
      if (!modifiedFactors.isEmpty()) {
        String factorMods = factorMods(modifiedFactors, true).collect(joining(", "));
        combatLog("Affected multipliers: " + factorMods);
        modifiedFactors.clear();
      }
    }

    public void writeTotals() {
      writeTotals(totalDamage, "damage");
      writeTotals(totalOverkill, "overkill");
    }

    public PlayByPlay() {
      this.isDeathConfirmed = false;

      this.actual = null;
      this.id = null;
      this.isTicking = false;
      this.isExpired = false;

      this.duration = 0;
      this.nextDump = 1d / DUMPS;

      this.liveDamage = new LinkedHashMap<>();
      this.totalDamage = new LinkedHashMap<>();
      this.totalOverkill = new LinkedHashMap<>();

      this.modifiedFactors = new ArrayList<>();
    }

    private boolean isDeathConfirmed;

    private Target actual;
    private Effect.Id id;
    private boolean isTicking;
    private boolean isExpired;

    private double duration;
    private double nextDump;

    private final Map<Effect.Id, Double> liveDamage;
    private final Map<Effect.Id, Double> totalDamage;
    private final Map<Effect.Id, Double> totalOverkill;

    private final List<Factor> modifiedFactors;

    private void dumpNextChunkOfDamage() {
      liveDamage.forEach((id, d) -> combatLog(String.format("Took %s %.2f", id, d)));
      liveDamage.clear();
    }

    private void checkPossibleDeath() {
      if (!isDeathConfirmed && !enemy.isAlive()) {
        isDeathConfirmed = true;
        dumpNextChunkOfDamage();
        combatLog("The " + label + " has died.");
      }
    }

    private String newEffect(double percent) {
      return String.format("%s %s %.1f", newEffect(), id, percent);
    }

    private String newEffect() {
      return isExpired ? "Replaced" : "Added";
    }

    private void combatLog(String text) {
      Write.line("%06.3f %s", duration, text);
    }

    private void writeEnemyHp(String status) {
      combatLog(String.format("Health %s: %s", status, enemy.healthStatus()));
    }

    private void writeTotals(Map<Effect.Id, Double> totals, String desc) {
      if (!totals.isEmpty()) {
        Write.line("Total %s by effect id:", desc);
        totals.forEach(this::writeEffect);
      }
    }

    private void writeEffect(Effect.Id id, double d) {
      Write.line(String.format("%s: %.2f", id, d));
    }
  }

}
