package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;

import static eu.goodlike.oblivion.Global.THE_ARENA;

/**
 * Gives a rough explanation with some examples how to use the application.
 */
public final class SomebodyHelp extends BaseCommand {

  @Override
  protected void performTask() {
    Write.line("To perform a calculation, select an enemy, queue some hits and type 'go'.");
    Write.separator();
    Write.line("Enemy examples:");
    Write.line("'enemy 100' -> enemy with 100 HP");
    Write.line("'enemy 50 50rm -> enemy with 50 HP and 50% resistance to magic");
    Write.line("'enemy 1000 :dagon' -> enemy with 1000 HP called 'dagon'");
    Write.line("Note: all names must be unique. Names that appear in the prepared file will be rejected.");
    Write.line("'enemy $dagon' -> enemy in memory called 'dagon' (either from prepared file or previous command)");
    Write.separator();
    Write.line("Hit examples:");
    Write.line("'+m 10m' -> melee for 10 magic damage over 1s");
    Write.line("'+s 10fr' -> spell for 10 frost damage over 1s");
    Write.line("'+st 100d10' -> staff for 100 drain life for 10s");
    Write.line("'+b 1f +a 1s +p 100wp' -> bow for 1 fire damage over 1s + arrow for 1 shock damage over 1s + poison for 100 weakness to poison for 1s");
    Write.line("'+m 1m :melee +p 1m :poison' -> melee for 1 magic damage for 1s called 'melee' + poison for 1 magic damage for 1s called 'poison'");
    Write.line("Note: all names must be unique. Names that appear in the prepared file will be rejected.");
    Write.line("'+s 100wf5s 100wm5s :weakness1' -> spell for 100 weakness to fire for 5s + 100 weakness to magic for 5s called 'weakness1'");
    Write.line("'$weakness1' -> spell/weapon/item in memory called 'weakness1' (either from prepared file or previous command)");
    Write.line("'$weakness1 :weakness2' -> copy of spell/weapon/item in memory called 'weakness1' renamed as 'weakness2'");
    Write.line("Note: each name corresponds to exactly the same spell/weapon/item. Repeated hits will replace effects.");
    Write.line("Copies are considered different, just with the same set of effects, so they will stack.");
    Write.line("Finally, all unnamed spells/weapons/items will be given a number as their name to be able to refer to them.");
    Write.line("'$some_melee $some_poison' -> melee & poison in memory called 'some_melee' and 'some_poison' respectively");
    Write.line("'$some_melee +p 1m' -> melee in memory called 'some_melee' + poison for 1 magic damage for 1s");
    Write.line("'+p 1m' -> melee with no effects + poison for 1 magic damage for 1s");
    Write.line("'#1' -> repeat the first hit from this session");
    Write.line("'#1 x10' -> repeat the first hit from this session 10 times");
    Write.separator();
    Write.line("Administrative examples:");
    Write.line("'level 50' -> set player level to 50 (affects leveled monster HP)");
    Write.line("'difficulty 100' -> set in-game difficulty slider to 100");
    Write.line("'spell_effect 95' -> set spell effectiveness to 95%");
    Write.line("'wait 0.5' -> forces the hits to be at least 0.5s apart (reduces overlap)");
    Write.line("'undo' -> removes last added hit, if any");
    Write.line("'undo 5' -> removes 5 last added hits, if any");
    Write.line("'forget $mistake' -> removes everything from memory called exactly 'mistake' (enemies, spells & hits)");
    Write.line("'refresh' -> clears all added hits, updates the enemy (e.g. if player level was changed)");
    Write.line("'reset' -> clears absolutely everything (almost equivalent to restarting the application)");
    Write.line("Note: if you change the settings or prepared files, they will not be re-loaded. Use 'restart' for that.");
    Write.line("'restart' -> clears absolutely everything (equivalent to restarting the application)");
    Write.line("'go' -> do the calculation; after it is done, the hits are cleared and need to be set again");
    Write.line("'quit' -> close the application");
    Write.separator();
    Write.line("Every reference, including command names can be used via prefix (e, en, enemy).");
    Write.line("In cases where prefixes match multiple things, alphabetic order takes precedence.");
    Write.line("That's about it, have fun!");

    THE_ARENA.refresh();
  }

}
