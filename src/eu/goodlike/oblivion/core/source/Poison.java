package eu.goodlike.oblivion.core.source;

import eu.goodlike.oblivion.core.Carrier;
import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.EffectText;
import eu.goodlike.oblivion.core.Factor;
import eu.goodlike.oblivion.core.Method;
import eu.goodlike.oblivion.core.Source;
import eu.goodlike.oblivion.core.effect.Resist;

import java.util.List;

public final class Poison implements Method, Source {

  @Override
  public EffectText resist(int pc) {
    return new EffectText(MAGIC, resistPoison, pc);
  }

  @Override
  public Carrier create(String label, List<EffectText> effects) {
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

  private static final class PoisonBottle extends Carrier {
    @Override
    public Effect.Id toId(EffectText effect) {
      return new AlwaysUnique();
    }

    @Override
    public Carrier copy(String label) {
      return new PoisonBottle(label, this);
    }

    public PoisonBottle(String label, Iterable<EffectText> effects) {
      super(label, Source.POISON, Factor.POISON, effects);
    }
  }

  private static final class AlwaysUnique implements Effect.Id {
  }

}
