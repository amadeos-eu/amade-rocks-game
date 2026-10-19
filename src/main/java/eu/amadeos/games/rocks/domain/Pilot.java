package eu.amadeos.games.rocks.domain;

import eu.amadeos.gameengine.math.Angles;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.gameengine.random.SeededRandom;

/**
 * The computer player of the title screen. It turns towards the nearest rock and fires when its nose points
 * there. A computer could aim perfectly, even where a rock will be when the shot arrives, and would then
 * clear wave after wave for ever; so it aims at where the rock is now, and its hand shakes: every aim is
 * off by a little, by chance. It plays well enough to watch, and dies often enough to show the end.
 */
public final class Pilot {
    /** The pilot pulls the trigger at most this often, as a person would. */
    static final double FIRE_PAUSE = 0.3;
    /** The most the pilot's aim is off, in radians, either way. */
    static final double SHAKE = 0.25;
    /** Shoots only at rocks closer than this. */
    static final double RANGE = 300;

    /** Its own chance, apart from the game's: the pilot's shaking hand must not move the rocks. */
    private final SeededRandom random;
    private double sinceShot = FIRE_PAUSE;
    private double shake;

    public Pilot(long seed) {
        random = new SeededRandom(seed).fork();
    }

    // --8<-- [start:steer]
    public Steering steer(Game game, double seconds) {
        sinceShot += seconds;
        Ship ship = game.ship();
        if (ship == null) return Steering.NONE;
        Rock target = null;
        double nearest = Double.MAX_VALUE;
        for (Rock rock : game.rocks()) {
            double gap = Field.WRAP.distance(ship.position(), rock.position()) - rock.size().radius();
            if (gap < nearest) {
                nearest = gap;
                target = rock;
            }
        }
        if (target == null) return Steering.NONE;
        // Straight at the rock, as most people aim, and a little off.
        Vector toRock = Field.WRAP.delta(ship.position(), target.position());
        double aim = toRock.angle() + shake;
        boolean onTarget = Math.abs(Angles.difference(ship.angle(), aim)) < 0.06;
        boolean fire = onTarget && nearest < RANGE && sinceShot >= FIRE_PAUSE;
        if (fire) {
            sinceShot = 0;
            shake = (random.nextDouble() * 2 - 1) * SHAKE;    // the next aim is off by a new error
        }
        boolean thrust = nearest > 180 && ship.velocity().length() < 40 && onTarget;
        return new Steering(0, aim, thrust, fire, false);
    }
    // --8<-- [end:steer]
}
