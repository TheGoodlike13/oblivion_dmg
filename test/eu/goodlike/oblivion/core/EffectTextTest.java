package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.Global;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static eu.goodlike.oblivion.Global.Settings.EFFECTIVENESS;
import static eu.goodlike.oblivion.core.Factor.FIRE;
import static org.assertj.core.api.Assertions.assertThat;

class EffectTextTest {

  private static final EffectText FLARE = new EffectText(FIRE, FIRE.damage(), 6).instant();

  @AfterEach
  void tearDown() {
    Global.Settings.load();
  }

  @Test
  void noScaling() {
    EFFECTIVENESS = 100;

    assertThat(FLARE.scale()).isSameAs(FLARE);
  }

  @Test
  void singleScaling() {
    EFFECTIVENESS = 90;

    EffectText scaled = FLARE.scale();
    assertThat(scaled.getMagnitude()).isEqualTo(5);

    assertThat(scaled).isNotEqualTo(FLARE);
    assertThat(scaled.scale()).isSameAs(scaled);
  }

  @Test
  void doubleScaling_effectivelySingle() {
    EFFECTIVENESS = 90;
    EffectText scaledOnce = FLARE.scale();

    EFFECTIVENESS = 85;
    assertThat(scaledOnce.scale()).isSameAs(scaledOnce);
  }

  @Test
  void doubleScaling_effectivelyDouble() {
    EFFECTIVENESS = 90;
    EffectText scaledOnce = FLARE.scale();

    EFFECTIVENESS = 80;
    EffectText scaledTwice = scaledOnce.scale();

    assertThat(scaledTwice.getMagnitude()).isEqualTo(4);
  }

}
