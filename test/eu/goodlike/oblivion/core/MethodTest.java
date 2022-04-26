package eu.goodlike.oblivion.core;

import eu.goodlike.oblivion.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static eu.goodlike.oblivion.core.Factor.MAGIC;
import static eu.goodlike.oblivion.core.Factor.POISON;
import static org.assertj.core.api.Assertions.assertThat;

class MethodTest {

  private Method method;

  @AfterEach
  void tearDown() {
    Settings.resetToFactory();
  }

  @Test
  void poison() {
    method = POISON;

    assertMultiplier(0, 1);
    assertMultiplier(50, 1);
    assertMultiplier(100, 1);
  }

  @Test
  void magicEffects() {
    method = MAGIC;

    assertMultiplier(0, 6);
    assertMultiplier(10, 5);
    assertMultiplier(25, 3.5);
    assertMultiplier(40, 2);
    assertMultiplier(50, 1);
    assertMultiplier(60, 1d/2);
    assertMultiplier(75, 2d/7);
    assertMultiplier(90, 1d/5);
    assertMultiplier(100, 1d/6);
  }
  
  private void assertMultiplier(double difficulty, double multiplier) {
    Settings.DIFFICULTY = difficulty;

    assertThat(method.damageMultiplier()).isEqualTo(multiplier);
  }

}
