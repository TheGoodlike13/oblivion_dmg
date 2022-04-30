package eu.goodlike.oblivion;

import eu.goodlike.oblivion.command.ButWhatDoesThisMean;
import eu.goodlike.oblivion.command.ItsAllOver;
import eu.goodlike.oblivion.command.JustForget;
import eu.goodlike.oblivion.command.LowerTheGates;
import eu.goodlike.oblivion.command.RepeatHit;
import eu.goodlike.oblivion.command.Reset;
import eu.goodlike.oblivion.command.SetEnemy;
import eu.goodlike.oblivion.command.UndoLastHit;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

public interface Command {

  enum Name {
    ENEMY(SetEnemy::new),
    FORGET(JustForget::new),
    GO(LowerTheGates::new),
    HIT(RepeatHit::new),
    RESET(Reset::new),
    QUIT(ItsAllOver::new),
    UNDO(UndoLastHit::new),
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

  void execute();

}
