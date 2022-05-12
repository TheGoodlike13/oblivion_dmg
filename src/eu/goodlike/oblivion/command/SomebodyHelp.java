package eu.goodlike.oblivion.command;

import eu.goodlike.oblivion.Write;

import static eu.goodlike.oblivion.Global.THE_ARENA;

/**
 * Gives a rough explanation with some examples how to use the application.
 */
public final class SomebodyHelp extends BaseCommand {

  @Override
  protected void performTask() {
    Write.line("To perform a calculation, select an enemy, queue some hits and type 'go'.");      // console boundary
    Write.line("All commands or references operate on the prefix rule.");                         //
    Write.line("This means the following are equivalent: e, en, ene, enem, enemy");               //
    Write.line("If there are multiple matches, alphabetic order is assumed.");                    //
    Write.line("This means 'f' will always match 'fire' before 'frost'.");                        //
    Write.separator();                                                                            //
    Write.line("Enemies will be receiving your hits.");                                           //
    Write.line("Each enemy has some HP and may have some permanent effects.");                    //
    Write.line("It is also possible for an enemy to have leveled HP.");                           //
    Write.line("Every named enemy will be stored in memory.");                                    //
    Write.line("Names in memory must be unique, including prepared enemies.");                    //
    Write.separator();                                                                            //
    Write.line("Enemy examples:");                                                                //
    Write.line("'enemy 100'            enemy with 100 HP");                                       //
    Write.line("'enemy 50 50rm         enemy with 50 HP and 50% resistance to magic");            //
    Write.line("'enemy 15 *5 [5  9]'   enemy with 15 HP at level 5 up to 35 HP at level 9");      //
    Write.line("                       Levels 1-4 will have 15HP");                               //
    Write.line("                       Levels 10+ will have 35HP");                               //
    Write.line("'enemy 1000 :dagon'    enemy with 1000 HP named 'dagon'");                        //
    Write.line("'enemy $dagon'         enemy named 'dagon' from memory");                         //
    Write.separator();                                                                            //
    Write.line("Hits are made from combinations of effectors.");                                  //
    Write.line("Every spell, weapon and item is an effector.");                                   //
    Write.line("That simply means they can have effects.");                                       //
    Write.line("Every hit and effector will be stored in memory.");                               //
    Write.line("Names in memory must be unique, including all prepared items and spells.");       //
    Write.line("Effectors without names (as well as all hits) will be given a numeric name.");    //
    Write.separator();                                                                            //
    Write.line("Hit examples:");                                                                  //
    Write.line("'+m 10m'                      melee, 10 magic damage over 1s");                   //
    Write.line("'+s 10fr0s'                   spell, 10 frost damage (instant)");                 //
    Write.line("'+st 100d10'                  staff, 100 drain life for 10s");                    //
    Write.line("'+b 1f +a 1s +p 100wp'        bow, 1 fire damage over 1s");                       //
    Write.line("                              +arrow, 1 shock damage over 1s");                   //
    Write.line("                              +poison, 100 weakness to poison for 1s");           //
    Write.line("'+m 1m :melee +p 1m :poison'  melee, 1 magic damage for 1s as 'melee'");          //
    Write.line("                              +poison, 1 magic damage for 1s as 'poison'");       //
    Write.line("'+s 100wf5s 100wm5s :weak1'   spell named 'weak1' with two effects:");            //
    Write.line("                              100 weakness to fire for 5s");                      //
    Write.line("                              100 weakness to magic for 5s");                     //
    Write.line("'$weak1'                      effector named 'weak1' from memory");               //
    Write.line("'$weak1 :weak2'               copy of effector named 'weak1' as 'weak2'");        //
    Write.line("'$m $p'                       effector named 'm' from memory");                   //
    Write.line("                              +effector named 'p' from memory");                  //
    Write.line("'$m +p 1m'                    effector named 'm' from memory");                   //
    Write.line("                              +poison, 1 magic damage for 1s");                   //
    Write.line("'+p 1m'                       melee with no effects");                            //
    Write.line("                              +poison, 1 magic damage for 1s");                   //
    Write.line("'#1'                          repeat the first hit from this session");           //
    Write.line("'#1 x10'                      repeat the first hit from this session 10 times");  //
    Write.separator();                                                                            //
    Write.line("Options and controls:");                                                          //
    Write.line("'level 50'          set player level to 50 (affects leveled enemy HP)");          //
    Write.line("                    Any selected enemy must still be manually refreshed");        //
    Write.line("'difficulty 100'    set in-game difficulty slider to 100");                       //
    Write.line("'spell_effect 95'   set spell effectiveness to 95%");                             //
    Write.line("'parse lenient'     allow duplicate effect types for items and spells");          //
    Write.line("'parse mixed'       allow duplicate effect types for items and spells from file");//
    Write.line("                    This is the default behavior");                               //
    Write.line("'parse strict'      forbid duplicate effect types for items and spells");         //
    Write.line("'wait 0.5'          forces the hits to be at least 0.5s apart (reduces overlap)");//
    Write.line("'go'                perform calculations using selected enemy and queued hits");  //
    Write.line("'help'              prints all this info again");                                 //
    Write.line("'quit'              close the application");                                      //
    Write.separator();                                                                            //
    Write.line("Commands which allow correcting mistakes:");                                      //
    Write.line("'undo'              removes last added hit, if any");                             //
    Write.line("'undo 5'            removes 5 last added hits, if any");                          //
    Write.line("'forget $mistake'   clears all values named 'mistake' (enemies, spells & hits)"); //
    Write.line("'refresh'           clears all queued hits, reloads enemy");                      //
    Write.line("'reload'            reloads caches");                                             //
    Write.line("'reset'             reloads everything, changes in 'config' ignored");            //
    Write.line("'restart'           reloads everything, ensures 'config' changes are visible");   //
    Write.line("                    Only works when using gradle or run.bat");                    //
    Write.separator();                                                                            //
    Write.line("For more details, please refer to README.md file.");                              //
    Write.line("That's about it, have fun!");                                                     //

    THE_ARENA.refresh();
  }

}
