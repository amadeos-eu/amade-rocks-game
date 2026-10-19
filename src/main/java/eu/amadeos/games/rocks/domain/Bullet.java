package eu.amadeos.games.rocks.domain;

import eu.amadeos.gameengine.math.Vector;

/**
 * A shot. Shots live in a pool: the game makes the four of them once, and {@link #launch} makes one like new
 * each time it is fired. So a shot is mutable, unlike the vectors it holds.
 */
// --8<-- [start:bullet]
public final class Bullet {
    private Vector position = Vector.ZERO;
    private Vector previous = Vector.ZERO;
    private Vector velocity = Vector.ZERO;
    private double life;

    /** Sends the shot off from {@code position}, as if it were new. */
    void launch(Vector position, Vector velocity) {
        this.position = position;
        this.previous = position;
        this.velocity = velocity;
        this.life = Field.BULLET_LIFE;
    }

    void move(double seconds) {
        previous = position;
        position = Field.WRAP.wrap(position.plus(velocity.times(seconds)));
        life -= seconds;
    }

    /** Flown its distance, or used up on a rock: back into the pool. */
    boolean expired() {
        return life <= 0;
    }

    void spend() {
        life = 0;
    }
    // --8<-- [end:bullet]

    public Vector position() {
        return position;
    }

    public Vector previous() {
        return previous;
    }
}
