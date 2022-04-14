package eu.goodlike.oblivion.calc;

import java.util.Objects;

public interface SpellEffect {

  Element element();

  double magnitude();

  Type type();

  default int seconds() {
    return 1;
  }

  default Id toId(int spellId) {
    return new Id(spellId, element());
  }

  enum Type {
    DAMAGE, MODIFY
  }

  final class Id {
    public int value() {
      return spellId;
    }

    public Element element() {
      return element;
    }

    public Id(int spellId, Element element) {
      this.spellId = spellId;
      this.element = element;
    }

    private final int spellId;
    private final Element element;

    @Override
    public String toString() {
      return element + "<" + spellId + ">";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Id id = (Id)o;
      return value() == id.value() && element() == id.element();
    }

    @Override
    public int hashCode() {
      return Objects.hash(value(), element());
    }
  }

}
