package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;

import static eu.goodlike.oblivion.Arena.THE_ARENA;

public final class Reset extends BaseCommand {

  @Override
  protected void performTask() {
    Write.separator();

    THE_ARENA.reset();
    RepeatHit.invalidate();
    SetHit.invalidate();
    Write.line("Everything has been reset.");
  }

}
