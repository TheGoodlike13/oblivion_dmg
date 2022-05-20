# Oblivion Spell Stacking Calculator

Calculates the amount of damage done by magical effects and poisons when using
the mechanic of spell stacking as described [here](https://en.uesp.net/wiki/Oblivion:Spell_Making#Spell_Stacking).

Strictly speaking, it performs a quick simulation of casting spells and/or
performing attacks with magical weapons & poisons.

This is a command line application and requires JDK 8+ to build & run.

## Usage example

>     run
>>     > Task :clean
>>     > Task :compileJava
>>     > Task :processResources
>>     > Task :classes
>>
>>     > Task :run
>>     -----
>>     Welcome to Oblivion Spell Stacking Calculator!
>>     Please select an enemy, cast some spells or perform attacks and GO!
>>     You can quit any time ;)
>>     -----
>>     Configurable settings:
>>     Player level: 30
>>     Difficulty slider: 100.0
>>     Spell effectiveness: 100
>>     Parse mode: MIXED
>>     -----
>     >> difficulty 50
>>     Difficulty slider has been set to <50.0>
>     >> enemy $meh
>>     You face the mehrunes dagon (1000 hp)
>>     FIRE   x0.00
>     >> $finger
>>     [#1] Next hit: <SPELL$finger_of_the_mountain> {SHOCK DMG 200 (instant)}
>     >> #1 x5
>>     [#1] Next hit: <SPELL$finger_of_the_mountain> {SHOCK DMG 200 (instant)}
>>     [#1] Next hit: <SPELL$finger_of_the_mountain> {SHOCK DMG 200 (instant)}
>>     [#1] Next hit: <SPELL$finger_of_the_mountain> {SHOCK DMG 200 (instant)}
>>     [#1] Next hit: <SPELL$finger_of_the_mountain> {SHOCK DMG 200 (instant)}
>>     [#1] Next hit: <SPELL$finger_of_the_mountain> {SHOCK DMG 200 (instant)}
>     >> go
>>     00.000 You cast <SPELL$finger_of_the_mountain>
>>     00.410 You hit with <SPELL$finger_of_the_mountain>
>>            Applied SHOCK DMG 200.0 (instant)
>>     01.140 You cast <SPELL$finger_of_the_mountain>
>>     01.550 You hit with <SPELL$finger_of_the_mountain>
>>            Applied SHOCK DMG 200.0 (instant)
>>     02.280 You cast <SPELL$finger_of_the_mountain>
>>     02.690 You hit with <SPELL$finger_of_the_mountain>
>>            Applied SHOCK DMG 200.0 (instant)
>>     03.420 You cast <SPELL$finger_of_the_mountain>
>>     03.830 You hit with <SPELL$finger_of_the_mountain>
>>            Applied SHOCK DMG 200.0 (instant)
>>     04.560 You cast <SPELL$finger_of_the_mountain>
>>     04.970 You hit with <SPELL$finger_of_the_mountain>
>>            Applied SHOCK DMG 200.0 (instant)
>>            The mehrunes dagon has died. Breakdown:
>>                <SPELL$finger_of_the_mountain> SHOCK DMG: 1000.00
>>     05.700 You cast <SPELL$finger_of_the_mountain>
>>     06.110 You hit with <SPELL$finger_of_the_mountain>
>>            Applied SHOCK DMG 200.0 (instant)
>>     The mehrunes dagon took a total of 1200.0 damage (200.0 overkill).
>>            Overkill by effect:
>>                <SPELL$finger_of_the_mountain> SHOCK DMG: 200.00
>>     -----
>>     You face the mehrunes dagon (1000 hp)
>>     FIRE   x0.00
>     >> q
>>     BUILD SUCCESSFUL in 39s
>>     4 actionable tasks: 4 executed

## Motivation

I was watching a [let's play of Oblivion by VoicesFromTheDark](https://www.youtube.com/playlist?list=PLayzTpaf6B4BGUFgskpn5vmV1RXXH3Wgt).

It's a challenge run, done at 100% difficulty from the start, trying to complete
most unique interactions in the game (quests, clear caves, collect nirnroots, etc.)

Without going too much into spoilers, the plan to manage with the ever increasing
difficulty of enemies was to create spells which weaken the enemy and stack with
each other.
I was interested by this mechanic, so I looked it up on the wiki.
While I found some information, it was not as extensive as I'd have liked.
Ideally, I'd love some sort of an online calculator, like the [neat alchemy one](https://en.uesp.net/oblivion/alchemy/alc_calc.php).

I could not find anything that fit the bill, so I wrote my own command line app.
In particular, I was interested in questions like:
1) How does difficulty affect the usefulness of spell stacking?
2) How much damage do you lose by using inefficient effect order?
3) What kind of effects would you want on a weapon finisher?
4) Poison is OP. Yes, this is not a question.

Anyway, I installed Oblivion and tried this out.
This application has been implemented using data from trying various items, spells
and their combinations out in-game, so it should be pretty accurate.
See below for any limitations or caveats.

Plus, it's been a while since I did some nice and refreshing JAVA programming.
So that's a bonus.

P.S. I may have gone a little insane half way through the project.
Please excuse any code or comments that seem a little too avant-garde.

## Setup & running

As long as you have successfully installed JDK 8+, running 'run' in command line
should work. Gradle will download itself and run the application.

You can adjust files in 'config' directory to change settings or add your own
prepared enemies, items or spells. However, if you do this while the application
is running, you will have to restart (manually or via [restart](#restart)).
See [limitations](#limitations) below.

In most cases, anything you can add to the file you can also manually enter
when application is running. The only exception is duplicate effect types,
which are allowed for files only by default. See [parse mode](#parsemode) setting.

## Settings

#### level

Default player level. Leveled enemies will use this to adjust their HP.
Should be a positive integer.

#### difficulty

Equivalent to in-game difficulty slider.
Any double is allowed.
To be faithful to the game, it should be in range [0, 100].

#### effectiveness

Spell effectiveness, which usually lowers due to wearing armor.
Should be a positive integer.
To be faithful to the game, it should never exceed 100.

#### parse.mode

Determines whether duplicate effect types are allowed for items and spells.
Should match 'lenient', 'mixed' or 'strict'.
Lenient allows duplicate effect types everywhere.
Strict forbids them everywhere.
Mixed, the default, allows them in files, but forbids them for user input.

#### tick

Simulation tick speed in seconds.
Should be a small positive double.

#### rampage

How many seconds to continue attacking after target is dead.
Should be zero or any positive double.
Values in the vicinity of Double#MAX_VALUE may produce unexpected results.
Please keep it reasonable.

#### prepared.{type}

Config filename for a certain type of prepared input.
Blank filenames will be ignored.

Types: 'enemies', 'items', 'spells'.

#### {armament}.swap
#### {armament}.combo

Timing configuration for weapon types or spells.

Swap indicates how long it takes to switch to that type of weapon.
Should be a positive double.
Spells ignore swap - it is always 0.

Combo indicates swing and cooldown timings.
Should be a list of positive double pairs.
The pairs should be separated by '_': (speed_cooldown).
The items of the list should be separated by ';': (combo1; combo2).

See bottom of [limitations](#limitations) for more explanation.

Armaments: 'melee', 'bow', 'staff', 'spell'.

## Commands

All commands (and references to anything in general) use prefix matching.
That means that 'e', 'en', 'ene', 'enem' and 'enemy' are treated the same as inputs.
If multiple things match some prefix, alphabetic order takes precedence.
This means 'f' will match 'fire' before 'frost'.

### Arguments

Arguments for commands are handled one of a few ways:
1. No arguments are expected. They are ignored.
2. A specific amount of arguments are expected. They may be optional. The rest are ignored.
3. There is no limit to arguments. All of them are parsed and interpreted in some way.

When all arguments are considered, but multiple of them match some pattern,
the last argument will be used and the rest ignored.
The only exception to this is enemy HP, which will only ever accept one numeric argument.

Arguments are often identified by prefix or suffix.
Commonly used argument types:
1. ':' for names when saving them.
2. '$' for names when retrieving them.
3. '#' for referencing hits specifically.

In all cases names should be unique within their category:
1. Enemies.
2. Effectors. This includes both items and spells with effects.
3. Hits. This is automatic, as all hits are assigned a unique number.

Effects are parsed as arguments like this:

{magnitude}{effect type}{duration}

Magnitude is just a positive integer indicating the strength of the effect.

Effect type is either 'drain', 'weakness/resist' or damage indicated by element.
All of these can be referred to by prefixes:
1. 100d : 100 drain life 
2. 25wfr: 25% weakness to frost 
3. 50rp : 50% resist poison 
4.  6m  :   6 magic damage 

Finally, duration is optional and defaults to 1s.
You can use '0s' to indicate instant spells.
The number should always be an integer.
's' itself is optional.

### List of commands

#### enemy *:name hp* **multiplier [min max] effect*...

Sets the enemy to attack.

':' prefix indicates enemy name.
Giving enemy a name will store it in memory for use within the session.

First numeric value is considered enemy HP.
It must be positive integer and is required.
For leveled enemies this is their HP for levels in range [1, min].

'*' prefix indicates the HP multiplier for leveling.
It must be a positive integer, but is optional.
Enemies without this argument are simply not leveled.

'[' prefix indicates minimum level.
It must be a positive integer. Defaults to 1.

']' suffix indicates maximum level.
It must be a positive integer larger than minimum level. Defaults to 2^31 - 1.
For levels above this one, enemy HP will simply cease to increase.

All remaining arguments will be parsed as permanent effects on the enemy.
Damage effects will be ignored. Drain effects will just reduce max HP.
This should be used for innate resistances and weaknesses.

#### enemy *$name*

Same command as above, but instead of using custom values, fetches enemy from
the memory using given name. The name is required.

#### *$name :copy_name* ... *+category :name effect*... ...

Enqueues the next hit by combining effectors.
An effector is an item or a spell which can have effects.

If the effector is referred to by name, it is fetched from memory.
If a different name is provided for this effector, it is copied and stored
into memory as well.

In the other case, a new effector is created.
It will be stored into memory with either a given name, or a generated integer.

The resulting hit must be a valid combination in-game:
1. Melee strike.
2. Melee strike with poison.
3. Bow shot with arrow.
4. Bow shot with arrow and poison.
5. Staff invocation.
6. Spell cast.

For poisons, bows and arrows, the other effectors are optional.
If none were provided, the application will assume they simply have no
magic effects.

To use exactly the same effector as part of the hit, either duplicate the hit
itself, or refer to it by its name.

If this is unclear, consider using [help](#help) command for some examples.

#### *#hit*... *xTimes*... ...

Repeats hits by reference.

'#' prefix indicates a hit to repeat.

'x' prefix indicates how many times should the previous reference be repeated:
>     #1      repeat first hit in the session
>     #1 x1   repeat first hit in the session once (same as above)

>     #1 #1   repeat first hit in the session twice
>     #1 x2   repeat first hit in the session twice (same as above)

#### level *player_level*

Sets the level of the player.
It must be positive integer and is required.
This will not affect the settings file, only this specific session.

This adjusts HP of leveled enemies to match.
Does not affect already selected enemies.
Use [refresh](#refresh) to achieve this.

#### difficulty *slider_position*

Sets the in-game difficulty slider.
It can be any double, but is required.
To be faithful to the game, it should be in range [0, 100].
This will not affect the settings file, only this specific session.

This adjusts all damage which comes from the player itself.
Notably does not affect poisons.

#### spell_effect *spell_effectiveness*

Sets the effectiveness of spells.
It must be a positive integer and is required.
To be faithful to the game, it should never exceed 100.
This will not affect the settings file, only this specific session.

This adjusts magnitudes of all spells as if the player was wearing armor.
Queued spells also get adjusted.

#### parse *mode*

Sets the parse mode for duplicate effect types.
It must match 'lenient', 'mixed' or 'strict'.
This will not affect the settings file, only this specific session.

Lenient allows duplicate effect types everywhere.
Strict forbids them everywhere.
Mixed, the default, allows them in files, but forbids them for user input.

#### wait *duration*

Ensures at least some time passes between hits.
Useful when spamming attacks with a very fast weapon.
Takes into account time wasted by swapping and cooldowns.

The duration should be a non-negative double.

#### go

Proceeds with the simulation using selected enemy and queued hits.
After it is done, the hits are no longer queued,
but the enemy is still selected & refreshed.

#### help

Gives a rough explanation of all commands and gives a few examples.

#### quit

Closes the application.

#### undo *amount*

Removes previously enqueued hits, if any.

Amount allows removing multiple hits at once.
It must be a positive integer and is optional, defaulting to 1.

#### forget *$name*...

Removes everything with exact 'name' from memory.
This includes hits, enemies and effectors.
This does not include things whose 'name' is merely a prefix.
Avoid using with integers, as that might cause unexpected results.

#### refresh

Removes all enqueued hits and updates selected enemy level (if applicable).

#### reload

Reloads all caches.
This clears the memory completely, then re-fills it with prepared files.

If you've made changes to the files before using this command, they will not
be visible. Use [restart](#restart) for that.

#### reset

Reloads all caches, settings, etc.

If you've made changes to the files before using this command, they will not
be visible. Use [restart](#restart) for that.

#### restart

Reloads all caches, settings, etc.

If you're using 'gradle' or 'run' to run the application, this will also
load all changes done to 'config' files while the application was running.

## Limitations

Updating settings or prepared files requires a restart.
This is due to the way the application is run by gradle.
It copies the files elsewhere and uses those copies.
It is possible to manually force gradle to re-copy the files.
If you are using 'gradle' (or 'run'), 'restart' command does this automatically.

I use Windows. This means the experience of using the application is tailored for that.
Since I had setup a linux VM recently, I tried it there, but the console is atrocious.
It treats arrow keys like some kind of text input... Anyway, I'm not going to waste
time fixing a limitation on an entire OS I don't use. It works fine for me.

Physical damage is ignored.
The focus is on spell stacking and using enchanted weapons or poisons as finishers.

Random effects that nullify magic are ignored.
This includes absorption and reflection.
These sort of effects dip too far into simulation territory.
I'm more interested in calculations :)

Healing is not implemented.
I feel like it would add to the complexity of the application needlessly.
Passive effects like troll regeneration should be negligible in most scenarios.
I'd expect them to be on par with physical damage, which is also ignored.

Absorb health is not explicitly implemented.
Magic damage is exactly the same from point of view of the calculations
we are doing, so you can use that as stand-in.

Permanent damage effects are ignored.
They are simply not applicable to any real combat scenario.
Similar to healing, it would only add to the complexity of the code.

Resist and weakness are treated as the same type of effect.
As a result, items or spells cannot have both under strict parsing rules.
It is possible to make such a spell in-game, but it would be of no practical use.
Also, due to the way effects are processed, it is very non-intuitive.

All items and spells must have a unique name.
This allows them to be uniquely referenced without issue when planning attacks.
This limitation exists at the parser level, so the code itself permits this.

Player level only determines HP of the enemy.
In-game it might prevent certain enemies from spawning.
This application does not care about that.
It does place a lower bound on enemy HP as if you've met them at the nearest appropriate level.
This assumes that the enemy data is entered properly or prepared data is used.
The user is free to create whatever funky enemy they want.

Calculations are done using double floating point precision.
The resulting errors are trivial and unlikely to cause issues, but do keep it in mind.

### Weapon & casting timing (speed, cooldowns, swapping)

Because all crafted item effects take at least 1 second to fully appreciate,
repeated attacks with the same weapon or spell can overlap with itself.
This application uses a very basic and simplified model to achieve this effect.
Each type of weapon is given an attack duration, cooldown and swap duration.
These are periods of time where the effects are allowed to tick on the enemy
before a follow-up hit is made.

Swap duration is incurred when you've attacked using one weapon, but a follow-up
hit must use a different weapon. Intuitively it's the amount of time it takes
to switch to the weapon in-game.

Attack duration is always incurred right before the hit. This is the amount of
time between user input and the weapon actually hitting the enemy.

Cooldown is incurred following an attack. This is the amount of time it takes
your weapon to 'reset' to normal position after the enemy has been hit.
As a result, there are cases where this cooldown will be ignored (for example,
a weapon attack immediately after casting a spell).

I tried to time various weapons and spells, their swap durations, attack animations
and so on using recorded video and sound cues. I've come to the conclusion that
these must be some factor (likely weight) that influences these durations.
Furthermore they seemed a little random, although only within a few frames.

In any case, I decided to use one set of timings for melee weapons, one for bows,
one for staffs and one for spells. The durations are approximate averages from my
timing attempts. I used a steel dagger for melee, although a good case can be
made that a 2-handed weapon would be preferable as a mage due to how shields
work with 2-handers. In any case, if the timings are too fast for you, feel free
to time them yourself and adjust the settings :)
