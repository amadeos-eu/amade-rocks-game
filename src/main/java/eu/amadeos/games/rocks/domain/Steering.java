package eu.amadeos.games.rocks.domain;

/**
 * What the player (or the computer) wants in one step. The rules never see keys or a mouse, only this.
 *
 * @param turn       -1 to turn left (anticlockwise), +1 right, 0 not at all
 * @param aim        an angle to turn towards, for the mouse; {@code NaN} to use {@code turn}
 * @param thrust     the engine fires while this is true
 * @param fire       a shot, if one is left in the pool
 * @param hyperspace jump to a random place
 */
public record Steering(int turn, double aim, boolean thrust, boolean fire, boolean hyperspace) {
    public static final Steering NONE = new Steering(0, Double.NaN, false, false, false);
}
