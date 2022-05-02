# Oblivion spell stacking calculator

Calculates the amount of damage done by magical effects and poisons when using
the mechanic of spell stacking as described here:
https://en.uesp.net/wiki/Oblivion:Spell_Making#Spell_Stacking

Strictly speaking, it performs a quick simulation of casting spells and/or
performing attacks with magical weapons & poisons.

This is a command line application and requires JDK 8+ to build & run.

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

P.S. I may have gone a little insane half way through the project.
Please excuse any code or comments that seems a little too avant-garde.

## Limitations

Physical damage is ignored.
The focus is on spell stacking and using enchanted weapons or poisons as finishers.

Random effects that nullify magic are ignored.
This includes absorption and reflection.
These sort of effects dip too far into simulation territory.
I'm more interested in calculations :)

Calculations are done using double floating point precision.
The resulting errors are trivial and unlikely to cause issues, but do keep it in mind.

For simplicity, resist & weakness of a factor are treated as the same type of effect.
As a result, items or spells cannot have both.
It could be possible to make such a spell in-game, but it would be of no practical use.

The core logic allows multiple spells with the same name that stack.
This is not possible in-game, as spell names have to be unique (albeit not case sensitive).
However, the parsing logic ensures all spells and other items will have a unique reference.
This prevents creating two weapons with the same name, which is possible in-game.
For the record, such weapon effects would stack in-game.

Targets can have permanent effects to simulate innate weakness or resist due to
race or equipment.
Drain works too, although all it does is effectively reduce max hp from the start.
Damage effects are ignored.
While possible, they are not applicable to combat situations, and would only
add to code complexity.
