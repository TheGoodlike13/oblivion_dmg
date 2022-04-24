package eu.goodlike.oblivion;

import eu.goodlike.oblivion.core.StructureException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class Command {

  public enum Name {
    ENEMY,
    QUIT;

    public boolean matches(String input) {
      return StringUtils.startsWithIgnoreCase(name(), input);
    }

    public static Optional<Name> find(String input) {
      return Stream.of(values())
        .filter(name -> name.matches(input))
        .findFirst();
    }
  }

  protected abstract void performTask();

  protected abstract void write(String line);

  public final void setParams(String... parsedInput) {
    Collections.addAll(inputs, parsedInput);
  }

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

  protected final String input() {
    return String.join(" ", inputs);
  }

  protected final String input(int index) {
    return inputs.size() <= index ? "" : inputs.get(index);
  }

  private final List<String> inputs = new ArrayList<>();

}
