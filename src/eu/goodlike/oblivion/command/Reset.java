package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;

import static eu.goodlike.oblivion.Arena.THE_ARENA;

public final class Reset extends BaseCommand {

  @Override
  protected void performTask() {
    THE_ARENA.reset();
    RepeatHit.invalidate();
    Write.line("Everything has been reset.");
  }

}
