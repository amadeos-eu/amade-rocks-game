package eu.amadeos.games.rocks.domain;

import eu.amadeos.gameengine.collision.Circle;
import eu.amadeos.gameengine.math.Vector;

/** A rock drifting through space, turning slowly. Which of the rock shapes it has is only for the painter. */
public final class Rock {
    private final RockSize size;
    private final int shape;
    private final Vector velocity;
    private final double spin;
    private Vector position;
    private Vector previous;
    private double angle;
    private double previousAngle;

    public Rock(RockSize size, int shape, Vector position, Vector velocity, double angle, double spin) {
        this.size = size;
        this.shape = shape;
        this.position = position;
        this.previous = position;
        this.velocity = velocity;
        this.angle = angle;
        this.previousAngle = angle;
        this.spin = spin;
    }

    // --8<-- [start:move]
    /** Flies on in a straight line and turns; leaving the field on one side, it comes back on the other. */
    void move(double seconds) {
        previous = position;
        previousAngle = angle;
        position = Field.WRAP.wrap(position.plus(velocity.times(seconds)));
        angle += spin * seconds;
    }
    // --8<-- [end:move]

    public Circle hitArea() {
        return new Circle(position.x(), position.y(), size.radius());
    }

    public RockSize size() {
        return size;
    }

    public int shape() {
        return shape;
    }

    public Vector position() {
        return position;
    }

    /** Where the rock was before the last step, for drawing between two steps. */
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
}
