package eu.amadeos.games.rocks.ui.game;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.games.rocks.bootstrap.RocksModule;
import eu.amadeos.games.rocks.domain.Field;
import eu.amadeos.games.rocks.domain.Game;
import eu.amadeos.games.rocks.domain.GameResult;
import eu.amadeos.games.rocks.domain.Ship;
import eu.amadeos.games.rocks.repository.RocksSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The game without a window and without the loop thread: the test presses keys and moves the mouse, and
 * calls the steps itself, so everything happens in order on the test thread.
 */
class RocksGameTest {
    private static final double STEP = RocksGame.STEP;

    @TempDir
    Path dir;

    private final long[] now = {0};
    private final Keyboard keyboard = new Keyboard(() -> now[0]);
    private final Mouse mouse = new Mouse();
    private final List<GameResult> results = new ArrayList<>();
    private RocksGame game;

    @BeforeEach
    void create() {
        Messages msg = Messages.load(RocksModule.class, RocksModule.MESSAGES, Locale.ENGLISH);
        game = new RocksGame(keyboard, mouse, msg, new RocksSettings(dir.resolve("rocks.properties")), () -> 77L,
                new SplittableRandom(4), new Mixer(), Runnable::run);
        game.onGameOver(results::add);
    }

    private void tap(int key) {
        keyboard.pressed(key, now[0]);
        keyboard.released(key, now[0] + 1_000_000);
        steps(1);
    }

    private void steps(int n) {
        for (int i = 0; i < n; i++) {
            now[0] += (long) (STEP * 1e9);
            game.update(STEP);
        }
    }

    private void seconds(double seconds) {
        steps((int) Math.round(seconds * RocksGame.UPDATES_PER_SECOND));
    }

    private RocksGame.PlayScene play() {
        tap(KeyEvent.VK_ENTER);
        return assertInstanceOf(RocksGame.PlayScene.class, game.top());
    }

    @Test
    void enterStartsAGameAndSpaceFires() {
        assertInstanceOf(RocksGame.TitleScene.class, game.top());
        RocksGame.PlayScene play = play();
        assertEquals(0, play.game().bullets().size());
        tap(KeyEvent.VK_SPACE);
        assertEquals(1, play.game().bullets().size());
    }

    @Test
    void holdingUpFiresTheEngine() {
        RocksGame.PlayScene play = play();
        keyboard.pressed(KeyEvent.VK_UP, now[0]);
        seconds(0.5);
        Ship ship = play.game().ship();
        assertTrue(ship.thrusting());
        assertTrue(ship.position().y() < Field.CENTRE.y() - 10, "flew up");
    }

    // --8<-- [start:test-mouse]
    @Test
    void theShipTurnsTowardsThePointerUntilAKeyTurnsIt() {
        RocksGame.PlayScene play = play();
        mouse.moved(Field.CENTRE.x() + 100, Field.CENTRE.y());     // to the right of the ship
        seconds(1);
        assertTrue(play.mouseAims());
        assertEquals(0, play.game().ship().angle(), 1e-9, "nose to the pointer");
        mouse.pressed(MouseEvent.BUTTON1);
        steps(1);
        assertEquals(1, play.game().bullets().size(), "a left click fires");
        mouse.released(MouseEvent.BUTTON1);
        mouse.pressed(MouseEvent.BUTTON3);
        steps(1);
        assertTrue(play.game().ship().thrusting(), "the right button held fires the engine");
        mouse.released(MouseEvent.BUTTON3);
        keyboard.pressed(KeyEvent.VK_LEFT, now[0]);
        seconds(0.5);
        assertFalse(play.mouseAims(), "a key takes over again");
        assertEquals(-Math.toRadians(125), play.game().ship().angle(), 1e-6);
    }
    // --8<-- [end:test-mouse]

    @Test
    void pFreezesTheGameAndAClickContinuesIt() {
        RocksGame.PlayScene play = play();
        tap(KeyEvent.VK_P);
        assertEquals(2, game.sceneCount());
        double seconds = play.game().waveSeconds();
        seconds(1);
        assertEquals(seconds, play.game().waveSeconds());
        mouse.pressed(MouseEvent.BUTTON1);
        steps(1);
        seconds(0.5);
        assertTrue(play.game().waveSeconds() > seconds, "goes on");
    }

    @Test
    void aShipThatDoesNothingIsLostAndTheResultReachesThePresenter() {
        RocksGame.PlayScene play = play();
        for (int i = 0; i < 20 * 60 * 120 && results.isEmpty(); i++) steps(1);
        assertEquals(1, results.size());
        assertEquals(Game.Phase.OVER, play.game().phase());
        assertInstanceOf(RocksGame.OverScene.class, game.top());
        seconds(2);
        tap(KeyEvent.VK_ENTER);
        assertInstanceOf(RocksGame.TitleScene.class, game.top());
    }

    @Test
    void theDemoBreaksRocksAndTheDustFlies() {
        boolean dust = false;
        for (int i = 0; i < 30 * 120 && !dust; i++) {
            steps(1);
            dust = game.effects().particles().count() > 0;
        }
        assertTrue(dust, "a rock was broken");
        RocksGame.TitleScene title = assertInstanceOf(RocksGame.TitleScene.class, game.top());
        assertTrue(title.demo().score() > 0);
    }

    @Test
    void theHeartbeatGetsFaster() {
        assertEquals(1.0, Effects.beatInterval(0), 1e-9);
        assertTrue(Effects.beatInterval(30) < 0.7);
        assertEquals(0.28, Effects.beatInterval(600), 1e-9);
    }

    @Test
    void everySceneDrawsWorldAndOverlay() {
        BufferedImage image = new BufferedImage((int) Field.WIDTH, (int) Field.HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        seconds(2);
        game.render(g, 0.5);
        game.renderOverlay(g, 0.5);
        RocksGame.PlayScene play = play();
        game.render(g, 0.5);
        Ship ship = play.game().ship();
        int x = (int) Math.round(ship.position().x());
        int y = (int) Math.round(ship.position().y());
        assertEquals(SpacePainter.SHIP_FILL.getRGB() & 0xFFFFFF, image.getRGB(x, y) & 0xFFFFFF, "inside the ship");
        game.renderOverlay(g, 0.5);
        tap(KeyEvent.VK_P);
        game.render(g, 0.5);
        game.renderOverlay(g, 0.5);
        g.dispose();
    }
}
