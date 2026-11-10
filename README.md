# CINÉ — Android TV Launcher

A premium, cinematic and fully customisable Android TV launcher designed for Sony Bravia and Android TV devices.

Native Kotlin + Android Views (no Compose, no image libraries, **no permissions, no network access**). Designed on a 1920×1080 canvas from a Google Stitch design ("Cinematic Luxury TV OS", see [docs/DESIGN.md](docs/DESIGN.md)).

## Features

- **Home**: CINÉ branding, top navigation, cinematic hero, *Your Entertainment* app row, Continue Watching, Trending This Week, Critically Acclaimed, Recently Added, Watchlist.
- **Real app icons**: apps are discovered with `PackageManager` (Leanback + launcher intents). Labels and icons come straight from the installed apps — nothing is downloaded or redrawn. Installs/uninstalls are detected live. Missing core brands (Netflix, Prime Video, YouTube, Disney+, Sony LIV, Spotify) appear as clearly-marked *Not installed* placeholders (no logo is fabricated); OK opens their Play Store page if available.
- **Cinematic app preview**: focusing an app tile enlarges it, shows its real name/icon in the hero with sample artwork/title/description and a *Press OK to launch* cue. Preview content is labelled **SAMPLE** and never claims availability.
- **Screens**: Home, Movie details, Application library (categories, recents, favourites), Search (D-pad keyboard), Settings, Movies / Series / Watchlist.
- **Settings** (persisted locally): wallpaper, animations, UI scale, theme accent, application favourites/hidden, start screen, About.
- **Remote-first**: full D-pad navigation, OK to launch, hold OK or MENU for app options, BACK walks the stack and never exits, HOME returns to the home screen, focus is recovered if ever lost.
- **Modular content**: `CatalogSource` interface in `data/Catalog.kt`; `SampleCatalog` uses fictional titles and procedurally generated art. Plug in a real metadata source later.

## Architecture

```
app/src/main/java/com/cine/launcher/
├── MainActivity.kt          single activity: screen stack, key handling, Host implementation
├── data/
│   ├── AppRepository.kt     PackageManager discovery, live install/uninstall receiver, launching
│   ├── Catalog.kt           Title/Shelf models, CatalogSource interface, SampleCatalog
│   ├── ArtDrawable.kt       procedural sample artwork (no bitmaps)
│   └── Prefs.kt             SharedPreferences-backed settings
└── ui/
    ├── Theme.kt             design tokens, accent themes, 1280→device px scaling, fonts
    ├── ViewKit.kt           view helpers, TV focus treatment (gold ring + lift + scale)
    ├── Components.kt        AppTile, TitleCard, Row, TopBar, ContextMenu
    ├── Host.kt              Screen/Host contracts
    └── HomeScreen / AppsScreen / DetailsScreen / SearchScreen / SettingsScreen / CatalogScreen
```

UI is authored in Stitch's 1280-px space and scaled to the real display width (so it is correct on 1080p and 4K panels regardless of reported density), then multiplied by the user's UI-scale preference.

## Requirements

- JDK 17, Android SDK (platform 34, build-tools 34), `adb`.
- Target device: Android TV 9+ (minSdk 28, targetSdk 34).

## Build

```bash
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk     # or create local.properties with sdk.dir=...
./gradlew assembleDebug                      # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest lintDebug
```

A prebuilt debug APK is in `dist/CineLauncher.apk`.

## Install on a TV with ADB

Enable Developer options → USB/Network debugging on the TV, then:

```bash
adb connect <tv-ip>:5555
adb install -r CineLauncher.apk
adb shell am start -n com.cine.launcher/.MainActivity
```

CINÉ appears in the TV's apps row and works as a normal app; the stock launcher is **not** disabled and no root is needed. If your TV offers a home-app picker, CINÉ registers as a HOME candidate.

## Visual Studio Code

Open the folder, install the recommended extensions (Kotlin, Gradle for Java), and use **Terminal → Run Build Task** (`Build debug APK`). Other tasks: *Run unit tests*, *Lint*, *Install on TV (adb)*.

## Contributing

1. Fork and branch from `main`.
2. Keep the TV-first rules: everything reachable by D-pad, visible focus, no new permissions or network calls without discussion.
3. Run `./gradlew testDebugUnitTest lintDebug` before opening a PR.

## Known limitations

- Content is **sample data** with generated artwork. No real metadata or streaming-availability source is wired in yet.
- Not-installed brands have no logo (no authorised brand-asset licence is bundled); they show a neutral placeholder.
- Tested on an Android 12 (API 31) Android TV emulator only; no Android 9 / Sony Bravia hardware test has been done yet.
- Android does not let apps intercept the HOME key or silently become the default launcher.

## Licence

MIT (see [LICENSE](LICENSE)). Bundled fonts Bodoni Moda and Manrope are under the SIL Open Font License 1.1 ([FONTS-OFL.txt](FONTS-OFL.txt)).
