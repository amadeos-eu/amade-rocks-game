package eu.amadeos.games.rocks.ui.game;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.games.rocks.bootstrap.RocksModule;
import eu.amadeos.games.rocks.domain.Event;
import eu.amadeos.games.rocks.domain.Field;
import eu.amadeos.games.rocks.domain.Game;
import eu.amadeos.games.rocks.domain.Rock;
import eu.amadeos.games.rocks.domain.RockSize;
import eu.amadeos.games.rocks.repository.RocksSettings;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.SplittableRandom;

/**
 * The screenshots of chapter 23, drawn without a screen: the title, the computer playing with a rock across
 * the edge, the start of a game. {@code ./gradlew screenshots -Pshots=<folder>}.
 */
public final class Screenshots {
    private final long[] now = {0};
    private final Keyboard keyboard = new Keyboard(() -> now[0]);
    private final RocksGame game;
    private final Hud hud;

    private Screenshots() throws Exception {
        Messages msg = Messages.load(RocksModule.class, RocksModule.MESSAGES, Locale.ENGLISH);
        long[] seeds = {11};
        game = new RocksGame(keyboard, new Mouse(), msg,
                new RocksSettings(Files.createTempDirectory("rocks").resolve("rocks.properties")), () -> seeds[0]++,
                new SplittableRandom(11), new Mixer(), Runnable::run);
        hud = new Hud(msg);
    }

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "screenshots");
        dir.mkdirs();
        Screenshots shots = new Screenshots();
        shots.steps(6 * 120);
        shots.save(new File(dir, "title.png"), true);
        // The demo, without the title's texts but with the score: shots flying, dust, a rock over an edge.
        RocksGame.TitleScene title = (RocksGame.TitleScene) shots.game.top();
        for (int i = 0; i < 600 * 120; i++) {
            shots.steps(1);
            Game demo = title.demo();
            boolean broken = demo.events().stream().anyMatch(e -> e instanceof Event.RockBroken b
                    && b.size() != RockSize.SMALL);
            if (broken && demo.ship() != null && demo.bullets().size() >= 1 && demo.rocks().size() >= 6
                    && overEdge(demo)) break;
        }
        shots.steps(8);     // the dust a moment later
        shots.save(new File(dir, "play.png"), false);
        shots.tap(KeyEvent.VK_ENTER);
        shots.save(new File(dir, "wave.png"), true);
        System.exit(0);
    }

    private static boolean overEdge(Game game) {
        for (Rock rock : game.rocks()) {
            double r = rock.size().radius();
            double x = rock.position().x();
            double y = rock.position().y();
            if (rock.size().radius() > 20 && (x < r || x > Field.WIDTH - r || y < r || y > Field.HEIGHT - r)) {
                return true;
            }
        }
        return false;
    }

    private void tap(int key) {
        keyboard.pressed(key, now[0]);
        keyboard.released(key, now[0] + 1_000_000);
        steps(4);
    }

    private void steps(int n) {
        for (int i = 0; i < n; i++) {
            now[0] += (long) (RocksGame.STEP * 1e9);
            game.update(RocksGame.STEP);
        }
    }

    /** Twice the logical size, as on a 1280 × 800 screen. */
    private void save(File file, boolean overlay) throws Exception {
        BufferedImage image = new BufferedImage(2 * (int) Field.WIDTH, 2 * (int) Field.HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.scale(2, 2);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        game.render(g, 0);
        if (overlay) {
            game.renderOverlay(g, 0);
        } else if (game.top() instanceof RocksGame.TitleScene title) {
            hud.status(g, title.demo());
        }
        g.dispose();
        ImageIO.write(image, "png", file);
    }
}
