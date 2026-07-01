# Shredder

A small JavaFX desktop app that **securely erases folders**. Point it at a
folder and it will:

1. Browse the file system with a native folder picker.
2. Recursively list every file underneath the chosen folder.
3. Overwrite each file's bytes with `(byte) 0`.
4. Delete every file, then remove the emptied folders.
5. Report what it did — files shredded, folders deleted, data overwritten,
   errors, and elapsed time.

> ⚠️ **Shredding is irreversible.** Overwritten files cannot be recovered.
> The app asks for confirmation and refuses to touch protected locations
> (the filesystem root, your home folder, `/System`, `/Applications`, etc.).

## Requirements

- **JDK 17 or newer** (built and tested with Temurin 25). `jpackage`, used to
  build the native app, ships with the JDK.
- **Maven 3.8+** to build.

No JavaFX SDK install is needed — the JavaFX libraries are pulled from Maven
Central and bundled into the app.

## Run it in development

```bash
mvn javafx:run
```

## Build a double-clickable app

The repo includes a helper that produces a native, self-contained bundle with
its own runtime — no IDE or separate Java install required to launch it.

```bash
# macOS .app under dist/Shredder.app
./build-app.sh

# or a distributable .dmg installer
./build-app.sh dmg
```

Then just double-click `dist/Shredder.app` (or open the `.dmg` and drag it to
Applications). On first launch macOS Gatekeeper may warn that the app is from
an unidentified developer — right-click the app and choose **Open**, or allow
it under *System Settings → Privacy & Security*.

### Building on Windows / Linux

`build-app.sh` auto-detects the platform and JavaFX native classifier. On
Windows run the equivalent commands (Git Bash works), or invoke jpackage with
`--type msi` / `--type deb` to produce the native installer for that OS.

## How it works

| File | Responsibility |
|------|----------------|
| `Launcher.java` | Plain `main` entry point so the shaded jar launches without the JavaFX module path. |
| `ShredderApp.java` | JavaFX UI: folder picker, confirmation, progress, live stats. |
| `ShredService.java` | Background `Task` that scans, zero-overwrites, and deletes. |
| `ShredStats.java` | Tally of files/folders/bytes/errors reported back to the UI. |

The build uses `maven-shade-plugin` to produce a single runnable fat jar
(`target/shredder.jar`), which `jpackage` wraps into a native app image with a
bundled JRE.

## License

Provided as-is for personal use. Use responsibly — you are deleting real data.
