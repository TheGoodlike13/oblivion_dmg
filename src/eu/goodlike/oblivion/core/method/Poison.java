package eu.goodlike.oblivion.core.method;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.effect.Resist;

import java.util.Collections;
import java.util.List;

public final class Poison implements Method, Source {

  @Override
  public EffectText resist(int pc) {
    return new EffectText(MAGIC, resistPoison, pc);
  }

  @Override
  public Effect.Type resist() {
    return resistPoison;
  }

  @Override
  public Effector withNoEffect() {
    return NAME_POISON;
  }

  @Override
  public Effector create(String label, List<EffectText> effects) {
    return new PoisonBottle(label, effects);
  }

  @Override
  public double damageMultiplier() {
    return 1;
  }

  private final Resist.Type resistPoison = new Resist.OfFactor(this);

  @Override
  public String toString() {
    return "POISON";
  }

  /**
   * The way class loading works must be preventing referencing static instances.
   * Basically, in some cases they are referenced before they are assigned, leaving them as NULL.
   * This is likely because of the interface structure and not much can be done about it.
   * This idiom ensures every static reference to {@link Poison} will exist.
   */
  public static Poison getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Poison();
    }
    return INSTANCE;
  }

  private static Poison INSTANCE;
  private static final Effector NAME_POISON = new PoisonBottle(null, Collections.emptyList());

  private static final class PoisonBottle extends Effector {
    @Override
    public Effect.Id toId(EffectText effect) {
      Effect.Type type = effect.getType();
      useCounter.add(type);
      return new AlwaysUnique(this, type, useCounter.count(type));
    }

    @Override
    public Effector copy(String label) {
      return new PoisonBottle(label, this);
    }

    public PoisonBottle(String label, Iterable<EffectText> effects) {
      super(label, getInstance(), getInstance(), effects);
    }

    private final Multiset<Effect.Type> useCounter = HashMultiset.create();
  }

  private static final class AlwaysUnique implements Effect.Id {
    @Override
    public Effect.Type getType() {
      return type;
    }

    private AlwaysUnique(PoisonBottle poison, Effect.Type type, int use) {
      this.poison = poison;
      this.type = type;
      this.use = use;
    }

    private final PoisonBottle poison;
    private final Effect.Type type;
    private final int use;

    @Override
    public String toString() {
      return "(" + use + ")" + poison.getLabel() + " " + getType();
    }
  }

}
