package eu.goodlike.oblivion;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Streams;
import eu.goodlike.oblivion.core.Armament;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.Enemy;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Hit;
import eu.goodlike.oblivion.core.Target;
import eu.goodlike.oblivion.core.effect.Damage;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static eu.goodlike.oblivion.Consumerable.whileConsuming;
import static eu.goodlike.oblivion.Global.Settings.RAMPAGE;
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
    this.enemy = enemy.getValue().updateLevel();
    announceOpponent();
  }

  public void enqueueHit(NamedValue<Hit> hit) {
    hits.add(hit.getValue());
    Write.line("[#" + hit.getName() + "] Next hit: " + hit.getValue());
  }

  public void setPause(double waitTime) {
    this.pause = waitTime;
    Write.line("You will wait at least %.2fs between hits", pause);
  }

  public void removeLastHit() {
    if (hits.isEmpty()) {
      Write.line("No hits to remove.");
    }
    else {
      Write.line("Removed hit: " + hits.removeLast());
    }
  }

  public void lowerTheGates() {
    if (ready()) {
      try {
        fight();
      }
      finally {
        refresh();
      }
    }
  }

  public void refresh() {
    Write.separator();

    play = new PlayByPlay();
    hits = new ArrayDeque<>();
    if (enemy != null) {
      enemy.updateLevel();
      announceOpponent();
    }
  }

  public void reset() {
    play = new PlayByPlay();
    hits = new ArrayDeque<>();
    pause = 0;
    label = null;
    enemy = null;
  }

  public Arena() {
    reset();
  }

  private PlayByPlay play;
  private Deque<Hit> hits;
  private double pause;
  private String label;
  private Enemy enemy;

  private void announceOpponent() {
    Write.line("You face the %s (%.0f hp)", label, enemy.healthRemaining());
    writeResists();
  }

  private void writeResists() {
    factorMods(Factor.ALL, true).forEach(Write::line);
  }

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
    equipFirstWeapon();

    int planned = hits.size();
    for (Hit hit : whileConsuming(hits)) {
      boolean shouldContinue = play.next(hit);
      if (!shouldContinue && !hits.isEmpty()) {
        play.giveItARest();
        break;
      }
    }

    enemy.resolve(play);

    play.writeRemainingHits(planned);
    play.writeObituary();
  }

  private void equipFirstWeapon() {
    hits.stream()
      .map(Hit::getWeapon)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findFirst()
      .ifPresent(play::setInitialWeapon);
  }

  private Stream<String> factorMods(Iterable<Factor> factors, boolean ignoreTrivial) {
    return Streams.stream(factors)
      .map(factor -> factorMod(factor, ignoreTrivial))
      .filter(StringUtils::isNotBlank);
  }

  private String factorMod(Factor factor, boolean ignoreTrivial) {
    double multiplier = enemy.getMultiplier(factor);
    return ignoreTrivial && multiplier == 1
      ? ""
      : String.format("%-6s x%.2f", factor, multiplier);
  }

  private final class PlayByPlay implements Enemy.Observer {
    public void setInitialWeapon(Armament initialWeapon) {
      equippedWeapon = initialWeapon;
      play.combatLog("You begin equipped with " + initialWeapon.getName());
    }

    public boolean next(Hit hit) {
      prepare(hit);
      idleTime = pause - perform(hit);

      return isAliveOrJustRecentlyDeceased();
    }

    public void giveItARest() {
      combatLog("You stop beating the dead " + label);
    }

    @Override
    public Target observing(Target actual) {
      return new Stats(actual);
    }

    @Override
    public void tick() {
      duration += TICK;
    }

    @Override
    public void next(Effect.Id id) {
      this.removed = null;
      this.id = id;
    }

    @Override
    public void markForRemoval(Effect effect) {
      this.removed = effect;
      if (effect.hasExpired() && !effect.isInstant()) {
        combatLog("Expired " + id);
      }
    }

    public void writeRemainingHits(int planned) {
      if (!hits.isEmpty()) {
        Write.line("Performed %d hits out of total %d prepared", planned - hits.size(), planned);
        for (Hit hit : hits) {
          Write.line("Skipped: " + hit);
        }
      }
    }

    public void writeObituary() {
      if (enemy.isAlive()) {
        Write.line("The %s has survived %.1f damage (%.1f hp left).", label, enemy.damageTaken(), enemy.healthRemaining());
        writeFinalBreakdown("Damage");
      }
      else {
        Write.line("The %s took a total of %.1f damage (%.1f overkill).", label, enemy.damageTaken(), enemy.overkill());
        writeFinalBreakdown("Overkill");
      }
      writeWastedDamageBreakdown();
    }

    public PlayByPlay() {
      this.equippedWeapon = null;
      this.needsSwap = false;

      this.idleTime = 0;
      this.lastHit = null;
      this.combo = 0;

      this.id = null;
      this.removed = null;

      this.modifiedFactors = new LinkedHashSet<>();
      this.damageTotals = new LinkedHashMap<>();
      this.wastedDamage = ArrayListMultimap.create();

      this.hasDeathBeenBrokenDown = false;

      this.duration = 0;
      this.lastLog = -1;
      this.deathStamp = Double.MAX_VALUE;
    }

    private Armament equippedWeapon;
    private boolean needsSwap;

    private double idleTime;
    private Hit lastHit;
    private int combo;

    private Effect.Id id;
    private Effect removed;

    private boolean hasDeathBeenBrokenDown;

    private final Set<Factor> modifiedFactors;
    private final Map<Effect.Id, Double> damageTotals;
    private final ListMultimap<Effect.Id, Effect> wastedDamage;

    private double duration;
    private double lastLog;
    private double deathStamp;

    private void combatLog(String text) {
      if (lastLog == duration) {
        Write.line("       %s", text);
      }
      else {
        lastLog = duration;
        Write.line("%06.3f %s", duration, text);
      }
    }

    private boolean isAliveOrJustRecentlyDeceased() {
      return deathStamp + RAMPAGE > duration;
    }

    private void prepare(Hit hit) {
      needsSwap = false;
      hit.requiresSwap(equippedWeapon).ifPresent(newWeapon -> {
        needsSwap = true;
        equippedWeapon = newWeapon;
      });

      if (needsSwap || hit.requiresCooldownAfter(lastHit)) {
        double cooldown = lastHit.cooldown(combo);
        idleTime -= cooldown;
        enemy.tick(cooldown, this);
      }

      if (needsSwap) {
        combatLog("You begin to swap your weapon.");
        double timeToSwap = equippedWeapon.timeToSwap();
        idleTime -= timeToSwap;
        enemy.tick(timeToSwap, this);
        combatLog("You equip " + equippedWeapon.getName());
      }

      if (idleTime > 0) {
        play.combatLog(String.format("You wait for %.2fs", idleTime));
        enemy.tick(idleTime, this);
      }

      if (!hit.isCombo(lastHit) || idleTime >= lastHit.cooldown(combo)) {
        combo = 0;
      }
    }

    private double perform(Hit hit) {
      combatLog("You " + hit.toPerformString());
      double timeToHit = hit.timeToHit(combo);
      enemy.tick(timeToHit, this);

      combatLog("You hit with " + hit.toLabelString());
      lastHit = hit;
      enemy.hit(hit, this);
      combo += 1;

      dumpModifiedFactors();
      return timeToHit;
    }

    private void dumpModifiedFactors() {
      if (enemy.isAlive() && !modifiedFactors.isEmpty()) {
        String factorMods = factorMods(modifiedFactors, false).collect(joining(", "));
        combatLog("Resulting multipliers: " + factorMods);
        modifiedFactors.clear();
      }
    }

    private void breakdownPossibleDeath() {
      if (!hasDeathBeenBrokenDown && !enemy.isAlive()) {
        deathStamp = duration;
        hasDeathBeenBrokenDown = true;
        combatLog("The " + label + " has died. Breakdown:");
        writeDamageBreakdown();
        switchTotalsToOverkillMode();
      }
    }

    private void switchTotalsToOverkillMode() {
      damageTotals.clear();
      double overkill = enemy.overkill();
      if (overkill >= BASICALLY_NO_OVERKILL) {
        damageTotals.merge(id, overkill, Double::sum);
      }
    }

    private void writeFinalBreakdown(String desc) {
      if (!damageTotals.isEmpty()) {
        combatLog(desc + " by effect:");
      }
      writeDamageBreakdown();
    }

    private void writeDamageBreakdown() {
      damageTotals.forEach(this::writeEffect);
    }

    private void writeEffect(Effect.Id id, double d) {
      combatLog(String.format("    %s: %.2f", id, d));
    }

    private void writeWastedDamageBreakdown() {
      if (!wastedDamage.isEmpty()) {
        combatLog("Total damage wasted due to overlap:");
      }
      double total = wastedDamage.asMap().entrySet()
        .stream()
        .mapToDouble(e -> writeWastedEffect(e.getKey(), e.getValue()))
        .sum();
      if (total > 0) {
        combatLog(String.format("Grand total: %.2f", total));
      }
    }

    private double writeWastedEffect(Effect.Id id, Collection<Effect> effects) {
      combatLog(id.toString());

      double sum = 0;
      for (Effect effect : effects) {
        double total = effect.effectiveMagnitude() * effect.remainingDuration();
        combatLog(String.format("%9.1f * %4.2fs = %7.2f", effect.effectiveMagnitude(), effect.remainingDuration(), total));
        sum += total;
      }
      return sum;
    }

    private static final double BASICALLY_NO_OVERKILL = 0.005;

    private final class Stats implements Target {
      @Override
      public void modifyResist(Factor factor, double percent) {
        actual.modifyResist(factor, percent);
        modifiedFactors.add(factor);
      }

      @Override
      public void damage(double dmg) {
        actual.damage(dmg);
        damageTotals.merge(id, dmg, Double::sum);
        breakdownPossibleDeath();
      }

      @Override
      public boolean drain(double hp) {
        boolean wasApplied = actual.drain(hp);
        if (wasApplied) {
          if (hp > 0) {
            damageTotals.put(id, hp);
            writeDrainStatus("drained");
          }
          else {
            damageTotals.remove(id);
            writeDrainStatus("restored");
          }
          breakdownPossibleDeath();
        }
        return wasApplied;
      }

      @Override
      public void poke(double magnitude, double duration) {
        actual.poke(magnitude, duration);
        combatLog(newEffect(magnitude, duration));
        if (removed != null && Damage.matches(id)) {
          wastedDamage.put(id, removed);
        }
      }

      protected Stats(Target actual) {
        this.actual = actual;
      }

      private final Target actual;

      private void writeDrainStatus(String status) {
        combatLog(String.format("The " + label + " hp %s [%s]", status, enemy.healthStatus()));
      }

      private String newEffect(double magnitude, double duration) {
        return String.join(" ", howNew(), whichEffect(), numbers(magnitude, duration));
      }

      private String howNew() {
        return removed == null ? "Applied" : "Replaced";
      }

      private String whichEffect() {
        return String.valueOf(lastHit.hasMultiple(id.getType()) ? id : id.getType());
      }

      private String numbers(double magnitude, double duration) {
        return replacement(magnitude) + decimal(magnitude) + indicator(duration);
      }

      private String replacement(double updated) {
        return removed == null || removed.effectiveMagnitude() == updated
          ? ""
          : String.format("%.1f with ", removed.effectiveMagnitude());
      }

      private String decimal(double magnitude) {
        return String.format("%.1f", magnitude);
      }

      private String indicator(double duration) {
        return duration > 0 ? String.format(" for %.0fs", duration) : " (instant)";
      }
    }
  }

}
