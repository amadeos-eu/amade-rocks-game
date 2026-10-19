package eu.amadeos.games.rocks.ui.game;

import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.audio.Sound;
import eu.amadeos.gameengine.fx.Particles;
import eu.amadeos.gameengine.loop.Ticker;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.games.rocks.domain.Event;
import eu.amadeos.games.rocks.domain.Game;
import eu.amadeos.games.rocks.domain.RockSize;
import eu.amadeos.games.rocks.domain.Ship;

import java.awt.Color;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * What the events of a step look and sound like: dust for a broken rock, fire for a lost ship, the shots,
 * the engine and the heartbeat from the mixer. The rules know nothing of this; they only report what
 * happened. The particles take their chance from a random generator of their own, never from the rules'.
 */
final class Effects {
    static final Color[] PALETTE = {new Color(0xB8C0CC), Color.WHITE, new Color(0xFF9A40), new Color(0x7DE0FF),
            new Color(0xFFE070)};
    private static final int DUST = 0;
    private static final int WHITE = 1;
    private static final int FIRE = 2;
    private static final int BLUE = 3;
    private static final int YELLOW = 4;
    /** The engine's rumble is played again after this many seconds while it fires. */
    private static final double THRUST_REPEAT = 0.1;

    private final Particles particles = new Particles(1500, PALETTE);
    private final SoundBank sounds = new SoundBank();
    private final Mixer mixer;
    private final RandomGenerator random;
    private final Ticker heartbeat = new Ticker(1);
    private boolean highBeat;
    private double untilThrust;

    Effects(Mixer mixer, RandomGenerator random) {
        this.mixer = mixer;
        this.random = random;
    }

    Particles particles() {
        return particles;
    }

    void clear() {
        particles.clear();
    }

    // --8<-- [start:heartbeat]
    /**
     * Moves the particles; while {@code audible}, plays the engine and the heartbeat. The heartbeat starts
     * slow with each wave and gets faster the longer the wave lasts, down to a beat every 0.28 seconds.
     */
    void update(double seconds, Game game, boolean audible) {
        particles.update(seconds, 0);    // no gravity in space
        Ship ship = game.ship();
        untilThrust -= seconds;
        if (audible && ship != null && ship.thrusting() && untilThrust <= 0) {
            mixer.play(sounds.thrust);
            untilThrust = THRUST_REPEAT;
        }
        heartbeat.setInterval(beatInterval(game.waveSeconds()));
        for (int beats = heartbeat.advance(seconds); beats > 0; beats--) {
            if (!audible || game.phase() == Game.Phase.OVER || game.rocks().isEmpty()) continue;
            mixer.play(highBeat ? sounds.beatHigh : sounds.beatLow);
            highBeat = !highBeat;
        }
    }

    static double beatInterval(double waveSeconds) {
        return Math.max(0.28, 1.0 - waveSeconds * 0.012);
    }
    // --8<-- [end:heartbeat]

    // --8<-- [start:react]
    /** Shows and, if {@code audible}, plays what happened in the last step. */
    void react(List<Event> events, boolean audible) {
        for (Event event : events) {
            switch (event) {
                case Event.Fired fired -> {
                    if (audible) mixer.play(sounds.fire);
                }
                case Event.RockBroken(RockSize size, Vector at) -> {
                    burst(at, DUST, (int) size.radius() * 2, size.radius() * 4);
                    if (audible) mixer.play(bang(size));
                }
                case Event.ShipLost(Vector at) -> {
                    burst(at, FIRE, 80, 140);
                    burst(at, WHITE, 30, 80);
                    if (audible) mixer.play(sounds.shipLost);
                }
                case Event.ShipReady(Vector at) -> {
                    ring(at, BLUE, 40);
                    if (audible) mixer.play(sounds.shipReady);
                }
                case Event.HyperspaceIn(Vector from) -> {
                    ring(from, BLUE, 30);
                    if (audible) mixer.play(sounds.hyperspaceIn);
                }
                case Event.HyperspaceOut(Vector to) -> {
                    ring(to, BLUE, 30);
                    if (audible) mixer.play(sounds.hyperspaceOut);
                }
                case Event.ExtraLife life -> {
                    if (audible) mixer.play(sounds.extraLife);
                }
                case Event.WaveStarted started -> highBeat = false;
                case Event.GameOver over -> {
                    if (audible) mixer.play(sounds.gameOver);
                }
            }
        }
    }
    // --8<-- [end:react]

    private Sound bang(RockSize size) {
        return switch (size) {
            case LARGE -> sounds.bangLarge;
            case MEDIUM -> sounds.bangMedium;
            case SMALL -> sounds.bangSmall;
        };
    }

    /** {@code count} particles fly out of {@code at} in all directions. */
    private void burst(Vector at, int colour, int count, double speed) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double v = speed * (0.2 + random.nextDouble() * 0.8);
            particles.spawn(at.x(), at.y(), Math.cos(angle) * v, Math.sin(angle) * v, 0.4 + random.nextDouble() * 0.6,
                    1 + random.nextDouble() * 1.5, random.nextDouble() < 0.2 ? YELLOW : colour);
        }
    }

    /** A ring of sparks spreading evenly: the ship appearing or vanishing. */
    private void ring(Vector at, int colour, int count) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            particles.spawn(at.x(), at.y(), Math.cos(angle) * 90, Math.sin(angle) * 90, 0.35, 1.5, colour);
        }
    }
}
