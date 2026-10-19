package eu.amadeos.games.rocks.repository;

import eu.amadeos.gameengine.swing.RenderMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RocksSettingsTest {
    @TempDir
    Path dir;

    @Test
    void defaultsAndChangesAreKept() throws Exception {
        RocksSettings settings = new RocksSettings(dir.resolve("rocks.properties"));
        assertEquals(RenderMode.DIRECT, settings.renderMode());
        assertTrue(settings.sound());
        settings.setRenderMode(RenderMode.PIXEL);
        settings.setSound(false);
        RocksSettings again = new RocksSettings(dir.resolve("rocks.properties"));
        assertEquals(RenderMode.PIXEL, again.renderMode());
        assertFalse(again.sound());
        Files.writeString(dir.resolve("rocks.properties"), "keys.left=J NUMPAD4\n", java.nio.file.StandardOpenOption.APPEND);
        assertArrayEquals(new int[]{KeyEvent.VK_J, KeyEvent.VK_NUMPAD4}, again.keys("left", KeyEvent.VK_LEFT));
        assertArrayEquals(new int[]{KeyEvent.VK_RIGHT}, again.keys("right", KeyEvent.VK_RIGHT));
    }
}
