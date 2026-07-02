# Files

A multi-module Maven project of file utilities:

| Module | Kind | What it does |
|--------|------|--------------|
| [`Shredder/`](Shredder/README.md) | JavaFX app | Securely erases folders: recursively overwrites every file with zeros, deletes them, then removes the emptied folders. |
| [`Verifier/`](Verifier/README.md) | JavaFX app | Browses a folder tree and computes per-file checksums (CRC32, Adler-32) and hashes (MD5, SHA-1, SHA-256), reading each file once for all five. |
| [`Reductions/`](Reductions/README.md) | Pure Java library | Compression toolkit: a pluggable registry of 20 codecs — from in-repo RLE, Huffman, BWT, LZ78 and LZW to GZIP, Zstandard, Brotli, XZ, LZ4 and more — plus corpus generation and Markdown benchmark reports. |

## Requirements

- **JDK 17 or newer** (built and tested with Temurin 25). `jpackage`, used to
  build the native apps, ships with the JDK.
- **Maven 3.8+** to build.

No JavaFX SDK install is needed — the JavaFX libraries are pulled from Maven
Central and bundled into the apps.

## Build everything

```bash
mvn clean package
```

## Run an app in development

```bash
mvn -pl Shredder javafx:run
mvn -pl Verifier javafx:run
```

## Build the native, double-clickable apps

```bash
mvn -P dist clean verify
```

This produces a self-contained app image (with its own runtime) per JavaFX
module: `Shredder/dist/Shredder.app` and `Verifier/dist/Verifier.app` on
macOS (`.exe`/binary layouts on Windows/Linux — build on the target OS, since
`jpackage` only builds for the OS it runs on).

To build just one app:

```bash
mvn -P dist -pl Shredder clean verify     # or -pl Verifier
```

Each app module also ships a standalone `build-app.sh` helper (add `dmg` for a
macOS installer instead of an app image).

### Build from IntelliJ

The repo ships an IntelliJ run configuration called **Build Native App**
(under `.run/`): it runs `mvn -P dist clean verify` with IntelliJ's bundled
Maven and drops both native apps in their modules' `dist/` folders.
