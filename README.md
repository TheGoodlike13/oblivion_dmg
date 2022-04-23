# Oblivion spell stacking calculator

Calculates the amount of damage done by magical effects and poisons when using
the mechanic of spell stacking as described here:
https://en.uesp.net/wiki/Oblivion:Spell_Making#Spell_Stacking

Strictly speaking, it performs a quick simulation of casting spells and/or
performing attacks with magical weapons & poisons.

This is a command line application and requires JDK 1.8+ to build & run.

## Usage examples

TO BE IMPLEMENTED

## Motivation

I was watching a let's play of Oblivion by VoicesFromTheDark:
https://www.youtube.com/playlist?list=PLayzTpaf6B4BGUFgskpn5vmV1RXXH3Wgt

It's a challenge run, done at 100% difficulty from the start, trying to complete
most unique interactions in the game (quests, clear caves, collect nirnroots, etc.)

Without going too much into spoilers, the plan to manage with the ever increasing
difficulty of enemies was to create spells which weaken the enemy and stack with
each other.
I was interested by this mechanic, so I looked it up on the wiki.
While I found some information, it was not as extensive as I'd have liked.
Ideally, I'd love some sort of an online calculator, like the neat alchemy one here:
https://en.uesp.net/oblivion/alchemy/alc_calc.php

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

## Limitations

Physical damage is ignored.
The focus is on spell stacking and using enchanted weapons or poisons as finishers.

Calculations are done using double floating point precision.
The resulting errors are trivial and unlikely to cause issues, but do keep it in mind.

For simplicity, resist & weakness of a factor are treated as the same type of effect.
As a result, items or spells cannot have both.
It could be possible to make such a spell in-game, but it would be of no practical use.

Targets can have permanent effects to simulate innate weakness or resist due to
race or equipment.
Drain works too, although all it does is effectively reduce max hp from the start.
Damage effects are ignored.
While possible, they are not applicable to combat situations, and would only
add to code complexity.

### Caveats

Effects of a single hit are applied all at once, including "drain hp".
If this hit has multiple drain effects, e.g. weapon + poison,
they will be bundled together.
This has no effect on calculations leading up to death, only overkill.
Normally, the overkill damage will not include any drains past death.

Anything that this application allows that is outside of the bounds of the base
game should be considered extra.
As a result, there are a few assumptions that I have no way of testing
without modding the game:
1) I've added a magic factor to weakness/resist effects.
To verify, I'd need a poison with such an effect.
Under the assumption, it is affected by magic resistance.
For reference, "drain hp" did have this factor as a poison.
Elemental damage did not.
2) I assume a hit is processed in order of bow -> arrow -> poison.
This seems like a logical order, but it's possible arrow and poison are switched.
To verify, I'd need an arrow with a weakness/resist effect.
Poison would not do, because it always stacks.
Under the assumption, this arrow has no effect on the poison because
just like the bow, it refreshes itself, removing the previous effect
before it has a chance to affect the poison.
