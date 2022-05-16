package eu.goodlike.oblivion.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class HitPatternTest {

  @Test
  @SuppressWarnings("Convert2MethodRef")
  void noCombo() {
    assertThatExceptionOfType(StructureException.class)
      .isThrownBy(() -> combo());
  }

  @Test
  void comboOfOne() {
    HitPattern combo = combo(1, 2);

    for (int i = 0; i < 5; i++) {
      assertThat(combo.timeToHit(i)).isEqualTo(1);
      assertThat(combo.cooldown(i)).isEqualTo(2);
    }
  }

  @Test
  void comboOfTwo() {
    HitPattern combo = combo(1, 2, 3, 4);

    assertThat(combo.timeToHit(0)).isEqualTo(1);
    assertThat(combo.cooldown(0)).isEqualTo(2);
    assertThat(combo.timeToHit(1)).isEqualTo(3);
    assertThat(combo.cooldown(1)).isEqualTo(4);
    assertThat(combo.timeToHit(2)).isEqualTo(1);
    assertThat(combo.cooldown(2)).isEqualTo(2);
    assertThat(combo.timeToHit(3)).isEqualTo(3);
    assertThat(combo.cooldown(3)).isEqualTo(4);
    assertThat(combo.timeToHit(4)).isEqualTo(1);
    assertThat(combo.cooldown(4)).isEqualTo(2);
  }

  @Test
  void comboOfThree() {
    HitPattern combo = combo(1, 2, 3, 4, 5, 6);

    assertThat(combo.timeToHit(0)).isEqualTo(1);
    assertThat(combo.cooldown(0)).isEqualTo(2);
    assertThat(combo.timeToHit(1)).isEqualTo(3);
    assertThat(combo.cooldown(1)).isEqualTo(4);
    assertThat(combo.timeToHit(2)).isEqualTo(5);
    assertThat(combo.cooldown(2)).isEqualTo(6);
    assertThat(combo.timeToHit(3)).isEqualTo(1);
    assertThat(combo.cooldown(3)).isEqualTo(2);
    assertThat(combo.timeToHit(4)).isEqualTo(3);
    assertThat(combo.cooldown(4)).isEqualTo(4);
  }

  private HitPattern combo(double... inputs) {
    HitPattern.Builder builder = Hit.Combo.builder();
    for (int i = 0; i < inputs.length; i +=2) {
      builder.combo(inputs[i], inputs[i + 1]);
    }
    return builder.build();
  }

}
