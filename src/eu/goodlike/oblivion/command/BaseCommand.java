package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Arena;
import eu.goodlike.oblivion.Command;
import eu.goodlike.oblivion.core.StructureException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseCommand implements Command {

  protected abstract void performTask();

  @Override
  public final void setParams(String... parsedInput) {
    Collections.addAll(inputs, parsedInput);
  }

  @Override
  public final void setWriter(Consumer<String> writer) {
    this.writer = writer;
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
      write("Bad input: " + e.getMessage());
    }
    catch (Exception e) {
      write("Unexpected exception: " + e.getMessage());
    }
  }

  protected final void write(String line) {
    writer.accept(line);
  }

  protected final String input() {
    return String.join(" ", inputs);
  }

  protected final String input(int index) {
    return inputs.size() <= index ? "" : inputs.get(index);
  }

  protected Arena arena;

  private final List<String> inputs = new ArrayList<>();
  private Consumer<String> writer;

}
