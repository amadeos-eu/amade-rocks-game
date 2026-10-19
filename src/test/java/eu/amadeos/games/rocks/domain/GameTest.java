package eu.amadeos.games.rocks.domain;

import eu.amadeos.gameengine.math.Vector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {
    private static final double STEP = 1.0 / 120;
    private static final Steering FIRE = new Steering(0, Double.NaN, false, true, false);
    private static final Steering THRUST = new Steering(0, Double.NaN, true, false, false);

    /** A game without rocks, but one far away, so no new wave starts: the test adds what it needs. */
    private static Game quiet() {
        Game game = new Game(1);
        game.clearRocks();
        game.putRock(still(RockSize.SMALL, 20, 20));
        return game;
    }

    private static Rock still(RockSize size, double x, double y) {
        return new Rock(size, 0, new Vector(x, y), Vector.ZERO, 0, 0);
    }

    /** Runs the game for {@code seconds} with the same steering in every step; returns all events. */
    private static List<Event> run(Game game, double seconds, Steering steering) {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < Math.round(seconds / STEP); i++) {
            game.update(STEP, steering);
            events.addAll(game.events());
        }
        return events;
    }

    private static long count(List<Event> events, Class<? extends Event> kind) {
        return events.stream().filter(kind::isInstance).count();
    }

    @Test
    void aNewGameHasFourLargeRocksAwayFromTheShip() {
        Game game = new Game(5);
        assertEquals(4, game.rocks().size());
        for (Rock rock : game.rocks()) {
            assertEquals(RockSize.LARGE, rock.size());
            assertTrue(Field.WRAP.distance(rock.position(), game.ship().position()) >= Field.SPAWN_DISTANCE);
        }
        assertEquals(Field.START_LIVES, game.lives());
        assertEquals(1, game.wave());
    }

    // --8<-- [start:test-flying]
    @Test
    void theShipTurnsThrustsAndDriftsToAStop() {
        Game game = quiet();
        Ship ship = game.ship();
        run(game, 0.5, new Steering(1, Double.NaN, false, false, false));
        assertEquals(-Math.PI / 2 + Field.TURN_SPEED * 0.5, ship.angle(), 1e-9, "turned right");
        run(game, 0.5, THRUST);
        double speed = ship.velocity().length();
        assertTrue(speed > 100, "faster: " + speed);
        assertEquals(ship.angle(), ship.velocity().angle(), 1e-9, "flies where the nose points");
        run(game, 5, Steering.NONE);
        assertTrue(ship.velocity().length() < speed / 5, "drag slowed it down");
    }

    @Test
    void theShipFliesOutAtTheTopAndComesBackAtTheBottom() {
        Game game = quiet();
        Ship ship = game.ship();    // in the centre, nose up
        boolean crossed = false;
        for (int i = 0; i < 3 * 120 && !crossed; i++) {
            game.update(STEP, THRUST);
            crossed = ship.position().y() > Field.CENTRE.y() + 50;
        }
        assertTrue(crossed, "came back from below");
        assertEquals(Field.CENTRE.x(), ship.position().x(), 1e-6, "on the same line");
    }
    // --8<-- [end:test-flying]

    @Test
    void theMouseTurnsTheShipTowardsItsAngleAndStops() {
        Game game = quiet();
        run(game, 2, new Steering(0, 0, false, false, false));
        assertEquals(0, game.ship().angle(), 1e-9, "nose to the right, no further");
    }

    // --8<-- [start:test-bullets]
    @Test
    void fourShotsAtMostAndThePoolRefills() {
        Game game = quiet();
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            game.update(STEP, FIRE);
            events.addAll(game.events());
        }
        assertEquals(4, count(events, Event.Fired.class), "the fifth and sixth click do nothing");
        assertEquals(4, game.bullets().size());
        run(game, Field.BULLET_LIFE, Steering.NONE);
        assertEquals(0, game.bullets().size(), "flown their distance");
        game.update(STEP, FIRE);
        assertEquals(1, game.bullets().size(), "and back in the pool");
    }
    // --8<-- [end:test-bullets]

    // --8<-- [start:test-break]
    @Test
    void aShotBreaksALargeRockIntoTwoMediumOnes() {
        Game game = quiet();
        game.putRock(still(RockSize.LARGE, Field.CENTRE.x(), Field.CENTRE.y() - 80));    // straight ahead
        game.update(STEP, FIRE);
        List<Event> events = run(game, 0.5, Steering.NONE);
        assertEquals(List.of(new Event.RockBroken(RockSize.LARGE, new Vector(Field.CENTRE.x(), Field.CENTRE.y() - 80))),
                events.stream().filter(Event.RockBroken.class::isInstance).toList());
        assertEquals(RockSize.LARGE.points(), game.score());
        assertEquals(2, game.rocks().stream().filter(r -> r.size() == RockSize.MEDIUM).count());
        assertEquals(0, game.bullets().size(), "the shot is used up");
    }

    @Test
    void aShotHitsARockThatReachesOverTheEdge() {
        Game game = quiet();
        // The ship in the centre shoots up. The rock's centre is near the bottom edge, but its upper part
        // reaches over it and shows at the top of the field.
        game.putRock(still(RockSize.LARGE, Field.CENTRE.x(), Field.HEIGHT - 5));
        game.update(STEP, FIRE);
        List<Event> events = run(game, 0.45, Steering.NONE);
        assertEquals(1, count(events, Event.RockBroken.class), "hit at the top, not only after the edge");
    }
    // --8<-- [end:test-break]

    @Test
    void smallRocksBreakIntoDust() {
        Game game = quiet();
        game.putRock(still(RockSize.SMALL, Field.CENTRE.x(), Field.CENTRE.y() - 50));
        int before = game.rocks().size();
        game.update(STEP, FIRE);
        run(game, 0.3, Steering.NONE);
        assertEquals(before - 1, game.rocks().size());
        assertEquals(RockSize.SMALL.points(), game.score());
    }

    // --8<-- [start:test-crash]
    @Test
    void aCrashCostsAShipAndTheNextWaitsForAClearCentre() {
        Game game = quiet();
        game.putRock(still(RockSize.MEDIUM, Field.CENTRE.x() + 10, Field.CENTRE.y()));
        game.update(STEP, Steering.NONE);
        assertEquals(1, count(game.events(), Event.ShipLost.class));
        assertNull(game.ship());
        assertEquals(Field.START_LIVES - 1, game.lives());
        assertEquals(Game.Phase.WAITING, game.phase());

        game.clearRocks();
        game.putRock(still(RockSize.LARGE, Field.CENTRE.x() + 60, Field.CENTRE.y()));
        run(game, Field.RESPAWN_SECONDS + 1, Steering.NONE);
        assertNull(game.ship(), "a rock sits next to the centre");

        game.clearRocks();
        game.putRock(still(RockSize.LARGE, 40, 40));
        List<Event> events = run(game, 0.1, Steering.NONE);
        assertEquals(1, count(events, Event.ShipReady.class));
        assertEquals(Field.CENTRE, game.ship().position());
        assertEquals(Game.Phase.PLAYING, game.phase());
    }
    // --8<-- [end:test-crash]

    @Test
    void theLastShipEndsTheGame() {
        Game game = quiet();
        List<Event> events = new ArrayList<>();
        for (int life = 0; life < Field.START_LIVES; life++) {
            while (game.ship() == null) {
                game.update(STEP, Steering.NONE);
                events.addAll(game.events());
            }
            game.putRock(still(RockSize.SMALL, Field.CENTRE.x(), Field.CENTRE.y()));
            game.update(STEP, Steering.NONE);
            events.addAll(game.events());
        }
        assertEquals(Game.Phase.OVER, game.phase());
        assertEquals(List.of(new Event.GameOver(new GameResult(3 * RockSize.SMALL.points(), 1))),
                events.stream().filter(Event.GameOver.class::isInstance).toList());
        run(game, 10, Steering.NONE);
        assertNull(game.ship(), "no more ships");
    }

    @Test
    void tenThousandPointsGiveAnExtraShip() {
        Game game = quiet();
        game.putScore(Field.EXTRA_LIFE_EVERY - 10);
        game.putRock(still(RockSize.LARGE, Field.CENTRE.x(), Field.CENTRE.y() - 80));
        game.update(STEP, FIRE);
        List<Event> events = run(game, 0.5, Steering.NONE);
        assertEquals(List.of(new Event.ExtraLife(Field.START_LIVES + 1)),
                events.stream().filter(Event.ExtraLife.class::isInstance).toList());
        assertEquals(Field.START_LIVES + 1, game.lives());
    }

    @Test
    void aClearedWaveIsFollowedByALargerOne() {
        Game game = quiet();
        game.clearRocks();
        List<Event> events = run(game, Field.WAVE_PAUSE + 0.1, Steering.NONE);
        assertEquals(List.of(new Event.WaveStarted(2)), events.stream().filter(Event.WaveStarted.class::isInstance).toList());
        assertEquals(Field.rocksInWave(2), game.rocks().size());
        assertEquals(6, Field.rocksInWave(2));
        assertEquals(11, Field.rocksInWave(9));
    }

    @Test
    void hyperspaceTakesTheShipSomewhereElse() {
        Game game = quiet();
        game.update(STEP, new Steering(0, Double.NaN, false, false, true));
        assertNull(game.ship());
        assertTrue(game.inHyperspace());
        List<Event> events = run(game, Field.HYPERSPACE_SECONDS, Steering.NONE);
        assertEquals(1, count(events, Event.HyperspaceOut.class));
        assertNotNull(game.ship());
        assertFalse(game.inHyperspace());
        assertNotEquals(Field.CENTRE, game.ship().position());
        assertEquals(Vector.ZERO, game.ship().velocity());
        assertEquals(Field.START_LIVES, game.lives(), "no ship lost");
    }

    // --8<-- [start:test-determinism]
    @Test
    void theSameSeedAndTheSamePilotPlayTheSameGame() {
        int[] scores = new int[2];
        for (int round = 0; round < 2; round++) {
            Game game = new Game(2026);
            Pilot pilot = new Pilot();
            for (int i = 0; i < 60 * 120 && game.phase() != Game.Phase.OVER; i++) {
                game.update(STEP, pilot.steer(game, STEP));
            }
            scores[round] = game.score();
        }
        assertTrue(scores[0] > 0);
        assertEquals(scores[0], scores[1]);
    }
    // --8<-- [end:test-determinism]
}
