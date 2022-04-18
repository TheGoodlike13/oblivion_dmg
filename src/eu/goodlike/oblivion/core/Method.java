package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.core.hit.MonoHit;

import java.util.Arrays;

public interface Method extends Factor {

  default Hit hit(String name, EffectText... effects) {
    return new MonoHit(name, this, Arrays.asList(effects));
  }

  default double damageMultiplier() {
    return 1;
  }

  Effect.Id toId(Hit exactHit, Effect.Type type);

}
