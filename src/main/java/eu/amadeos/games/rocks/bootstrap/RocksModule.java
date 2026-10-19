package eu.amadeos.games.rocks.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import eu.amadeos.commons.app.AppInfo;
import eu.amadeos.commons.config.AppDirectories;
import eu.amadeos.commons.config.XdgDirectories;
import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.gameengine.random.SeededRandom;
import eu.amadeos.gameengine.score.HighScores;
import eu.amadeos.gameengine.score.PlayerName;
import eu.amadeos.gameengine.score.Ranking;
import eu.amadeos.games.rocks.repository.RocksSettings;
import eu.amadeos.games.rocks.ui.game.Arena;
import eu.amadeos.games.rocks.ui.game.RocksGame;
import eu.amadeos.games.rocks.ui.main.RocksPresenter;
import eu.amadeos.games.rocks.ui.main.RocksView;
import eu.amadeos.games.rocks.ui.main.RocksWindow;

import java.util.Locale;
import java.util.random.RandomGenerator;

/**
 * amade-rocks-game bindings. Theme, language and the XDG directories come from
 * {@link eu.amadeos.commons.app.CommonsModule}.
 */
public final class RocksModule extends AbstractModule {
    /** The texts, {@code messages.properties} and its translations. */
    public static final String MESSAGES = "eu.amadeos.games.rocks.messages";

    @Override
    protected void configure() {
        bind(RocksSettings.class).in(Scopes.SINGLETON);
        bind(Keyboard.class).in(Scopes.SINGLETON);
        bind(Mouse.class).in(Scopes.SINGLETON);
        bind(Mixer.class).in(Scopes.SINGLETON);
        bind(RocksGame.class).in(Scopes.SINGLETON);
        bind(Arena.class).to(RocksGame.class);
        bind(RocksWindow.class).in(Scopes.SINGLETON);
        bind(RocksView.class).to(RocksWindow.class);
        bind(RocksPresenter.class).in(Scopes.SINGLETON);
    }

    /** The best scores in {@code ~/.local/share/amadeos/games/rocks}. */
    @Provides
    @Singleton
    HighScores highScores(XdgDirectories xdg, AppInfo app) {
        return HighScores.of(xdg, app.configName(), Ranking.HIGHER_WINS);
    }

    @Provides
    @Singleton
    PlayerName playerName(AppDirectories directories) {
        return new PlayerName(directories.config(), System.getProperty("user.name"));
    }

    /** Chance for the effects only; the rules make their own from each game's seed. */
    @Provides
    RandomGenerator looks() {
        return new SeededRandom(SeededRandom.freshSeed());
    }

    /** The texts in the language {@code AmadeApplication} chose. */
    @Provides
    @Singleton
    Messages messages(Locale locale) {
        return Messages.load(RocksModule.class, MESSAGES, locale);
    }
}
