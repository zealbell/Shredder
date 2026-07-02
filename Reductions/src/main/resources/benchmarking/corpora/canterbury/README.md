# Canterbury Corpus

This directory contains the Standard Canterbury Corpus used for lossless compression benchmarking.

## Structure

- **`archives/`**: Contains the original `cantrbry.zip` and other archive files.
- **`contents/`**: Contains the extracted raw files (e.g., `alice29.txt`, `kennedy.xls`) used for benchmarking.
- **`blobs/`**: Contains the single binary blob file generated from `contents/` using `Generator.toBlob()`.

## Usage

The `Benchmarking` class automatically handles:
1.  Downloading and unpacking `archives/` from corpus source if missing.
2.  Unzipping `archives/` to `contents/` if needed.
3.  Generating `blobs/` from `contents/` if needed.
