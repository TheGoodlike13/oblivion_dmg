# TODO

Usage, examples

Motivation

## Limitations

Calculations are done using double floating point precision.
The resulting errors are trivial and unlikely to cause issues, but do keep it in mind.

For simplicity, resist & weakness of a factor are treated as the same type of effect.
As a result, carriers cannot have both.
It could be possible to make such a spell in-game, but it would be of no practical use.

### Caveats

Hit effects are applied all at once, including "drain hp".
If a single hit has multiple drain effects, e.g. weapon + poison,
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
2) I assume the hits are processed in order of bow -> arrow -> poison.
This seems like a logical order, but it's possible arrow and poison are switched.
To verify, I'd need an arrow with a weakness/resist effect.
Poison would not do, because it always stacks.
Under the assumption, this arrow has no effect on the poison because
just like the bow, it refreshes itself, removing the previous effect
before it has a chance to affect the poison.
