# Verifier

A module of the [Files](../README.md) project.

A JavaFX desktop app that **verifies file contents**. Point it at a folder and
it will:

1. Browse the file system with a native folder picker.
2. Show the folder's contents as a navigable tree (folders first, sorted).
3. Expand any file (+) to compute its checksums — **CRC32**, **Adler-32** —
   and hashes — **MD5**, **SHA-1**, **SHA-256** — in the background, reading
   the file once for all five algorithms.
4. Cache results per file (path + size + mtime), so collapsing (−) and
   re-expanding is instant.
5. **Expand All / Collapse All** buttons act on the whole tree; right-click a
   digest row to copy its value.

## Requirements

- **JDK 17 or newer** (built and tested with Temurin 25). `jpackage`, used to
  build the native app, ships with the JDK.
- **Maven 3.8+** to build.

No JavaFX SDK install is needed — the JavaFX libraries are pulled from Maven
Central and bundled into the app.

## Run it in development

```bash
mvn -pl Verifier javafx:run        # from the Files root
mvn javafx:run                     # or from this folder
```

## Build a double-clickable app

The module includes a helper that produces a native, self-contained bundle with
its own runtime — no IDE or separate Java install required to launch it.

```bash
# macOS .app under dist/Verifier.app
./build-app.sh

# or a distributable .dmg installer
./build-app.sh dmg
```

Or without the script, straight from Maven (this is what the IntelliJ run
config below does):

```bash
mvn -P dist clean verify
```

Either way the executable lands in `dist/` — double-click `dist/Verifier.app`.
On first launch macOS Gatekeeper may warn that the app is from an unidentified
developer — right-click the app and choose **Open**, or allow it under
*System Settings → Privacy & Security*.

### Build it from IntelliJ

This module ships an IntelliJ run configuration called
**Build Native App (Verifier)** (under `Verifier/.run/`). Pick it from the
run-configuration dropdown and press Run: it runs
`mvn -P dist clean verify -pl :verifier` with IntelliJ's bundled Maven and
drops the native app in this module's `dist/`. No global Maven install needed.

So there are two ways to run inside IntelliJ:

1. **Launcher** — the ordinary application run (run `Launcher.main()`) to launch
   the app directly inside the IDE.
2. **Build Native App (Verifier)** — builds the standalone, double-clickable
   executable in `dist/` that you can then launch outside the IDE.

### Cross-platform builds

`jpackage` only builds for the OS it runs on, so run the build on the target OS.
Both `mvn -P dist clean verify` and `build-app.sh` auto-detect the platform and
the matching JavaFX native classifier, producing:

| OS | Output |
|----|--------|
| macOS | `dist/Verifier.app` |
| Windows | `dist/Verifier/Verifier.exe` |
| Linux | `dist/Verifier/bin/Verifier` |

For a distributable installer instead of an app image, use
`./build-app.sh dmg` on macOS, or change the jpackage `<type>` (e.g. `MSI`,
`DEB`) in the `dist` profile.

## How it works

| File | Responsibility |
|------|----------------|
| `Launcher.java` | Plain `main` entry point so the shaded jar launches without the JavaFX module path. |
| `VerifierApp.java` | JavaFX UI: folder picker, tree navigation, expand/collapse all, digest rows. |
| `DigestService.java` | Background pool that computes CRC32/Adler-32/MD5/SHA-1/SHA-256 in one pass per file and caches the results. |

The build uses `maven-shade-plugin` to produce a single runnable fat jar
(`target/verifier.jar`), which `jpackage` wraps into a native app image with a
bundled JRE.

## License

Provided as-is for personal use.
