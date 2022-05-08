package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Command;
import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.StructureException;

import java.util.stream.Stream;

/**
 * Handles common logic for executing a command.
 * Also provides convenient access to input params.
 */
public abstract class BaseCommand implements Command {

  protected abstract void performTask();

  @Override
  public final void setParams(String... parsedInput) {
    this.inputs = parsedInput;
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

  protected String[] inputs;

  protected final String input() {
    return String.join(" ", inputs);
  }

  protected final String input(int index) {
    return inputs.length <= index ? "" : inputs[index];
  }

  protected final Stream<String> inputs() {
    return Stream.of(inputs);
  }

  protected final Stream<String> inputs(int start) {
    return inputs().skip(start);
  }

  protected final Stream<String> inputs(int start, int end) {
    int count = end - start;
    if (count <= 0) {
      return Stream.empty();
    }

    Stream<String> infiniteBlankWorks = Stream.generate(() -> "");
    return Stream.concat(inputs(start), infiniteBlankWorks).limit(count);
  }

  protected final Stream<String> args() {
    return inputs(1);
  }

}
