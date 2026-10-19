package eu.amadeos.games.rocks.ui.game;

import eu.amadeos.gameengine.fx.Particles;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.gameengine.math.Wrap;
import eu.amadeos.gameengine.pool.Pool;
import eu.amadeos.gameengine.random.SeededRandom;
import eu.amadeos.gameengine.shape.Outline;
import eu.amadeos.gameengine.sprite.Sprite;
import eu.amadeos.games.rocks.domain.Bullet;
import eu.amadeos.games.rocks.domain.Field;
import eu.amadeos.games.rocks.domain.Game;
import eu.amadeos.games.rocks.domain.Rock;
import eu.amadeos.games.rocks.domain.Ship;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Draws the world in logical coordinates (640 × 400): the stars as one sprite, and everything that moves
 * and turns as {@link Outline}s, lines like on the vector screen of the arcade original. Things near an edge
 * are drawn on both sides of it.
 */
final class SpacePainter {
    static final Color ROCK_LINE = new Color(0xC8D0DC);
    static final Color ROCK_FILL = new Color(0x141820);
    static final Color SHIP_LINE = Color.WHITE;
    static final Color SHIP_FILL = new Color(0x1C2A48);
    static final Color FLAME = new Color(0xFF9A40);
    static final Color SHOT = new Color(0xFFF4C0);

    private final Sprite sky = Sprite.drawn(Field.WIDTH, Field.HEIGHT, SpacePainter::drawSky);
    private final Outline[] rocks = rockShapes();
    private final Outline ship = shipOutline();
    private final Outline flameShort = Outline.open(-6, -4, -12, 0, -6, 4);
    private final Outline flameLong = Outline.open(-6, -4, -18, 0, -6, 4);
    private final BasicStroke stroke = new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private final Rectangle2D.Double dot = new Rectangle2D.Double(0, 0, 2.4, 2.4);
    /** Counts frames, so the flame flickers. */
    private long frames;

    // What drawCopy draws: set before each Wrap.forEachCopy, so drawing creates no objects.
    private Graphics2D g;
    private Outline outline;
    private double angle;
    private double scale;
    private Color fill;
    private Color line;
    private final Wrap.Copy drawCopy = this::drawAt;

    /** The ship's figure, nose to the right; the HUD draws the ships left with it too. */
    static Outline shipOutline() {
        return Outline.closed(Ship.NOSE, 0, -8, -8, -5, 0, -8, 8);
    }

    // --8<-- [start:shapes]
    /**
     * A few jagged rocks of radius about 1, enlarged to each rock's size when drawn: corners at even angles
     * around the centre, each at a random distance. A fixed seed, so the rocks look the same in every game.
     */
    private static Outline[] rockShapes() {
        SeededRandom random = new SeededRandom(23);
        Outline[] shapes = new Outline[Game.ROCK_SHAPES];
        for (int s = 0; s < shapes.length; s++) {
            int corners = 9 + random.nextInt(4);
            double[] xy = new double[2 * corners];
            for (int i = 0; i < corners; i++) {
                double angle = 2 * Math.PI * (i + random.nextDouble() * 0.4) / corners;
                double distance = 0.72 + random.nextDouble() * 0.33;
                xy[2 * i] = Math.cos(angle) * distance;
                xy[2 * i + 1] = Math.sin(angle) * distance;
            }
            shapes[s] = Outline.closed(xy);
        }
        return shapes;
    }
    // --8<-- [end:shapes]

    // --8<-- [start:paint]
    /** @param alpha how far the time is between the last two update steps, 0 to 1 */
    void paint(Graphics2D g, Game game, Particles particles, double alpha) {
        frames++;
        sky.draw(g, 0, 0);
        this.g = g;
        g.setStroke(stroke);
        for (Rock rock : game.rocks()) {
            Vector at = Field.WRAP.between(rock.previous(), rock.position(), alpha);
            set(rocks[rock.shape()], between(rock.previousAngle(), rock.angle(), alpha), rock.size().radius(),
                    ROCK_FILL, ROCK_LINE);
            Field.WRAP.forEachCopy(at.x(), at.y(), outline.radius() * scale + 1, drawCopy);
        }
        Ship s = game.ship();
        if (s != null) {
            Vector at = Field.WRAP.between(s.previous(), s.position(), alpha);
            double turned = between(s.previousAngle(), s.angle(), alpha);
            if (s.thrusting()) {
                set(frames % 4 < 2 ? flameLong : flameShort, turned, 1, null, FLAME);
                Field.WRAP.forEachCopy(at.x(), at.y(), 20, drawCopy);
            }
            set(ship, turned, 1, SHIP_FILL, SHIP_LINE);
            Field.WRAP.forEachCopy(at.x(), at.y(), outline.radius() + 1, drawCopy);
        }
        g.setColor(SHOT);
        Pool<Bullet> bullets = game.bullets();
        for (int i = 0; i < bullets.size(); i++) {
            Bullet bullet = bullets.get(i);
            Vector at = Field.WRAP.between(bullet.previous(), bullet.position(), alpha);
            dot.x = Field.WRAP.x(at.x()) - dot.width / 2;
            dot.y = Field.WRAP.y(at.y()) - dot.height / 2;
            g.fill(dot);
        }
        particles.render(g, alpha);
        this.g = null;
    }
    // --8<-- [end:paint]

    private void set(Outline outline, double angle, double scale, Color fill, Color line) {
        this.outline = outline;
        this.angle = angle;
        this.scale = scale;
        this.fill = fill;
        this.line = line;
    }

    /** Draws the current outline at one of its places; the path is worked out once for fill and lines. */
    private void drawAt(double x, double y) {
        Path2D.Double path = outline.path(x, y, angle, scale);
        if (fill != null) {
            g.setColor(fill);
            g.fill(path);
        }
        g.setColor(line);
        g.draw(path);
    }

    private static double between(double previous, double current, double alpha) {
        return previous + (current - previous) * alpha;
    }

    /** Black space with stars of a few brightnesses, always the same ones. */
    private static void drawSky(Graphics2D g, double width, double height) {
        g.setColor(Color.BLACK);
        g.fill(new Rectangle2D.Double(0, 0, width, height));
        SeededRandom random = new SeededRandom(7);
        for (int i = 0; i < 140; i++) {
            int light = 60 + random.nextInt(140);
            g.setColor(new Color(light, light, Math.min(255, light + 30)));
            double size = random.nextDouble() < 0.1 ? 1.6 : 1;
            g.fill(new Rectangle2D.Double(random.nextDouble() * width, random.nextDouble() * height, size, size));
        }
    }
}
