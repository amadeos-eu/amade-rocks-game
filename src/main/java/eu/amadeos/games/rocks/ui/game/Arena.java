package eu.amadeos.games.rocks.ui.game;

import eu.amadeos.games.rocks.domain.GameResult;

import java.util.function.Consumer;

/**
 * What the presenter may ask of the running game. The game runs on its own thread; every method here is
 * safe to call from the Swing thread and takes effect at the start of the game's next step.
 */
public interface Arena {
    /** Back to the title screen, where the computer plays. */
    void showTitle();

    void newGame();

    /** Pauses a running game; does nothing elsewhere. */
    void pause();

    void setSound(boolean on);

    /** Called on the Swing thread when a game ends. */
    void onGameOver(Consumer<GameResult> listener);
}
