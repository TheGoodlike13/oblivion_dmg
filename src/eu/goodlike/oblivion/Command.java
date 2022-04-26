package eu.goodlike.oblivion;

import eu.goodlike.oblivion.command.ButWhatDoesThisMean;
import eu.goodlike.oblivion.command.ItsAllOver;
import eu.goodlike.oblivion.command.SetEnemy;
import eu.goodlike.oblivion.command.TimeToGo;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

public interface Command {

  enum Name {
    ENEMY(SetEnemy::new),
    GO(TimeToGo::new),
    HIT(ButWhatDoesThisMean::new),
    RESET(ButWhatDoesThisMean::new),
    QUIT(ItsAllOver::new),
    WHAT(ButWhatDoesThisMean::new);

    public static Name find(String input) {
      return Stream.of(Name.values())
        .filter(name -> name.matches(input))
        .findFirst()
        .orElse(WHAT);
    }

    public Command newCommand() {
      return factory.get();
    }

    public boolean matches(String input) {
      return startsWithIgnoreCase(name(), input);
    }

    Name(Supplier<Command> factory) {
      this.factory = factory;
    }

    private final Supplier<Command> factory;
  }

  void setParams(String... parsedInput);

  void setArena(Arena arena);

  void execute();

}
