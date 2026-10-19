import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Draws the program icon of Rocks, a white ship with its engine burning, shooting at a grey rock in dark
 * space, as a PNG of the given size.
 *
 * <pre>
 * java tools/MakeIcon.java 48 src/main/resources/icons/amade-rocks-game.png
 * java tools/MakeIcon.java 256 build/icon-256.png
 * python3 -c "from PIL import Image; Image.open('build/icon-256.png').save('packaging/windows/amade-rocks-game.ico',
 *     sizes=[(16,16),(24,24),(32,32),(48,48),(64,64),(128,128),(256,256)])"
 * </pre>
 *
 * The files are part of the repository; run this again only to change the icon.
 */
public class MakeIcon {
    public static void main(String[] args) throws IOException {
        int size = Integer.parseInt(args[0]);
        File file = new File(args[1]);
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        ImageIO.write(icon(size), "png", file);
    }

    static BufferedImage icon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.scale(size / 48.0, size / 48.0);
        // Space.
        RoundRectangle2D.Double space = new RoundRectangle2D.Double(2, 2, 44, 44, 10, 10);
        g.setPaint(new GradientPaint(0, 2, new Color(0x1A2440), 0, 46, new Color(0x05070C)));
        g.fill(space);
        g.setColor(new Color(0x3A4A70));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(space);
        g.setColor(new Color(0xC0C8E0));
        for (double[] star : new double[][]{{9, 9}, {38, 39}, {12, 37}, {24, 6}}) {
            g.fill(new Ellipse2D.Double(star[0], star[1], 1.2, 1.2));
        }
        // The rock, top right.
        Path2D.Double rock = new Path2D.Double();
        double[][] corners = {{30, 7}, {38, 8}, {42, 15}, {40, 22}, {33, 25}, {27, 21}, {25, 14}};
        rock.moveTo(corners[0][0], corners[0][1]);
        for (int i = 1; i < corners.length; i++) rock.lineTo(corners[i][0], corners[i][1]);
        rock.closePath();
        g.setColor(new Color(0x2A3040));
        g.fill(rock);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0xC8D0DC));
        g.draw(rock);
        // The ship, bottom left, its nose towards the rock, the engine burning.
        g.rotate(-Math.PI / 4, 17, 31);
        Path2D.Double flame = new Path2D.Double();
        flame.moveTo(12, 28);
        flame.lineTo(4, 31);
        flame.lineTo(12, 34);
        g.setColor(new Color(0xFF9A40));
        g.draw(flame);
        Path2D.Double ship = new Path2D.Double();
        ship.moveTo(28, 31);
        ship.lineTo(9, 23);
        ship.lineTo(13, 31);
        ship.lineTo(9, 39);
        ship.closePath();
        g.setColor(new Color(0x1C2A48));
        g.fill(ship);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(ship);
        // A shot on its way.
        g.setColor(new Color(0xFFF4C0));
        g.fill(new Ellipse2D.Double(30.5, 29.7, 2.6, 2.6));
        g.dispose();
        return image;
    }
}
