package eu.goodlike.oblivion.core;

public interface Hit extends Iterable<EffectText> {

  Method getMethod(EffectText effect);

  Effect.Id getId(EffectText effect);

}
