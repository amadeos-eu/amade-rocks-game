package eu.amadeos.games.rocks.ui.game;

import eu.amadeos.gameengine.audio.Sound;
import eu.amadeos.gameengine.audio.Sound.Wave;
import eu.amadeos.gameengine.random.SeededRandom;

/** The sound effects of Rocks, made once at start: arcade tones and rumbling noise, no recordings. */
final class SoundBank {
    // --8<-- [start:sounds]
    /** A falling square blip: the shot. */
    final Sound fire = Sound.tone(Wave.SQUARE, 1300, 300, 0.08, 0.12);
    /** A short, dark rumble, played again and again while the engine fires. */
    final Sound thrust = rumble(0.12, 0.92, 0.25);
    /** The bigger the rock, the longer and darker the bang. */
    final Sound bangLarge = rumble(0.7, 0.97, 0.6);
    final Sound bangMedium = rumble(0.45, 0.93, 0.5);
    final Sound bangSmall = rumble(0.25, 0.8, 0.4);
    final Sound shipLost = rumble(1.2, 0.96, 0.7).then(Sound.tone(Wave.TRIANGLE, 400, 80, 0.5, 0.25));
    /** The heartbeat of the original: two low notes, in turn, faster and faster. */
    final Sound beatLow = Sound.tone(Wave.SQUARE, 98, 92, 0.09, 0.22);
    final Sound beatHigh = Sound.tone(Wave.SQUARE, 110, 104, 0.09, 0.22);
    // --8<-- [end:sounds]
    final Sound hyperspaceIn = Sound.tone(Wave.TRIANGLE, 300, 1800, 0.22, 0.25);
    final Sound hyperspaceOut = Sound.tone(Wave.TRIANGLE, 1800, 300, 0.22, 0.25);
    final Sound shipReady = Sound.tone(Wave.TRIANGLE, 440, 880, 0.15, 0.2);
    final Sound extraLife = ping().then(silence(0.05)).then(ping()).then(silence(0.05)).then(ping());
    /** G, E, C, falling. */
    final Sound gameOver = note(392, 0.2).then(note(330, 0.2)).then(Sound.tone(Wave.TRIANGLE, 262, 250, 0.5, 0.3));

    // --8<-- [start:rumble]
    /**
     * Noise with its high tones taken away: a bang, not a hiss. Each sample moves only part of the way
     * towards a new random value (a low-pass filter); the larger {@code darkness}, 0 to 1, the smaller that
     * part, the slower the speaker moves, the deeper the sound. It starts at once and dies away.
     */
    static Sound rumble(double seconds, double darkness, double volume) {
        int n = (int) Math.round(seconds * Sound.RATE);
        SeededRandom random = new SeededRandom(n);    // the same sound every time
        double[] wave = new double[n];
        double value = 0;
        double loudest = 1e-9;
        for (int i = 0; i < n; i++) {
            value += (1 - darkness) * ((random.nextDouble() * 2 - 1) - value);
            wave[i] = value;
            loudest = Math.max(loudest, Math.abs(value));
        }
        short[] samples = new short[n];
        int attack = Sound.RATE / 400;
        for (int i = 0; i < n; i++) {
            double envelope = Math.min(1, i / (double) attack) * Math.exp(-4.0 * i / n) * (n - 1 - i) / n;
            samples[i] = (short) Math.round(wave[i] / loudest * envelope * volume * Short.MAX_VALUE);
        }
        return Sound.of(samples);
    }
    // --8<-- [end:rumble]

    private static Sound ping() {
        return Sound.tone(Wave.SQUARE, 1760, 1760, 0.06, 0.12);
    }

    private static Sound silence(double seconds) {
        return Sound.of(new short[(int) Math.round(seconds * Sound.RATE)]);
    }

    private static Sound note(double hz, double seconds) {
        return Sound.tone(Wave.TRIANGLE, hz, hz, seconds, 0.3);
    }
}
