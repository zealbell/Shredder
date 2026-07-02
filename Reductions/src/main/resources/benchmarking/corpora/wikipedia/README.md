# Wikipedia Corpus

This directory contains large text dumps from Wikipedia (e.g., `enwik8`, `enwik9`) to test compression performance on large English text data.

## Structure

- **`archives/`**: Contains the original archives (e.g., `enwik8.zip`, `enwik9.zip`).
- **`contents/`**: Contains the extracted raw `enwik` files.
- **`blobs/`**: Contains the single binary blob file generated from `contents/` using `Generator.toBlob()`.

## Usage

The `Benchmarking` class automatically handles:
1.  Downloading and unpacking `archives/` from corpus source if missing.
2.  Unzipping `archives/` to `contents/` if needed.
3.  Generating `blobs/` from `contents/` if needed.
