package eu.amadeos.games.rocks.domain;

import eu.amadeos.gameengine.math.Angles;
import eu.amadeos.gameengine.math.Vector;

/**
 * The computer player of the title screen. It turns towards the nearest rock, aims where the rock will be
 * when the shot arrives, and fires when the nose points there. A rock coming too close sends it into
 * hyperspace. It plays well enough to watch, and dies often enough to show the end.
 */
public final class Pilot {
    /** The pilot pulls the trigger at most this often, as a person would. */
    static final double FIRE_PAUSE = 0.18;
    /** Shoots only at rocks closer than this. */
    static final double RANGE = 300;

    private double sinceShot = FIRE_PAUSE;

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
        if (nearest < Field.SHIP_RADIUS + 6 && approaching(ship, target)) {
            return new Steering(0, Double.NaN, false, false, true);
        }
        // Lead the target: where will it be when a shot flying from here gets there?
        Vector toRock = Field.WRAP.delta(ship.position(), target.position());
        double flight = toRock.length() / Field.BULLET_SPEED;
        Vector aimAt = toRock.plus(target.velocity().minus(ship.velocity()).times(flight));
        double aim = aimAt.angle();
        boolean onTarget = Math.abs(Angles.difference(ship.angle(), aim)) < 0.06;
        boolean fire = onTarget && nearest < RANGE && sinceShot >= FIRE_PAUSE;
        if (fire) sinceShot = 0;
        boolean thrust = nearest > 180 && ship.velocity().length() < 40 && onTarget;
        return new Steering(0, aim, thrust, fire, false);
    }
    // --8<-- [end:steer]

    /** True if the rock and the ship are getting closer. */
    private static boolean approaching(Ship ship, Rock rock) {
        Vector toRock = Field.WRAP.delta(ship.position(), rock.position());
        return toRock.dot(rock.velocity().minus(ship.velocity())) < 0;
    }
}
