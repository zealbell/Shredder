# Random Data Corpus

This directory organizes randomly generated files of various sizes to test compression overhead and worst-case scenarios (high entropy).

## Structure

- **`random/`**: The root directory for random files.
- **Subdirectories**: Files are organized by unit size (e.g., `KB`, `MB`) and then by specific size (e.g., `10KB`, `5MB`).
- **Files**: Individual `.random` files are generated on-the-fly or persisted here.

## Usage

The `Benchmarking` class uses `setCorpusRandoms(int count)` to:
1.  Iterate through the directory structure.
2.  Generate `count` number of random files for each size configuration.
3.  These files are used for benchmarking and can be cleaned up or persisted.
