package eu.amadeos.games.rocks.ui.main;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.commons.swing.EdtExecutor;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.gameengine.score.HighScores;
import eu.amadeos.gameengine.score.PlayerName;
import eu.amadeos.gameengine.score.Ranking;
import eu.amadeos.gameengine.swing.GameCanvas;
import eu.amadeos.gameengine.swing.RenderMode;
import eu.amadeos.games.rocks.bootstrap.RocksModule;
import eu.amadeos.games.rocks.domain.Field;
import eu.amadeos.games.rocks.repository.RocksSettings;
import eu.amadeos.games.rocks.ui.game.RocksGame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** The real window, game and loop: keys on the canvas reach the menu, the graphics menu reaches the canvas. */
class RocksWindowTest {
    @TempDir
    Path dir;

    private RocksWindow window;
    private RocksGame game;
    private RocksSettings settings;
    private final Mouse mouse = new Mouse();

    @BeforeEach
    void open() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        Messages msg = Messages.load(RocksModule.class, RocksModule.MESSAGES, Locale.ENGLISH);
        settings = new RocksSettings(dir.resolve("rocks.properties"));
        HighScores scores = new HighScores(dir.resolve("scores"), Ranking.HIGHER_WINS);
        Keyboard keyboard = new Keyboard();
        onEdt(() -> {
            game = new RocksGame(keyboard, mouse, msg, settings, new SplittableRandom(1), new Mixer(),
                    new EdtExecutor());
            window = new RocksWindow(msg, scores, new PlayerName(dir, "tester"), game, keyboard, mouse);
            RocksPresenter presenter = new RocksPresenter(window, game, msg, settings, scores);
            window.bind(presenter);
            presenter.start();
            window.setVisible(true);
            game.start(window.canvas());
        });
    }

    @AfterEach
    void close() throws Exception {
        if (game != null) game.stop();
        if (window != null) onEdt(window::dispose);
    }

    private static void onEdt(Runnable code) throws Exception {
        try {
            SwingUtilities.invokeAndWait(code);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error error) throw error;
            throw (Exception) e.getCause();
        }
    }

    private void key(int code) throws Exception {
        onEdt(() -> window.canvas().dispatchEvent(new KeyEvent(window.canvas(), KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, code, KeyEvent.CHAR_UNDEFINED)));
    }

    private JMenuItem item(String name) {
        return find(window.getJMenuBar(), name);
    }

    private static JMenuItem find(MenuElement element, String name) {
        for (MenuElement child : element.getSubElements()) {
            if (child instanceof JMenuItem item && name.equals(item.getName())) return item;
            JMenuItem found = find(child, name);
            if (found != null) return found;
        }
        return null;
    }

    @Test
    void theMouseOverTheCanvasIsInTheGamesCoordinates() throws Exception {
        for (String mode : new String[]{"game-graphics-direct", "game-graphics-pixel"}) {
            onEdt(() -> {
                item(mode).doClick(0);
                GameCanvas canvas = window.canvas();
                canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0,
                        canvas.getWidth() / 2, canvas.getHeight() / 2, 0, false));
            });
            assertEquals(Field.WIDTH / 2, mouse.x(), 1, mode);
            assertEquals(Field.HEIGHT / 2, mouse.y(), 1, mode);
        }
    }

    @Test
    void theMouseShowsACrossHairOverTheField() throws Exception {
        onEdt(() -> assertEquals(java.awt.Cursor.CROSSHAIR_CURSOR, window.canvas().getCursor().getType()));
    }

    @Test
    void theSavedRenderModeIsUsedAndTheMenuChangesIt() throws Exception {
        onEdt(() -> {
            assertEquals(RenderMode.DIRECT, window.canvas().renderMode());
            assertTrue(item("game-graphics-direct").isSelected());
            JMenuItem pixel = item("game-graphics-pixel");
            assertNotNull(pixel);
            pixel.doClick(0);
            assertEquals(RenderMode.PIXEL, window.canvas().renderMode());
        });
        assertEquals(RenderMode.PIXEL, settings.renderMode());
    }

    @Test
    void f12ShowsTheFrameTimesAndF11SwitchesToFullScreen() throws Exception {
        key(KeyEvent.VK_F12);
        onEdt(() -> assertTrue(window.canvas().showsFrameTimes()));
        key(KeyEvent.VK_F11);
        onEdt(() -> assertTrue(window.fullScreen().active(), "full screen"));
        key(KeyEvent.VK_F11);
        onEdt(() -> assertFalse(window.fullScreen().active(), "back in the window"));
        long deadline = System.currentTimeMillis() + 3000;
        while (window.canvas().frameTimes().framesPerSecond() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(window.canvas().frameTimes().framesPerSecond() > 0, "frames are drawn and measured");
    }
}
