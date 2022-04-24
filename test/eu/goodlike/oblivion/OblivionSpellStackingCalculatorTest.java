package eu.goodlike.oblivion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class OblivionSpellStackingCalculatorTest implements Supplier<String>, Consumer<String> {

  private final OblivionSpellStackingCalculator calc = new OblivionSpellStackingCalculator(this, this);

  private Iterator<String> input;
  private List<String> output;

  @Override
  public String get() {
    return input.next();
  }

  @Override
  public void accept(String s) {
    if (!">> ".equals(s)) {
      output.add(s.trim());
    }
  }

  @BeforeEach
  void setup() {
    input = null;
    output = new ArrayList<>();
  }

  @Test
  void quitImmediately() {
    mockInput("quit");
    assertOutput();
  }

  @Test
  void unrecognizedOutput() {
    mockInput("?");
    assertOutput("No idea what <?> is supposed to mean.");
  }

  @Test
  void newEnemy() {
    mockInput("enemy 1000");
    assertOutput("Today you'll be hitting an enemy with 1000.0 hp.");
    assertThat(calc.enemy.healthRemaining()).isEqualTo(1000, within(0.01));
  }

  private void mockInput(String... lines) {
    input = Stream.of(lines).iterator();
  }

  private void assertOutput(String... lines) {
    calc.run();
    assertThat(output).containsExactly(lines);
  }

}
