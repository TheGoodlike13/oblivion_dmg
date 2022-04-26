package eu.goodlike.oblivion.command;

public final class TimeToGo extends BaseCommand {

  @Override
  protected void performTask() {
    arena.lowerTheGates();
  }

}
