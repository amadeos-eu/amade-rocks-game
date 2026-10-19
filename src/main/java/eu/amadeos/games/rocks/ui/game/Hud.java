package eu.amadeos.games.rocks.ui.game;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.shape.Outline;
import eu.amadeos.games.rocks.domain.Field;
import eu.amadeos.games.rocks.domain.Game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * The overlay: score, ships left and wave at the top, and the texts of the title, pause and end screens.
 * The canvas draws it after the world, at the full size of the screen, so the letters are sharp in every
 * render mode.
 */
final class Hud {
    static final Color CHALK = Color.WHITE;
    static final Color GOLD = new Color(0xF0C040);
    /** A veil over the field behind texts. */
    static final Color VEIL = new Color(0, 0, 0, 150);

    private final Messages msg;
    // Fonts and the stroke are made once, not in every frame.
    private final Font score = new Font(Font.SANS_SERIF, Font.BOLD, 1).deriveFont(20f);
    private final Font small = new Font(Font.SANS_SERIF, Font.BOLD, 1).deriveFont(13f);
    private final Map<Integer, Font> fonts = new HashMap<>();
    private final Outline life = SpacePainter.shipOutline();
    private final BasicStroke stroke = new BasicStroke(1.1f);
    private final Rectangle2D.Double veil = new Rectangle2D.Double(0, 0, Field.WIDTH, Field.HEIGHT);

    Hud(Messages msg) {
        this.msg = msg;
    }

    // --8<-- [start:hud]
    /** The score at the top left, a small ship for each one left below it, the wave at the top right. */
    void status(Graphics2D g, Game game) {
        g.setColor(CHALK);
        g.setFont(score);
        g.drawString(msg.format("hud.score", game.score()), 14f, 26f);
        g.setStroke(stroke);
        for (int i = 0; i < game.lives() - (game.ship() != null || game.inHyperspace() ? 1 : 0); i++) {
            life.draw(g, 22 + i * 16, 42, -Math.PI / 2, 0.7);
        }
        g.setFont(small);
        FontMetrics fm = g.getFontMetrics();
        String wave = msg.format("hud.wave", game.wave());
        g.drawString(wave, (float) (Field.WIDTH - 14 - fm.stringWidth(wave)), 24f);
    }
    // --8<-- [end:hud]

    /** Darkens the field so text over it stays readable. */
    void veil(Graphics2D g) {
        g.setColor(VEIL);
        g.fill(veil);
    }

    /** A line of text centred horizontally, its baseline at {@code y}. */
    void text(Graphics2D g, String line, double y, int size, boolean bold, Color colour) {
        g.setFont(fonts.computeIfAbsent(2 * size + (bold ? 1 : 0),
                key -> new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size)));
        g.setColor(colour);
        g.drawString(line, (float) (Field.WIDTH - g.getFontMetrics().stringWidth(line)) / 2, (float) y);
    }
}
