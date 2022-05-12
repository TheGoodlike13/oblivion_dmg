package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Parse;
import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.parse.ParseEffector;

import static eu.goodlike.oblivion.Global.Settings.PARSE_MODE;

/**
 * Sets the parsing mode for effectors.
 * See {@link ParseEffector.Mode} for more info.
 */
public final class SetParseMode extends BaseCommand {

  @Override
  protected void performTask() {
    ParseEffector.Mode mode = Parse.mode(input(1));
    PARSE_MODE = mode;
    Write.line("Parse mode has been set to <" + mode + ">");
  }

}
