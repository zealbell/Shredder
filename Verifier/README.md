# Verifier

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

## Run it in development

```bash
mvn -pl Verifier javafx:run        # from the Files root
mvn javafx:run                     # or from this folder
```

## Build a double-clickable app

```bash
# macOS .app under dist/Verifier.app
./build-app.sh

# or a distributable .dmg installer
./build-app.sh dmg
```

Or straight from Maven (this is also what the IntelliJ **Build Native App**
run config does, for every app module at once):

```bash
mvn -P dist clean verify
```

The executable lands in `dist/` — double-click `dist/Verifier.app`. On first
launch macOS Gatekeeper may warn that the app is from an unidentified
developer — right-click the app and choose **Open**.

## How it works

| File | Responsibility |
|------|----------------|
| `Launcher.java` | Plain `main` entry point so the shaded jar launches without the JavaFX module path. |
| `VerifierApp.java` | JavaFX UI: folder picker, tree navigation, expand/collapse all, digest rows. |
| `DigestService.java` | Background pool that computes CRC32/Adler-32/MD5/SHA-1/SHA-256 in one pass per file and caches the results. |
