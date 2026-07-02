# Reductions

A **pure Java** (no JavaFX) compression toolkit, lifted from
[Harmattan](../../Whirlwind/Harmattan)'s `com.linkersoft.harmattan.reductions`
package. It keeps the original package name so the sources stay a faithful
copy.

| File | Responsibility |
|------|----------------|
| `Transcoder.java` | Compression/decompression across codecs — GZIP/Deflate (JDK), LZMA & XZ (commons-compress), Zstandard (zstd-jni), Brotli (brotli4j) — plus encoding helpers. |
| `Benchmarking.java` | Downloads/loads benchmark corpora, runs codec benchmarks and writes Markdown reports from the blueprint template. |
| `Generator.java` | Generates random corpora files by unit/size under `src/main/resources/benchmarking/corpora/random`. |
| `Hardware.java` | Prints the host's CPU/memory/disk/GPU inventory (OSHI) for benchmark reports. |

## Heavy assets

Benchmark corpora (Canterbury, Silesia, Wikipedia, random) are **not tracked**,
mirroring Harmattan's repository: only each corpus' `README.md` is committed
(see this module's `.gitignore`). `Benchmarking` downloads/regenerates the
actual corpora at runtime into `src/main/resources/benchmarking/corpora/`.

Note: `Benchmarking` and `Generator` resolve those resource paths relative to
the working directory, so run them with this module (`Reductions/`) as the
working directory.

## Build

```bash
mvn -pl Reductions package     # from the Files root
```
