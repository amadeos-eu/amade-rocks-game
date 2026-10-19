package eu.amadeos.games.rocks.domain;

import eu.amadeos.gameengine.math.Vector;

/** Something that happened in a step; the user interface decides what it looks and sounds like. */
// --8<-- [start:event]
public sealed interface Event {
    record Fired(Vector at) implements Event {
    }

    /** A shot or the ship broke a rock of {@code size} at {@code at}. */
    record RockBroken(RockSize size, Vector at) implements Event {
    }

    /** The ship ran into a rock. */
    record ShipLost(Vector at) implements Event {
    }

    /** A new ship appeared in the centre. */
    record ShipReady(Vector at) implements Event {
    }

    record HyperspaceIn(Vector from) implements Event {
    }

    record HyperspaceOut(Vector to) implements Event {
    }

    record ExtraLife(int lives) implements Event {
    }

    record WaveStarted(int wave) implements Event {
    }

    record GameOver(GameResult result) implements Event {
    }
}
// --8<-- [end:event]
