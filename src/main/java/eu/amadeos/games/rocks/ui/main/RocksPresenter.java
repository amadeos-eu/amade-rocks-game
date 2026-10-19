package eu.amadeos.games.rocks.ui.main;

import com.google.inject.Inject;
import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.score.HighScores;
import eu.amadeos.gameengine.score.Score;
import eu.amadeos.gameengine.swing.RenderMode;
import eu.amadeos.games.rocks.domain.GameResult;
import eu.amadeos.games.rocks.repository.RocksSettings;
import eu.amadeos.games.rocks.ui.game.Arena;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * Everything around the field, without Swing: the menu commands, the settings, pausing when the window
 * loses the focus, and the best results. All methods run on the Swing thread.
 */
public final class RocksPresenter {
    /** The one table of best results. */
    static final String TABLE = "classic";

    private final RocksView view;
    private final Arena arena;
    private final Messages msg;
    private final RocksSettings settings;
    private final HighScores scores;
    private final Supplier<Instant> now;

    @Inject
    public RocksPresenter(RocksView view, Arena arena, Messages msg, RocksSettings settings, HighScores scores) {
        this(view, arena, msg, settings, scores, Instant::now);
    }

    RocksPresenter(RocksView view, Arena arena, Messages msg, RocksSettings settings, HighScores scores,
                   Supplier<Instant> now) {
        this.view = view;
        this.arena = arena;
        this.msg = msg;
        this.settings = settings;
        this.scores = scores;
        this.now = now;
    }

    public void start() {
        boolean sound = settings.sound();
        RenderMode mode = settings.renderMode();
        view.showOptions(sound, mode);
        view.useRenderMode(mode);
        arena.setSound(sound);
        arena.onGameOver(this::gameOver);
        arena.showTitle();
    }

    public void newGame() {
        arena.newGame();
    }

    public void pause() {
        arena.pause();
    }

    /** The window lost the focus: nobody is at the keys, so a running game waits. */
    public void focusLost() {
        arena.pause();
    }

    public void setSound(boolean on) {
        settings.setSound(on);
        arena.setSound(on);
    }

    public void setRenderMode(RenderMode mode) {
        settings.setRenderMode(mode);
        view.useRenderMode(mode);
    }

    public void showScores() {
        view.showScores(-1);
    }

    public void showAbout() {
        view.showAbout();
    }

    /** A game ended: a good score enters the table. */
    void gameOver(GameResult result) {
        if (result.score() <= 0 || !scores.qualifies(TABLE, result.score())) return;
        view.askName(msg.format("scores.reached", result.score(), result.wave())).ifPresent(name -> {
            int place = scores.add(TABLE, new Score(name, result.score(), now.get()));
            view.showScores(place);
        });
    }
}
