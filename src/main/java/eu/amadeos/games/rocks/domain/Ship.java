package eu.amadeos.games.rocks.domain;

import eu.amadeos.gameengine.math.Angles;
import eu.amadeos.gameengine.math.Vector;

/** The player's ship: where it is, how fast it flies, where its nose points. */
public final class Ship {
    /** The nose's distance from the centre: shots start there. */
    public static final double NOSE = 12;

    private Vector position;
    private Vector previous;
    private Vector velocity = Vector.ZERO;
    private double angle;
    private double previousAngle;
    private boolean thrusting;

    Ship(Vector position, double angle) {
        this.position = position;
        this.previous = position;
        this.angle = angle;
        this.previousAngle = angle;
    }

    // --8<-- [start:steer]
    /**
     * One step of flying. The ship turns by the keys, or towards the angle {@code steering.aim()} the mouse
     * points at; the engine pushes it the way its nose points; drag slows it down, and it never gets faster
     * than the limit.
     */
    void fly(Steering steering, double seconds) {
        previous = position;
        previousAngle = angle;
        double turn = Field.TURN_SPEED * seconds;
        if (!Double.isNaN(steering.aim())) angle = Angles.turnTowards(angle, steering.aim(), turn);
        else angle += steering.turn() * turn;
        thrusting = steering.thrust();
        if (thrusting) velocity = velocity.plus(Vector.ofAngle(angle, Field.THRUST * seconds));
        velocity = velocity.times(1 - Field.DRAG * seconds).limited(Field.MAX_SPEED);
        position = Field.WRAP.wrap(position.plus(velocity.times(seconds)));
    }
    // --8<-- [end:steer]

    /** Puts the ship somewhere else, at rest, without drawing a line from where it was. */
    void jumpTo(Vector position) {
        this.position = position;
        this.previous = position;
        this.velocity = Vector.ZERO;
        this.thrusting = false;
    }

    /** Where shots start: the tip of the nose. */
    public Vector nose() {
        return position.plus(Vector.ofAngle(angle, NOSE));
    }

    public Vector position() {
        return position;
    }

    public Vector previous() {
        return previous;
    }

    public Vector velocity() {
        return velocity;
    }

    public double angle() {
        return angle;
    }

    public double previousAngle() {
        return previousAngle;
    }

    public boolean thrusting() {
        return thrusting;
    }
}
