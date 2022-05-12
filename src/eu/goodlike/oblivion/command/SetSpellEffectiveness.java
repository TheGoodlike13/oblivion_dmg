package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.StructureException;

import static eu.goodlike.oblivion.Global.Settings.EFFECTIVENESS;

/**
 * Sets spell effectiveness for all spells.
 * This affects already queued hits as well.
 * Spell effectiveness below 100 occurs when wearing light or heavy armor in-game.
 */
public final class SetSpellEffectiveness extends BaseCommand {

  @Override
  protected void performTask() {
    int effectiveness = StructureException.natOrThrow(input(1), "spell effectiveness");
    EFFECTIVENESS = effectiveness;
    Write.line("Spell effectiveness has been set to <" + effectiveness + ">");
  }

}
