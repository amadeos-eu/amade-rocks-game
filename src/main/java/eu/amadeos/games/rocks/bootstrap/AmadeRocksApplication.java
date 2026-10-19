package eu.amadeos.games.rocks.bootstrap;

import eu.amadeos.commons.app.AmadeApplication;
import eu.amadeos.commons.app.AppInfo;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.games.rocks.ui.game.RocksGame;
import eu.amadeos.games.rocks.ui.main.RocksPresenter;
import eu.amadeos.games.rocks.ui.main.RocksWindow;

public final class AmadeRocksApplication {
    public static final AppInfo APP = new AppInfo("amade-rocks-game", "Rocks", "rocks");

    private AmadeRocksApplication() {
    }

    /** The version from the jar's manifest; "dev" when run from the IDE. */
    public static String version() {
        String version = AmadeRocksApplication.class.getPackage().getImplementationVersion();
        return version == null ? "dev" : version;
    }

    // --8<-- [start:main]
    public static void main(String[] args) {
        AmadeApplication.launch(APP, injector -> {
            Mixer mixer = injector.getInstance(Mixer.class);
            mixer.start();
            RocksWindow window = injector.getInstance(RocksWindow.class);
            RocksPresenter presenter = injector.getInstance(RocksPresenter.class);
            RocksGame game = injector.getInstance(RocksGame.class);
            window.bind(presenter);
            presenter.start();
            window.whenClosed(() -> {
                game.stop();
                mixer.close();
                System.exit(0);
            });
            window.setLocationByPlatform(true);
            window.setVisible(true);
            game.start(window.canvas());
        }, new RocksModule());
    }
    // --8<-- [end:main]
}
