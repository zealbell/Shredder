# Reductions

A **pure Java** (no JavaFX) compression toolkit built around a pluggable codec
registry: classical codecs implemented in-repo (RLE, Huffman, BWT, LZ78, LZW)
alongside library-backed ones (GZIP, Deflate, bzip2, XZ, LZMA, Zstandard,
Brotli, LZ4, Snappy), plus corpus generation and Markdown benchmark reports.

| File | Responsibility |
|------|----------------|
| `Transcoder.java` | Pluggable codec registry for compression/decompression with per-run stats (size, ratio, time, entropy). In-repo classical codecs (RLE, Huffman, BWT, LZ78, LZW) plus GZIP/Deflate (JDK), bzip2/XZ/LZMA/LZ4/Snappy/Deflate64 (commons-compress), Zstandard (zstd-jni), Brotli (brotli4j) and Base64/Base32 encoders. |
| `Benchmarking.java` | Downloads/loads benchmark corpora, runs codec benchmarks and writes Markdown reports from the blueprint template. |
| `Generator.java` | Generates random corpora files by unit/size under `src/main/resources/benchmarking/corpora/random`. |
| `Hardware.java` | Prints the host's CPU/memory/disk/GPU inventory (OSHI) for benchmark reports. |

## Heavy assets

Benchmark corpora (Canterbury, Silesia, Wikipedia, random) are **not tracked**:
only each corpus' `README.md` is committed
(see this module's `.gitignore`). `Benchmarking` downloads/regenerates the
actual corpora at runtime into `src/main/resources/benchmarking/corpora/`.

Note: `Benchmarking` and `Generator` resolve those resource paths relative to
the working directory, so run them with this module (`Reductions/`) as the
working directory.

## Build

```bash
mvn -pl Reductions package     # from the Files root
```
