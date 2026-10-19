package eu.amadeos.games.rocks.domain;

import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.gameengine.math.Wrap;

/** The field and the numbers of the game, in logical units and seconds. */
public final class Field {
    public static final double WIDTH = 640;
    public static final double HEIGHT = 400;
    // --8<-- [start:wrap]
    /** Space has no edges: what flies out on one side comes back on the other. */
    public static final Wrap WRAP = new Wrap(WIDTH, HEIGHT);
    // --8<-- [end:wrap]
    public static final Vector CENTRE = new Vector(WIDTH / 2, HEIGHT / 2);

    // --8<-- [start:ship]
    /** The ship's hit circle, a little smaller than its drawing, which is fairer. */
    public static final double SHIP_RADIUS = 8;
    /** Turning speed, radians per second: about two thirds of a turn. */
    public static final double TURN_SPEED = Math.toRadians(250);
    /** Acceleration while the engine fires, units per second². */
    public static final double THRUST = 260;
    public static final double MAX_SPEED = 260;
    /** The share of its speed the ship loses per second without thrust. Space has no friction, games do. */
    public static final double DRAG = 0.45;
    // --8<-- [end:ship]

    // --8<-- [start:bullets]
    /** Shots in the air at once: the pool's capacity, and a rule of the game. */
    public static final int MAX_BULLETS = 4;
    /** Units per second, on top of the ship's own speed. */
    public static final double BULLET_SPEED = 400;
    /** Seconds a shot flies: 360 units, a little more than half the field. */
    public static final double BULLET_LIFE = 0.9;
    // --8<-- [end:bullets]

    public static final int START_LIVES = 3;
    public static final int MAX_LIVES = 9;
    public static final int EXTRA_LIFE_EVERY = 10_000;
    /** After a crash, the next ship waits at least this long, and then until the centre is clear. */
    public static final double RESPAWN_SECONDS = 2;
    /** No rock may be closer than this to the centre when a new ship appears there. */
    public static final double SAFE_RADIUS = 90;
    /** How long the ship is gone in hyperspace. */
    public static final double HYPERSPACE_SECONDS = 0.6;
    /** The pause between a cleared wave and the next. */
    public static final double WAVE_PAUSE = 2.5;
    /** New rocks start at least this far from the ship. */
    public static final double SPAWN_DISTANCE = 160;

    private Field() {
    }

    /** Large rocks at the start of a wave: 4, 6, 8, 10, then 11. */
    public static int rocksInWave(int wave) {
        return Math.min(2 + 2 * wave, 11);
    }
}
