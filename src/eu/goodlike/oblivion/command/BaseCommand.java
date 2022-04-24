package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Arena;
import eu.goodlike.oblivion.Command;
import eu.goodlike.oblivion.core.StructureException;
import eu.goodlike.oblivion.global.Write;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseCommand implements Command {

  protected abstract void performTask();

  @Override
  public final void setParams(String... parsedInput) {
    Collections.addAll(inputs, parsedInput);
  }

  @Override
  public final void setArena(Arena arena) {
    this.arena = arena;
  }

  @Override
  public final void execute() {
    try {
      performTask();
    }
    catch (StructureException e) {
      Write.line("Bad input: " + e.getMessage());
    }
    catch (Exception e) {
      Write.line("Unexpected exception: " + e);
      e.printStackTrace();
    }
  }

  protected final String input() {
    return String.join(" ", inputs);
  }

  protected final String input(int index) {
    return inputs.size() <= index ? "" : inputs.get(index);
  }

  protected final List<String> inputs = new ArrayList<>();
  protected Arena arena;

}
