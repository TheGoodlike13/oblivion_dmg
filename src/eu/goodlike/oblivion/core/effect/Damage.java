package eu.goodlike.oblivion.core.effect;

import eu.goodlike.oblivion.core.Effect;
import eu.goodlike.oblivion.core.Element;
import eu.goodlike.oblivion.core.Target;

import java.util.Objects;

public final class Damage extends BaseEffect {

  public static final class OfElement implements Type {
    @Override
    public Effect activate(double magnitude, double duration) {
      return new Damage(magnitude, duration);
    }

    @Override
    public boolean affectsHp() {
      return true;
    }

    public OfElement(Element element) {
      this.element = element;
    }

    private final Element element;

    @Override
    public String toString() {
      return element + " DMG";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      OfElement ofElement = (OfElement)o;
      return Objects.equals(element, ofElement.element);
    }

    @Override
    public int hashCode() {
      return Objects.hash(element);
    }
  }

  @Override
  public void onApply(Target target) {
    target.poke(effectiveMagnitude(), remainingDuration());
  }

  @Override
  public void onRemove(Target target) {
  }

  @Override
  protected void onEffectiveTick(Target target, double tick) {
    target.damage(tick * effectiveMagnitude());
  }

  public Damage(double magnitude, double duration) {
    super(magnitude, duration);
  }

}
