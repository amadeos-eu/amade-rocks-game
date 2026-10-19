package eu.amadeos.games.rocks.domain;

/** The three sizes of rocks: a hit breaks a rock into two of the next smaller size, the smallest into dust. */
// --8<-- [start:sizes]
public enum RockSize {
    LARGE(30, 20, 30, 60),
    MEDIUM(16, 50, 50, 95),
    SMALL(8, 100, 70, 130);

    private final double radius;
    private final int points;
    private final double minSpeed;
    private final double maxSpeed;

    RockSize(double radius, int points, double minSpeed, double maxSpeed) {
        this.radius = radius;
        this.points = points;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
    }

    public double radius() {
        return radius;
    }

    /** Points for breaking it: the smaller, the harder to hit, the more points. */
    public int points() {
        return points;
    }

    public double minSpeed() {
        return minSpeed;
    }

    public double maxSpeed() {
        return maxSpeed;
    }

    /** The size of the two pieces a hit leaves, or {@code null} for the smallest rocks. */
    public RockSize smaller() {
        return switch (this) {
            case LARGE -> MEDIUM;
            case MEDIUM -> SMALL;
            case SMALL -> null;
        };
    }
}
// --8<-- [end:sizes]
