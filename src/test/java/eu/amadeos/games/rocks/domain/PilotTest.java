package eu.amadeos.games.rocks.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PilotTest {
    private static final double STEP = 1.0 / 120;

    // --8<-- [start:test-demo]
    @Test
    void thePilotClearsWavesUntilItsShipsAreGone() {
        int best = 0;
        int waves = 0;
        for (long seed = 1; seed <= 5; seed++) {
            Game game = new Game(seed);
            Pilot pilot = new Pilot();
            for (int i = 0; i < 10 * 60 * 120 && game.phase() != Game.Phase.OVER; i++) {
                game.update(STEP, pilot.steer(game, STEP));
            }
            best = Math.max(best, game.score());
            waves = Math.max(waves, game.wave());
        }
        assertTrue(best >= 3000, "best " + best);
        assertTrue(waves >= 2, "reached wave " + waves);
    }
    // --8<-- [end:test-demo]
}
