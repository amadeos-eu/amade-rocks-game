# amade-rocks-game

**amade-rocks-game** is the classic rocks in space on the [amadeOS](https://github.com/amadeos-eu/amadeos) desktop: turn, thrust and shoot your little ship through waves of drifting rocks that break into smaller ones. Built on [amade-game-engine](https://github.com/amadeos-eu/amade-game-engine); runs on other operating systems as well.

![License](https://img.shields.io/badge/license-EUPL--1.2-blue)
![Java](https://img.shields.io/badge/Java-25-orange)

## Playing

- **Enter**, **Space** or a **click** (or F2) starts a game.
- **Left** and **right** (or A, D) turn the ship, **up** (or W) fires the engine, **Space** (or Ctrl) shoots, **down** (or S, Shift) jumps into hyperspace.
- Or the **mouse**: the ship turns towards the pointer, the **left button** shoots, the **right button** held fires the engine, the **middle button** jumps into hyperspace. A turning key takes over from the mouse until it moves again.
- A hit breaks a **large rock** (20 points) into two medium ones (50 points each), a medium one into two small ones (100 points each), and a small one into dust.
- **Four shots** fly at a time. The field has **no edges**: what leaves it on one side comes back on the other, rocks, shots and the ship alike.
- **Hyperspace** takes the ship to a random place, perhaps right next to a rock.
- Three ships, and an **extra ship** every 10,000 points. A lost ship's successor waits until the centre is clear. Every cleared wave brings a bigger one, up to eleven large rocks; the heartbeat gets faster the longer a wave lasts.
- **P** or **Esc** pauses, and so does leaving the window.
- **Best scores**, ten of them. On the title screen the computer plays.
- **F11** switches to full screen and back. *Game → Graphics* chooses how the field reaches the screen: **Direct** (sharp, at the screen's full size) or **Pixels** (the field at 640 × 400, enlarged with hard square pixels). **F12** shows how long each frame takes.
- Sound effects can be switched off under *Game → Sound*.

The keys can be changed in `~/.config/amadeos/rocks/rocks.properties`, with the names Java gives them, several separated by spaces:

```properties
keys.left=LEFT A
keys.right=RIGHT D
keys.thrust=UP W
keys.fire=SPACE CONTROL
keys.hyperspace=DOWN S SHIFT
keys.pause=P ESCAPE
```

Texts in English, Deutsch, Français, Español and Italiano.

## Files

| What | Where |
|---|---|
| Settings (graphics, sound, keys) | `~/.config/amadeos/rocks/rocks.properties` |
| Name for high scores | `~/.config/amadeos/rocks/player.properties` |
| Best scores | `~/.local/share/amadeos/games/rocks/classic.scores` |

## Building

```bash
./gradlew test          # the window test needs a display (xvfb-run on servers)
./gradlew run
./gradlew deb           # build/distributions/amade-rocks-game_<version>_all.deb
./gradlew installToAmadeOs   # copies the package to ../amadeos/config/packages.chroot/
```

Everything in the game is drawn in code: the stars as one sprite, the rocks and the ship as outlines of straight lines. The icon is drawn by `tools/MakeIcon.java` (see the comment at its top). `./gradlew screenshots -Pshots=<folder>` draws the handbook's screenshots.

With `../amade-game-engine` (and `../amade-commons`) checked out next to this repository, Gradle builds those checkouts instead of the JitPack versions.

## Structure

- `domain`: the rules without user interface or time of their own. `Game` (ship, rocks, the pool of four shots, collisions, lives, waves, hyperspace; chance from a seeded random generator; it reports `Event`s), `Ship`, `Rock`, `RockSize`, `Bullet`, `Steering` (what the player wants in a step), `Field` (sizes, speeds, points, the wrap-around field), `Pilot` (plays the title screen's demo), `GameResult`.
- `repository`: `RocksSettings` (graphics, sound, keys).
- `ui.game`: the running game on the loop thread. `RocksGame` (loop, controls and mouse, scenes: title, game, pause, end), `SpacePainter` (stars, rocks, ship, flame and shots, drawn between update steps and on both sides of an edge), `Hud` (score, ships left, wave, and the texts), `Effects` (events become dust, sparks and sounds; the engine and the heartbeat), `SoundBank`, `Arena` (what the presenter may ask of the game).
- `ui.main`: Model-View-Presenter for everything around the field: `RocksPresenter`, `RocksView`, `RocksWindow`.

From the engine: `GameLoop` and `Ticker`, `Vector`, `Angles`, `Wrap`, `Outline`, `Pool`, `Circle`, `SeededRandom`, `GameCanvas`, `Sprite`, `Particles`, `Sound` and `Mixer`, `Keyboard`, `Mouse` and `Controls`, `SceneStack`, `FullScreen`, high scores.

Comments of the form `// --8<-- [start:name]` mark the excerpts the [Game Developer's Handbook](https://datazuul.github.io/amade-game-engine/) quotes; chapter 23 is about this game.

## Licence

[EUPL-1.2](LICENSE)
