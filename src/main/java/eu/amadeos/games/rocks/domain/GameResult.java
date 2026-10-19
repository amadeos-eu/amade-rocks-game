package eu.amadeos.games.rocks.domain;

/** How a game ended: the points, and the wave the last ship was lost in. */
public record GameResult(int score, int wave) {
}
