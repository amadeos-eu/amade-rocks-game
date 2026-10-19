package eu.amadeos.games.rocks.domain;

import eu.amadeos.gameengine.collision.Circle;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.gameengine.pool.Pool;
import eu.amadeos.gameengine.random.SeededRandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A game of Rocks. Plain Java without a clock: {@link #update} advances everything by a step, with what the
 * player wants in that step, and the {@link #events()} tell what happened in it.
 *
 * <p>Chance (where rocks appear, where their pieces fly, where hyperspace leads) comes from a
 * {@link SeededRandom} made from the seed, so a test can play the same game twice.
 */
public final class Game {
    /** The ship flies; the ship is lost and the next waits; the last ship is lost. */
    public enum Phase { PLAYING, WAITING, OVER }

    /** How many different rock shapes the painter has; each rock gets one of them. */
    public static final int ROCK_SHAPES = 4;

    private final long seed;
    private final SeededRandom random;
    // --8<-- [start:fields]
    private final List<Rock> rocks = new ArrayList<>();
    private final Pool<Bullet> bullets = new Pool<>(Field.MAX_BULLETS, Bullet::new);
    // --8<-- [end:fields]
    private final List<Event> events = new ArrayList<>();
    /** The ship, or {@code null} while it waits to appear or is in hyperspace. */
    private Ship ship;
    private Phase phase = Phase.PLAYING;
    private int score;
    private int lives = Field.START_LIVES;
    private int nextExtraLife = Field.EXTRA_LIFE_EVERY;
    private int wave;
    private double waveSeconds;
    /** Counts down while there is no ship: after a crash, or in hyperspace. */
    private double shipless;
    private boolean inHyperspace;
    private Ship hyperspaceShip;
    private double waveBreak;

    public Game(long seed) {
        this.seed = seed;
        this.random = new SeededRandom(seed);
        ship = new Ship(Field.CENTRE, -Math.PI / 2);
        startWave(1);
    }

    public long seed() {
        return seed;
    }

    /** The ship, or {@code null} while there is none on the field. */
    public Ship ship() {
        return ship;
    }

    public List<Rock> rocks() {
        return Collections.unmodifiableList(rocks);
    }

    /** The shots in the air, {@link Pool#get} 0 to {@link Pool#size()} - 1. Read only. */
    public Pool<Bullet> bullets() {
        return bullets;
    }

    public Phase phase() {
        return phase;
    }

    public int score() {
        return score;
    }

    /** Ships left, counting the one flying. */
    public int lives() {
        return lives;
    }

    public int wave() {
        return wave;
    }

    /** Seconds since the wave started; the heartbeat gets faster with it. */
    public double waveSeconds() {
        return waveSeconds;
    }

    public boolean inHyperspace() {
        return inHyperspace;
    }

    /** What happened in the last step. */
    public List<Event> events() {
        return Collections.unmodifiableList(events);
    }

    // --8<-- [start:update]
    /** One step of {@code seconds}: the ship flies and shoots, everything moves, and then what touches collides. */
    public void update(double seconds, Steering steering) {
        events.clear();
        waveSeconds += seconds;
        if (ship != null) {
            ship.fly(steering, seconds);
            if (steering.fire()) fire();
            if (steering.hyperspace()) enterHyperspace();
        }
        for (int i = 0; i < bullets.size(); i++) bullets.get(i).move(seconds);
        for (Rock rock : rocks) rock.move(seconds);
        shotsHitRocks();
        if (ship != null) shipHitsRock();
        bullets.releaseIf(Bullet::expired);
        waitForShip(seconds);
        nextWave(seconds);
    }
    // --8<-- [end:update]

    // --8<-- [start:fire]
    private void fire() {
        Bullet bullet = bullets.obtain();
        if (bullet == null) return;     // four in the air: the trigger clicks, nothing happens
        bullet.launch(ship.nose(), ship.velocity().plus(Vector.ofAngle(ship.angle(), Field.BULLET_SPEED)));
        events.add(new Event.Fired(ship.nose()));
    }
    // --8<-- [end:fire]

    // --8<-- [start:hits]
    /** A shot that is inside a rock breaks it, and is used up. */
    private void shotsHitRocks() {
        for (int b = 0; b < bullets.size(); b++) {
            Bullet bullet = bullets.get(b);
            Circle shot = new Circle(bullet.position().x(), bullet.position().y(), 0);
            for (int r = 0; r < rocks.size(); r++) {
                if (rocks.get(r).hitArea().overlaps(shot, Field.WRAP)) {
                    bullet.spend();
                    breakRock(r);
                    break;
                }
            }
        }
    }

    private void shipHitsRock() {
        Circle hull = new Circle(ship.position().x(), ship.position().y(), Field.SHIP_RADIUS);
        for (int r = 0; r < rocks.size(); r++) {
            if (rocks.get(r).hitArea().overlaps(hull, Field.WRAP)) {
                events.add(new Event.ShipLost(ship.position()));
                breakRock(r);     // the rock breaks too, and still counts
                loseShip();
                return;
            }
        }
    }
    // --8<-- [end:hits]

    // --8<-- [start:break]
    /** The rock at {@code index} breaks into two smaller ones, flying apart, or into dust. */
    private void breakRock(int index) {
        Rock rock = rocks.remove(index);
        addPoints(rock.size().points());
        events.add(new Event.RockBroken(rock.size(), rock.position()));
        RockSize smaller = rock.size().smaller();
        if (smaller == null) return;
        double heading = rock.velocity().angle();
        for (int side = -1; side <= 1; side += 2) {
            double angle = heading + side * (0.3 + random.nextDouble() * 0.8);
            rocks.add(newRock(smaller, rock.position(), angle));
        }
    }
    // --8<-- [end:break]

    private Rock newRock(RockSize size, Vector position, double angle) {
        double speed = size.minSpeed() + random.nextDouble() * (size.maxSpeed() - size.minSpeed());
        speed *= 1 + Math.min(wave - 1, 8) * 0.08;     // a little faster each wave
        double spin = (random.nextDouble() - 0.5) * 2;
        return new Rock(size, random.nextInt(ROCK_SHAPES), position, Vector.ofAngle(angle, speed),
                random.nextDouble() * 2 * Math.PI, spin);
    }

    private void addPoints(int points) {
        if (phase == Phase.OVER) return;    // the demo's rocks break on after the end
        score += points;
        if (score >= nextExtraLife) {
            nextExtraLife += Field.EXTRA_LIFE_EVERY;
            if (lives < Field.MAX_LIVES) {
                lives++;
                events.add(new Event.ExtraLife(lives));
            }
        }
    }

    private void loseShip() {
        ship = null;
        lives--;
        if (lives == 0) {
            phase = Phase.OVER;
            events.add(new Event.GameOver(new GameResult(score, wave)));
        } else {
            phase = Phase.WAITING;
            shipless = Field.RESPAWN_SECONDS;
        }
    }

    private void enterHyperspace() {
        events.add(new Event.HyperspaceIn(ship.position()));
        hyperspaceShip = ship;      // kept aside, gone from the field
        ship = null;
        inHyperspace = true;
        shipless = Field.HYPERSPACE_SECONDS;
    }

    /** Counts down while there is no ship, and brings it back when the time is up and it is safe. */
    private void waitForShip(double seconds) {
        if (ship != null || phase == Phase.OVER) return;
        shipless -= seconds;
        if (shipless > 0) return;
        if (inHyperspace) {
            // Anywhere at all, even next to a rock: that is the risk of hyperspace.
            Vector to = new Vector(random.nextDouble() * Field.WIDTH, random.nextDouble() * Field.HEIGHT);
            hyperspaceShip.jumpTo(to);
            ship = hyperspaceShip;
            inHyperspace = false;
            events.add(new Event.HyperspaceOut(to));
        } else if (centreIsClear()) {
            ship = new Ship(Field.CENTRE, -Math.PI / 2);
            phase = Phase.PLAYING;
            events.add(new Event.ShipReady(Field.CENTRE));
        }
    }

    // --8<-- [start:centre]
    /** A new ship appears in the centre only when no rock is near it: no ship lost the moment it appears. */
    private boolean centreIsClear() {
        for (Rock rock : rocks) {
            if (Field.WRAP.distance(rock.position(), Field.CENTRE) < Field.SAFE_RADIUS + rock.size().radius()) {
                return false;
            }
        }
        return true;
    }
    // --8<-- [end:centre]

    private void nextWave(double seconds) {
        if (!rocks.isEmpty() || phase == Phase.OVER) return;
        waveBreak += seconds;
        if (waveBreak >= Field.WAVE_PAUSE) startWave(wave + 1);
    }

    // --8<-- [start:wave]
    /** Large rocks at the edges of the field, none close to the ship, flying in all directions. */
    private void startWave(int number) {
        wave = number;
        waveSeconds = 0;
        waveBreak = 0;
        Vector shipAt = ship != null ? ship.position() : Field.CENTRE;
        for (int i = 0; i < Field.rocksInWave(number); i++) {
            Vector at;
            do {
                at = random.nextBoolean()
                        ? new Vector(random.nextDouble() * Field.WIDTH, 0)
                        : new Vector(0, random.nextDouble() * Field.HEIGHT);
            } while (Field.WRAP.distance(at, shipAt) < Field.SPAWN_DISTANCE);
            rocks.add(newRock(RockSize.LARGE, at, random.nextDouble() * 2 * Math.PI));
        }
        events.add(new Event.WaveStarted(number));
    }
    // --8<-- [end:wave]

    /** For tests: puts a rock where the test wants it. */
    void putRock(Rock rock) {
        rocks.add(rock);
    }

    /** For tests: sets the score, e.g. just below an extra ship. */
    void putScore(int score) {
        this.score = score;
    }

    /** For tests: removes all rocks. */
    void clearRocks() {
        rocks.clear();
    }
}
