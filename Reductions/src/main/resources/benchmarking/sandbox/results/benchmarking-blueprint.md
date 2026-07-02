# Light Benchmarking Report

> **Report Generated:** {{TIMESTAMP}}  
> **Benchmark Suite:** V 1.0  
> **Total Codecs Tested:** {{CODEC_COUNT}}  
> **Platform:** {{PLATFORM_INFO}}
{{DEVICE_INFO}}

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Participating Codecs](#participating-codecs)
3. [Test Corpus Structure](#test-corpus-structure)
4. [Benchmark Results](#benchmark-results)
5. [Comparative Analysis](#comparative-analysis)
6. [Conclusion](#conclusion)

---

## Executive Summary

This report presents comprehensive benchmarking results for {{TITLE}} against standard compression algorithms. The benchmarks evaluate performance against standardized test corpora (`Canterbury`, `Silesia`, `Wikipedia`) and a `Random` dataset. Key metrics include compression ratio, space savings, compression speed, decompression speed, and theoretical entropy analysis.

### Key Findings

{{KEY_FINDINGS_TABLE}}

---

## Participating Codecs

The following codecs were included in this benchmark run:

{{CODEC_LIST_TABLE}}

---

## Test Corpus Structure

The benchmarking suite utilizes a structured corpus located at `src/main/resources/benchmarking/corpora`. This directory is organized into four main sub-corpora:

1.  **Canterbury**: Standard academic compression corpus.
2.  **Silesia**: Miscellaneous file types (text, binary, medical images, etc.).
3.  **Wikipedia**: Large text dumps (enwik8, enwik9).
4.  **Random**: Cryptographically generated random files of varying sizes.

### Directory Organization
For standard corpora (Canterbury, Silesia, Wikipedia), data is organized into three stages:

*   **`archives/`**: Original compressed archives (e.g., `.zip`) of the corpus data.
*   **`contents/`**: Extracted files to be used as raw input for compression benchmarking. Files in this directory are individually compressed to stress codecs on smaller files in order to measure performance.
*   **`blobs/`**: A blob is a sandwich of all contents files and folders aggregated into a single binary file using `Generator.toBlob()`. This can be converted back to the original structure using `Generator.fromBlob()`.

For the **Random** corpus, files are generated on-the-fly or persisted in:
*   **`random/`**: Organized by size categories (`B`, `KB`, `MB`), containing files like `10KB/1.random`.

### Folder Tree

```
benchmarking/
├── corpus/
│   ├── canterbury/
│   │   ├── archives/
│   │   │   ├── ...
│   │   ├── contents/
│   │   │   ├── ...
│   │   └── blobs/
│   │       ├── ...
│   ├── silesia/
│   │   ├── archives/
│   │   │   ├── ...
│   │   ├── contents/
│   │   │   ├── ...
│   │   └── blobs/
│   │       ├── ...
│   ├── wikipedia/
│   │   ├── archives/
│   │   │   ├── ...
│   │   ├── contents/
│   │   │   ├── ...
│   │   └── blobs/
│   │       ├── ...
│   └── random/
│       ├── B/
│       │   ├── ...
│       ├── KB/
│       │   ├── ...
│       └── MB/
│           ├── ...
└── results/
    └── [generated reports]
```

---

## Metrics Overview

> <details>
> <summary>Key metrics used in this compression benchmarking exercise and representations</summary>
>
> ### 1. Compression Ratio <code>Ratio</code>
> $$\text{Ratio} = \frac{\text{Compressed Size}}{\text{Original Size}}$$
>
> | Ratio Range | Quality | Description |
> |-------------|---------|-------------|
> | < 0.20 | Excellent | Exceptional compression, typically only achieved on highly redundant data |
> | 0.20 - 0.40 | Very Good | High-quality compression suitable for archival |
> | 0.40 - 0.60 | Good | Balanced compression for general use |
> | 0.60 - 0.80 | Moderate | Fast algorithms often fall in this range |
> | > 0.80 | Poor | Little to no size reduction |
>
> ### 2. Space Savings <code>Savings</code>
> $$\text{Savings} = \left(1 - \text{Ratio}\right) \times 100\%$$
> Represents the percentage of the original file size that was eliminated.
>
> ### 3. Compression Speed <code>Cospeed</code>
> $$\text{Cospeed} = \frac{\text{Original Size (MB)}}{\text{Compression Time (seconds)}}$$
> Measured in **MB/s**, this indicates how quickly data can be compressed.
>
> ### 4. Decompression Speed <code>Despeed</code>
> $$\text{Despeed} = \frac{\text{Decompressed Size (MB)}}{\text{Decompression Time (seconds)}}$$
> Critical for applications requiring fast data access.
>
> ### 5. Shannon Entropy <code>Entropy</code>
> $$Entropy = H(X) = -\sum_{i=1}^{n} p(x_i) \log_2 p(x_i)$$
> Measures the theoretical minimum bits per symbol. **Lower entropy = more compressible**.
>
> | Entropy (bits/byte) | Data Characteristics |
> |--------------------|-----------------------|
> | 0.0 - 2.0 | Highly structured, very compressible |
> | 2.0 - 4.0 | Moderate structure, good compression potential |
> | 4.0 - 6.0 | Mixed content, moderate compression |
> | 6.0 - 7.5 | High variety, limited compression |
> | 7.5 - 8.0 | Near-random, compression may expand data |
>
> ### 6. Bits Per Byte <code>BPB</code>
> $$\text{BPB} = \frac{\text{Compressed Size} \times 8}{\text{Original Size}}$$
> The number of bits used to represent each byte of original data. Theoretical minimum is the Shannon entropy.
>
> ### 7. Compression Factor <code>Factor</code>
> $$\text{Factor} = \frac{\text{Original Size}}{\text{Compressed Size}}$$
> Inverse of compression ratio. A factor of 4× means data is compressed to 1/4 of original size.
>
> ### 8. Memory Utilization <code>Mutilization</code>
> Peak memory consumption during compression/decompression operations.
>
> ### 9. Processor Utilization <code>Putilization</code>
> Percentage of CPU resources consumed, important for multi-threaded environments.
>
> ### 10. Integrity Check <code>Integrity</code>
> CRC32 checksum comparison between original and decompressed files ensures lossless operation.
> </details>

---

## Benchmark Results

### 1. Canterbury Corpus
*Standard academic benchmark files.*

<details>
<summary><strong>archives/</strong></summary>

{{CANTERBURY_ARCHIVES_TABLE}}

</details>

---

<details>
<summary><strong>contents/</strong></summary>

{{CANTERBURY_CONTENTS_TABLE}}

</details>

---

<details>
<summary><strong>blobs/</strong></summary>

{{CANTERBURY_BLOBS_TABLE}}

</details>

---

#### Performance Overview

{{CANTERBURY_PERFORMANCE_TABLE}}

{{CANTERBURY_DETAILS}}

### 2. Silesia Corpus
*Diverse real-world file types.*

<details>
<summary><strong>archives/</strong></summary>

{{SILESIA_ARCHIVES_TABLE}}

</details>

---

<details>
<summary><strong>contents/</strong></summary>

{{SILESIA_CONTENTS_TABLE}}

</details>

---

<details>
<summary><strong>blobs/</strong></summary>

{{SILESIA_BLOBS_TABLE}}

</details>

---

#### Performance Overview

{{SILESIA_PERFORMANCE_TABLE}}

{{SILESIA_DETAILS}}


### 3. Wikipedia Corpus
*Large English text data.*

<details>
<summary><strong>archives/</strong></summary>

{{WIKIPEDIA_ARCHIVES_TABLE}}

</details>

---

<details>
<summary><strong>contents/</strong></summary>

{{WIKIPEDIA_CONTENTS_TABLE}}

</details>

---

<details>
<summary><strong>blobs/</strong></summary>

{{WIKIPEDIA_BLOBS_TABLE}}

</details>

---

#### Performance Overview

{{WIKIPEDIA_PERFORMANCE_TABLE}}

{{WIKIPEDIA_DETAILS}}

### 4. Random Data Corpus
*Testing overhead and worst-case scenarios.*

<details>
<summary><strong>B/</strong></summary>

{{RANDOM_B_TABLE}}

</details>

---

<details>
<summary><strong>KB/</strong></summary>

{{RANDOM_KB_TABLE}}

</details>

---

<details>
<summary><strong>MB/</strong></summary>

{{RANDOM_MB_TABLE}}

</details>

---

#### Performance Overview

{{RANDOM_PERFORMANCE_TABLE}}

{{RANDOM_DETAILS}}

---

## Comparative Analysis

### Compression Ratio vs Shannon Entropy

The following table shows compression ratio achieved by each codec across all corpora:

<table>
  <thead>
    <tr>
      <th rowspan="3">Codec</th>
      <th colspan="8">Corpus</th>
    </tr>
    <tr>
      <th colspan="2">Canterbury</th>
      <th colspan="2">Silesia</th>
      <th colspan="2">Wikipedia</th>
      <th colspan="2">Random</th>
    </tr>
    <tr>
      <th><code>Ratio</code></th>
      <th><code>Entropy</code></th>
      <th><code>Ratio</code></th>
      <th><code>Entropy</code></th>
      <th><code>Ratio</code></th>
      <th><code>Entropy</code></th>
      <th><code>Ratio</code></th>
      <th><code>Entropy</code></th>
    </tr>
  </thead>
  <tbody>
{{COMPARATIVE_ANALYSIS_TABLE}}
  </tbody>
</table>

> **⚠️ Anomaly:** Light achieves compression on random data (ratio 0.769) despite entropy of 7.999 bits/byte, which theoretically permits no lossless compression. This suggests Light employs novel techniques that operate outside traditional Shannon entropy bounds.

---

## Conclusion

{{CONCLUSION_TEXT}}

---
Copyright © 2026 Linkersoft
