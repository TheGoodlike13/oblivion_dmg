package eu.goodlike.oblivion.command;

public final class ButWhatDoesThisMean extends BaseCommand {

  @Override
  protected void performTask() {
    write("No idea what <" + input() + "> is supposed to mean.");
  }

}
