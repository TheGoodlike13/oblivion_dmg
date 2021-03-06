package eu.goodlike.oblivion;

import eu.goodlike.oblivion.command.ButWhatDoesThisMean;
import eu.goodlike.oblivion.command.EnqueueHit;
import eu.goodlike.oblivion.command.ItsAllOver;
import eu.goodlike.oblivion.command.JustForget;
import eu.goodlike.oblivion.command.LowerTheGates;
import eu.goodlike.oblivion.command.NowJustHoldOnACottonPickinMinute;
import eu.goodlike.oblivion.command.Refresh;
import eu.goodlike.oblivion.command.Reload;
import eu.goodlike.oblivion.command.RepeatHit;
import eu.goodlike.oblivion.command.Reset;
import eu.goodlike.oblivion.command.Restart;
import eu.goodlike.oblivion.command.SetDifficulty;
import eu.goodlike.oblivion.command.SetEnemy;
import eu.goodlike.oblivion.command.SetLevel;
import eu.goodlike.oblivion.command.SetParseMode;
import eu.goodlike.oblivion.command.SetSpellEffectiveness;
import eu.goodlike.oblivion.command.SomebodyHelp;
import eu.goodlike.oblivion.command.UndoLastHit;

import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

/**
 * Represents a unit of work to be done following input.
 * Instances of this class should be used once following the input and then discarded.
 * <p/>
 * Please review implementations for details.
 */
public interface Command {

  /**
   * Identifiers for commands.
   * Most commands will be referenced using a prefix to their name.
   * <p/>
   * {@link EnqueueHit} & {@link RepeatHit} have no names.
   * They are recognized differently and parsed in its entirety as arguments.
   */
  enum Name {
    DIFFICULTY(SetDifficulty::new),
    ENEMY(SetEnemy::new),
    FORGET(JustForget::new),
    GO(LowerTheGates::new),
    HELP(SomebodyHelp::new),
    LEVEL(SetLevel::new),
    PARSE(SetParseMode::new),
    QUIT(ItsAllOver::new),
    REFRESH(Refresh::new),
    RELOAD(Reload::new),
    RESET(Reset::new),
    RESTART(Restart::new),
    SPELL_EFFECT(SetSpellEffectiveness::new),
    UNDO(UndoLastHit::new),
    WAIT(NowJustHoldOnACottonPickinMinute::new),
    WHAT(ButWhatDoesThisMean::new);

    public Command newCommand() {
      return factory.get();
    }

    public boolean matches(String input) {
      return startsWithIgnoreCase(remove(name(), '_'), remove(input, '_'));
    }

    Name(Supplier<Command> factory) {
      this.factory = factory;
    }

    private final Supplier<Command> factory;
  }

  /**
   * Sets the input for this command.
   * This includes the name, if it was used.
   * <p/>
   * Using a setter prevents having to pass them via constructor,
   * which would inevitably make any sub-classes bulkier.
   * With this, all commands have the default constructor.
   */
  void setParams(String... parsedInput);

  /**
   * Performs the unit of work related to the action.
   * Does not throw exceptions if at all possible.
   * Writes the expected or unexpected errors instead.
   */
  void execute();

}
