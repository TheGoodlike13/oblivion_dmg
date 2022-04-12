package eu.goodlike;

import org.junit.jupiter.api.Test;

class MainTest {

  @Test
  void justFail() {
    throw new AssertionError("WOOT");
  }

}
