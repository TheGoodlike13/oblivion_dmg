package eu.goodlike.oblivion;

import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

public interface Command {

  enum Name {
    ENEMY,
    GO,
    HIT,
    RESET,
    QUIT,
    WHAT;

    public boolean matches(String input) {
      return startsWithIgnoreCase(name(), input);
    }

    public static Optional<Name> find(String input) {
      return Stream.of(Name.values())
        .filter(name -> name.matches(input))
        .findFirst();
    }
  }

  void setParams(String... parsedInput);

  void setArena(Arena arena);

  void execute();

}
