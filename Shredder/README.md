# Shredder

A module of the [Files](../README.md) project.

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
mvn -pl Shredder javafx:run        # from the Files root
mvn javafx:run                     # or from this folder
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

Or without the script, straight from Maven (this is what the IntelliJ run
config below does):

```bash
mvn -P dist clean verify
```

Either way the executable lands in `dist/` — double-click `dist/Shredder.app`.
On first launch macOS Gatekeeper may warn that the app is from an unidentified
developer — right-click the app and choose **Open**, or allow it under
*System Settings → Privacy & Security*.

### Build it from IntelliJ

The repo ships an IntelliJ run configuration called **Build Native App**
(under the root `.run/`). Pick it from the run-configuration dropdown and
press Run: it runs `mvn -P dist clean verify` with IntelliJ's bundled Maven
and drops the native app of every app module in its `dist/` (this one included).
No global Maven install needed.

So there are two ways to run inside IntelliJ:

1. **Launcher** — the ordinary application run (run `Launcher.main()`) to launch
   the app directly inside the IDE.
2. **Build Native App** — builds the standalone, double-clickable executable in
   `dist/` that you can then launch outside the IDE.

### Cross-platform builds

`jpackage` only builds for the OS it runs on, so run the build on the target OS.
Both `mvn -P dist clean verify` and `build-app.sh` auto-detect the platform and
the matching JavaFX native classifier, producing:

| OS | Output |
|----|--------|
| macOS | `dist/Shredder.app` |
| Windows | `dist/Shredder/Shredder.exe` |
| Linux | `dist/Shredder/bin/Shredder` |

For a distributable installer instead of an app image, use
`./build-app.sh dmg` on macOS, or change the jpackage `<type>` (e.g. `MSI`,
`DEB`) in the `dist` profile.

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
