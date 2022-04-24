package eu.goodlike.oblivion;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Command {

  enum Name {
    ENEMY,
    QUIT;

    public boolean matches(String input) {
      return StringUtils.startsWithIgnoreCase(name(), input);
    }

    public static Optional<Name> find(String input) {
      return Stream.of(Name.values())
        .filter(name -> name.matches(input))
        .findFirst();
    }
  }

  void setParams(String... parsedInput);

  void setWriter(Consumer<String> writer);

  void setArena(Arena arena);

  void execute();

}
