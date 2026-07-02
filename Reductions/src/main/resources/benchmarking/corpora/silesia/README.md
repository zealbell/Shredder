# Silesia Corpus

This directory contains the Silesia Corpus, which includes a diverse set of file types (text, binary, medical images, etc.) for realistic compression benchmarking.

## Structure

- **`archives/`**: Contains the original `silesia.zip`.
- **`contents/`**: Contains the extracted raw files (e.g., `dickens`, `mr`, `nci`, `samba`, `xml`, `x-ray`).
- **`blobs/`**: Contains the single binary blob file generated from `contents/` using `Generator.toBlob()`.

## Usage

The `Benchmarking` class automatically handles:
1.  Downloading and unpacking `archives/` from corpus source if missing.
2.  Unzipping `archives/` to `contents/` if needed.
3.  Generating `blobs/` from `contents/` if needed.
