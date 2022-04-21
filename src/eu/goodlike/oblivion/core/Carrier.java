package eu.goodlike.oblivion.core;

import java.util.Comparator;

public interface Carrier extends Iterable<EffectText>, Comparable<Carrier> {

  Comparator<Carrier> ORDER = Comparator.comparing(Carrier::getSource);

  Source getSource();
  String getName();
  Method getMethod();
  Effect.Id toId(EffectText effect);

  @Override
  default int compareTo(Carrier other) {
    return ORDER.compare(this, other);
  }

  interface Factory {
    Carrier create(String name, EffectText... effects);
    Effect.Id toId(String carrierName, Effect.Type type);
  }

}
