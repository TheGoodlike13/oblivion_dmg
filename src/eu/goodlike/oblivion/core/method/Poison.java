package eu.goodlike.oblivion.core.method;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import eu.goodlike.oblivion.core.Category;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Effector;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.effect.Resist;
import eu.goodlike.oblivion.core.effector.EffectorSkeleton;

import java.util.List;

public final class Poison extends Category<Poison.Bottle> implements Method {

  @Override
  public EffectText resist(int pc) {
    return new EffectText(MAGIC, resistPoison, pc);
  }

  @Override
  public Effect.Type resist() {
    return resistPoison;
  }

  @Override
  public Bottle create(String label, List<EffectText> effects) {
    return new Bottle(label, effects);
  }

  @Override
  public double damageMultiplier() {
    return 1;
  }

  private Poison() {
    super("POISON", Bottle::new);
  }

  private final Resist.Type resistPoison = new Resist.OfFactor(this);

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

  public static final class Bottle extends EffectorSkeleton {
    @Override
    public Category<?> getCategory() {
      return Poison.getInstance();
    }

    @Override
    public Method getMethod() {
      return Poison.getInstance();
    }

    @Override
    public Effect.Id toId(EffectText effect) {
      Effect.Type type = effect.getType();
      useCounter.add(type);
      return new AlwaysUnique(this, type, useCounter.count(type));
    }

    @Override
    public Effector copy(String label) {
      return new Bottle(label, this);
    }

    public Bottle(String label, Iterable<EffectText> effects) {
      super(label, effects);
    }

    private final Multiset<Effect.Type> useCounter = HashMultiset.create();
  }

  private static final class AlwaysUnique implements Effect.Id {
    @Override
    public Effect.Type getType() {
      return type;
    }

    private AlwaysUnique(Bottle poison, Effect.Type type, int use) {
      this.poison = poison;
      this.type = type;
      this.use = use;
    }

    private final Bottle poison;
    private final Effect.Type type;
    private final int use;

    @Override
    public String toString() {
      return "(" + use + ")" + poison.getName() + " " + getType();
    }
  }

}
