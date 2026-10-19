package eu.amadeos.games.rocks.ui.main;

import eu.amadeos.gameengine.swing.RenderMode;

import java.util.Optional;

/** What {@link RocksPresenter} needs from the window; tests replace it. The field itself is the game's. */
public interface RocksView {
    /** Shows the settings in the menu. */
    void showOptions(boolean sound, RenderMode mode);

    /** Switches the canvas to a render mode. */
    void useRenderMode(RenderMode mode);

    /** Asks for the player's name after a good result; empty if cancelled. */
    Optional<String> askName(String message);

    /** Shows the best results; {@code highlight} -1 for no new entry. */
    void showScores(int highlight);

    void showAbout();
}
