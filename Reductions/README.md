# Reductions

A module of the [Files](../README.md) project.

A **pure Java** (no JavaFX) compression toolkit built around a pluggable codec
registry: classical codecs implemented in-repo (RLE, Huffman, BWT, LZ78, LZW)
alongside library-backed ones (GZIP, Deflate, bzip2, XZ, LZMA, Zstandard,
Brotli, LZ4, Snappy), plus corpus generation and Markdown benchmark reports.

## Requirements

- **JDK 17 or newer** (built and tested with Temurin 25).
- **Maven 3.8+** to build.

No JavaFX involved — this is a plain Java library. The codec and hardware
dependencies (commons-compress, zstd-jni, brotli4j, OSHI) are pulled from
Maven Central.

## Build

```bash
mvn -pl Reductions package     # from the Files root
mvn package                    # or from this folder
```

## Run the tools

The classes are libraries first, but two ship a `main` for direct use:

1. **`Transcoder.Testing`** — smoke-tests the codecs against targeted inputs
   (RLE, Huffman, LZ78, BWT, GZIP, Deflate patterns), checking reversibility,
   ratios and entropy.
2. **`Benchmarking`** — downloads/loads the corpora, benchmarks the registered
   codecs and writes a Markdown report under
   `src/main/resources/benchmarking/sandbox/results/`.

`Benchmarking` and `Generator` resolve resource paths relative to the working
directory, so run them with this module (`Reductions/`) as the working
directory.

## How it works

| File | Responsibility |
|------|----------------|
| `Transcoder.java` | Pluggable codec registry for compression/decompression with per-run stats (size, ratio, time, entropy). In-repo classical codecs (RLE, Huffman, BWT, LZ78, LZW) plus GZIP/Deflate (JDK), bzip2/XZ/LZMA/LZ4/Snappy/Deflate64 (commons-compress), Zstandard (zstd-jni), Brotli (brotli4j) and Base64/Base32 encoders. |
| `Benchmarking.java` | Downloads/loads benchmark corpora, runs codec benchmarks and writes Markdown reports from the blueprint template. |
| `Generator.java` | Generates random corpora files by unit/size under `src/main/resources/benchmarking/corpora/random`. |
| `Hardware.java` | Prints the host's CPU/memory/disk/GPU inventory (OSHI) for benchmark reports. |

## Heavy assets

Benchmark corpora (Canterbury, Silesia, Wikipedia, random) are **not tracked**:
only each corpus' `README.md` is committed (see this module's `.gitignore`).
`Benchmarking` downloads/regenerates the actual corpora at runtime into
`src/main/resources/benchmarking/corpora/`.

## License

Provided as-is for personal use.
