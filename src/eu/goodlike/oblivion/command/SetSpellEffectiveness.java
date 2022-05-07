package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;
import eu.goodlike.oblivion.core.StructureException;

import static eu.goodlike.oblivion.Global.Settings.EFFECTIVENESS;

public final class SetSpellEffectiveness extends BaseCommand {

  @Override
  protected void performTask() {
    int effectiveness = StructureException.natOrThrow(input(1), "spell effectiveness");
    EFFECTIVENESS = effectiveness;
    Write.line("Spell effectiveness has been set to <" + effectiveness + ">");
  }

}
