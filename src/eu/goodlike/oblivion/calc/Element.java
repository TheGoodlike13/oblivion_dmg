package eu.goodlike.oblivion.calc;

import java.util.Collection;

public enum Element {

  POISON,
  MAGIC,
  FIRE,
  COLD,
  LIGHTNING;

  public Modifier weakness(int magnitude) {
    return new Modifier(magnitude);
  }

  public Modifier resist(int magnitude) {
    return new Modifier(-magnitude);
  }

  public Damage damage(int dmg) {
    return new Damage(dmg);
  }

  public static double effect(Collection<Modifier> all) {
    double sum = BASE_EFFECT;
    for (Modifier m : all) {
      sum += m.magnitude();
    }
    return Math.max(0, sum);
  }

  private static final int BASE_EFFECT = 100;

  abstract class Effect implements SpellEffect {
    @Override
    public Element element() {
      return Element.this;
    }

    @Override
    public double magnitude() {
      return magnitude;
    }

    Effect(double magnitude) {
      this.magnitude = magnitude;
    }

    private final double magnitude;
  }

  public final class Modifier extends Effect {
    @Override
    public Type type() {
      return Type.MODIFY;
    }

    public Modifier(double magnitude) {
      super(magnitude);
    }
  }

  public final class Damage extends Effect {
    @Override
    public Type type() {
      return Type.DAMAGE;
    }

    public Damage(double magnitude) {
      super(magnitude);
    }
  }

}
