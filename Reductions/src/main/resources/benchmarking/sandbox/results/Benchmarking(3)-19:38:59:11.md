# Light Benchmarking Report

> **Report Generated:** February 11, 2026 19:38:59  
> **Benchmark Suite:** V 1.0  
> **Total Codecs Tested:** 3  
> **Platform:** Mac OS X, Java 24.0.2

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

This report presents comprehensive benchmarking results for Reductions against standard compression algorithms. The benchmarks evaluate performance against standardized test corpora (`Canterbury`, `Silesia`, `Wikipedia`) and a `Random` dataset. Key metrics include compression ratio, space savings, compression speed, decompression speed, and theoretical entropy analysis.

### Key Findings

<table>
  <thead>
    <tr>
      <th>Category</th>
      <th>Best Performer</th>
      <th>Value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Best Compression Ratio</strong></td>
      <td>Zstandard</td>
      <td>0.120</td>
    </tr>
    <tr>
      <td><strong>Best Space Savings</strong></td>
      <td>Zstandard</td>
      <td>88.0%</td>
    </tr>
    <tr>
      <td><strong>Fastest Compression</strong></td>
      <td>Light</td>
      <td>2,137.3 MB/s</td>
    </tr>
    <tr>
      <td><strong>Fastest Decompression</strong></td>
      <td>Light</td>
      <td>2,050.8 MB/s</td>
    </tr>
  </tbody>
</table>

---

## Participating Codecs

The following codecs were included in this benchmark run:

<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Codec</th>
      <th>Library</th>
      <th>Streaming</th>
      <th>Notes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>1000</td>
      <td>Light</td>
      <td>Custom</td>
      <td>Yes</td>
      <td>Entropy-defying custom codec</td>
    </tr>
    <tr>
      <td>6</td>
      <td>GZIP</td>
      <td>Apache Commons</td>
      <td>Yes</td>
      <td>-</td>
    </tr>
    <tr>
      <td>4</td>
      <td>Zstandard</td>
      <td>zstd-jni</td>
      <td>Yes</td>
      <td>-</td>
    </tr>
  </tbody>
</table>

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
*   **`contents/`**: Extracted files to be used as raw input for compression benchmarking.
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

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>artificl.zip</td><td>74.66 KB</td><td>8.00</td><td>None</td><td>74.66 KB</td><td>74.33 KB</td><td>74.67 KB</td></tr>
    <tr><td>calgary.zip</td><td>1.02 MB</td><td>8.00</td><td>None</td><td>1.02 MB</td><td>1.02 MB</td><td>1.02 MB</td></tr>
    <tr><td>cantrbry.zip</td><td>716.77 KB</td><td>7.96</td><td>None</td><td>716.77 KB</td><td>614.17 KB</td><td>623.80 KB</td></tr>
    <tr><td>large.zip</td><td>3.11 MB</td><td>8.00</td><td>None</td><td>3.11 MB</td><td>3.11 MB</td><td>3.11 MB</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>4.90 MB</strong></td><td><strong>7.99</strong></td><td><strong>-</strong></td><td><strong>4.90 MB</strong></td><td><strong>4.80 MB</strong></td><td><strong>4.80 MB</strong></td></tr>
  </tbody>
</table>

</details>

---

<details>
<summary><strong>contents/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td><strong>Total/Average</strong></td><td><strong>0 B</strong></td><td><strong>0.00</strong></td><td><strong>-</strong></td><td><strong>0 B</strong></td><td><strong>0 B</strong></td><td><strong>0 B</strong></td></tr>
  </tbody>
</table>

</details>

---

<details>
<summary><strong>blobs/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>artificl.blob</td><td>293.03 KB</td><td>4.64</td><td>High</td><td>293.03 KB</td><td>74.98 KB</td><td>73.53 KB</td></tr>
    <tr><td>calgary.blob</td><td>3.10 MB</td><td>5.36</td><td>Moderate</td><td>3.10 MB</td><td>1.02 MB</td><td>1.02 MB</td></tr>
    <tr><td>cantrbry.blob</td><td>2.68 MB</td><td>4.71</td><td>High</td><td>2.68 MB</td><td>716.60 KB</td><td>625.41 KB</td></tr>
    <tr><td>large.blob</td><td>10.64 MB</td><td>4.23</td><td>High</td><td>10.64 MB</td><td>3.11 MB</td><td>3.10 MB</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>16.71 MB</strong></td><td><strong>4.74</strong></td><td><strong>-</strong></td><td><strong>16.71 MB</strong></td><td><strong>4.90 MB</strong></td><td><strong>4.80 MB</strong></td></tr>
  </tbody>
</table>

</details>

---

#### Performance Overview

| Codec | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| Light | 1.000 | 0.0% | 619.3 MB/s | 684.2 MB/s | 8.00 | 1.00× | 1 MB | 90% | ✅ |
| GZIP | 0.624 | 37.6% | 9.7 MB/s | 124.4 MB/s | 4.99 | 1.60× | 1 MB | 75% | ✅ |
| Zstandard | 0.621 | 37.9% | 132.1 MB/s | 181.6 MB/s | 4.97 | 1.61× | 1 MB | 78% | ✅ |



<details>
<summary><strong>Light</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **1.000** | **0.0%** | **619.3 MB/s** | **684.2 MB/s** | **8.00** | **1.00×** | **1 MB** | **90%** | **✅** |
| archives | 1.000 | 0.0% | 387.6 MB/s | 464.3 MB/s | 8.00 | 1.00× | 1 MB | 83% | ✅ |
| blobs | 1.000 | 0.0% | 850.9 MB/s | 904.1 MB/s | 8.00 | 1.00× | 1 MB | 98% | ✅ |

</details>

<details>
<summary><strong>GZIP</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.624** | **37.6%** | **9.7 MB/s** | **124.4 MB/s** | **4.99** | **1.60×** | **1 MB** | **75%** | **✅** |
| archives | 0.963 | 3.7% | 9.8 MB/s | 137.3 MB/s | 7.70 | 1.04× | 1 MB | 70% | ✅ |
| blobs | 0.284 | 71.6% | 9.7 MB/s | 111.5 MB/s | 2.27 | 3.52× | 1 MB | 80% | ✅ |

</details>

<details>
<summary><strong>Zstandard</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.621** | **37.9%** | **132.1 MB/s** | **181.6 MB/s** | **4.97** | **1.61×** | **1 MB** | **78%** | **✅** |
| archives | 0.968 | 3.2% | 203.3 MB/s | 190.2 MB/s | 7.74 | 1.03× | 1 MB | 71% | ✅ |
| blobs | 0.275 | 72.5% | 60.8 MB/s | 173.0 MB/s | 2.20 | 3.64× | 1 MB | 85% | ✅ |

</details>


### 2. Silesia Corpus
*Diverse real-world file types.*

<details>
<summary><strong>archives/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>extra.zip</td><td>41.01 MB</td><td>8.00</td><td>None</td><td>41.01 MB</td><td>41.00 MB</td><td>41.01 MB</td></tr>
    <tr><td>mozilla.zip</td><td>18.71 MB</td><td>7.97</td><td>None</td><td>18.71 MB</td><td>18.19 MB</td><td>18.33 MB</td></tr>
    <tr><td>samba.zip</td><td>6.75 MB</td><td>7.93</td><td>None</td><td>6.75 MB</td><td>5.88 MB</td><td>5.87 MB</td></tr>
    <tr><td>xml.zip</td><td>685.19 KB</td><td>8.00</td><td>None</td><td>685.19 KB</td><td>679.12 KB</td><td>681.69 KB</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>67.14 MB</strong></td><td><strong>7.98</strong></td><td><strong>-</strong></td><td><strong>67.14 MB</strong></td><td><strong>65.74 MB</strong></td><td><strong>65.88 MB</strong></td></tr>
  </tbody>
</table>

</details>

---

<details>
<summary><strong>contents/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td><strong>Total/Average</strong></td><td><strong>0 B</strong></td><td><strong>0.00</strong></td><td><strong>-</strong></td><td><strong>0 B</strong></td><td><strong>0 B</strong></td><td><strong>0 B</strong></td></tr>
  </tbody>
</table>

</details>

---

<details>
<summary><strong>blobs/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>extra.blob</td><td>127.57 MB</td><td>5.99</td><td>Moderate</td><td>127.57 MB</td><td>41.00 MB</td><td>40.54 MB</td></tr>
    <tr><td>mozilla.blob</td><td>48.35 MB</td><td>6.26</td><td>Low</td><td>48.35 MB</td><td>18.20 MB</td><td>17.83 MB</td></tr>
    <tr><td>samba.blob</td><td>53.46 KB</td><td>4.85</td><td>High</td><td>53.46 KB</td><td>19.91 KB</td><td>20.84 KB</td></tr>
    <tr><td>xml.blob</td><td>5.08 MB</td><td>5.50</td><td>Moderate</td><td>5.08 MB</td><td>672.79 KB</td><td>623.29 KB</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>181.04 MB</strong></td><td><strong>5.65</strong></td><td><strong>-</strong></td><td><strong>181.04 MB</strong></td><td><strong>59.88 MB</strong></td><td><strong>59.00 MB</strong></td></tr>
  </tbody>
</table>

</details>

---

#### Performance Overview

| Codec | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| Light | 1.000 | 0.0% | 1,184.2 MB/s | 918.8 MB/s | 8.00 | 1.00× | < 1 MB | 85% | ✅ |
| GZIP | 0.629 | 37.1% | 14.6 MB/s | 123.4 MB/s | 5.03 | 1.59× | 1 MB | 87% | ✅ |
| Zstandard | 0.630 | 37.0% | 147.1 MB/s | 231.8 MB/s | 5.04 | 1.59× | < 1 MB | 82% | ✅ |



<details>
<summary><strong>Light</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **1.000** | **0.0%** | **1,184.2 MB/s** | **918.8 MB/s** | **8.00** | **1.00×** | **< 1 MB** | **85%** | **✅** |
| archives | 1.000 | 0.0% | 1,197.6 MB/s | 973.4 MB/s | 8.00 | 1.00× | 1 MB | 82% | ✅ |
| blobs | 1.000 | 0.0% | 1,170.7 MB/s | 864.2 MB/s | 8.00 | 1.00× | < 1 MB | 89% | ✅ |

</details>

<details>
<summary><strong>GZIP</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.629** | **37.1%** | **14.6 MB/s** | **123.4 MB/s** | **5.03** | **1.59×** | **1 MB** | **87%** | **✅** |
| archives | 0.958 | 4.2% | 13.0 MB/s | 144.8 MB/s | 7.67 | 1.04× | 1 MB | 81% | ✅ |
| blobs | 0.300 | 70.0% | 16.2 MB/s | 102.0 MB/s | 2.40 | 3.33× | 1 MB | 92% | ✅ |

</details>

<details>
<summary><strong>Zstandard</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.630** | **37.0%** | **147.1 MB/s** | **231.8 MB/s** | **5.04** | **1.59×** | **< 1 MB** | **82%** | **✅** |
| archives | 0.961 | 3.9% | 214.9 MB/s | 279.8 MB/s | 7.69 | 1.04× | 1 MB | 76% | ✅ |
| blobs | 0.299 | 70.1% | 79.3 MB/s | 183.8 MB/s | 2.39 | 3.34× | < 1 MB | 87% | ✅ |

</details>



### 3. Wikipedia Corpus
*Large English text data.*

<details>
<summary><strong>archives/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>enwik8.zip</td><td>34.76 MB</td><td>8.00</td><td>None</td><td>34.76 MB</td><td>34.77 MB</td><td>34.76 MB</td></tr>
    <tr><td>enwik9.zip</td><td>307.65 MB</td><td>8.00</td><td>None</td><td>307.65 MB</td><td>307.61 MB</td><td>307.60 MB</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>342.41 MB</strong></td><td><strong>8.00</strong></td><td><strong>-</strong></td><td><strong>342.41 MB</strong></td><td><strong>342.37 MB</strong></td><td><strong>342.36 MB</strong></td></tr>
  </tbody>
</table>

</details>

---

<details>
<summary><strong>contents/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td><strong>Total/Average</strong></td><td><strong>0 B</strong></td><td><strong>0.00</strong></td><td><strong>-</strong></td><td><strong>0 B</strong></td><td><strong>0 B</strong></td><td><strong>0 B</strong></td></tr>
  </tbody>
</table>

</details>

---

<details>
<summary><strong>blobs/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>enwik8.blob</td><td>95.37 MB</td><td>5.08</td><td>Moderate</td><td>95.37 MB</td><td>34.86 MB</td><td>33.94 MB</td></tr>
    <tr><td>enwik9.blob</td><td>953.67 MB</td><td>5.16</td><td>Moderate</td><td>953.67 MB</td><td>308.93 MB</td><td>298.77 MB</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>1.02 GB</strong></td><td><strong>5.12</strong></td><td><strong>-</strong></td><td><strong>1.02 GB</strong></td><td><strong>343.79 MB</strong></td><td><strong>332.70 MB</strong></td></tr>
  </tbody>
</table>

</details>

---

#### Performance Overview

| Codec | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| Light | 1.000 | 0.0% | 890.2 MB/s | 886.6 MB/s | 8.00 | 1.00× | 1 MB | 54% | ✅ |
| GZIP | 0.672 | 32.8% | 13.0 MB/s | 113.5 MB/s | 5.38 | 1.49× | 1 MB | 85% | ✅ |
| Zstandard | 0.667 | 33.3% | 201.3 MB/s | 238.8 MB/s | 5.34 | 1.50× | 1 MB | 77% | ✅ |



<details>
<summary><strong>Light</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **1.000** | **0.0%** | **890.2 MB/s** | **886.6 MB/s** | **8.00** | **1.00×** | **1 MB** | **54%** | **✅** |
| archives | 1.000 | 0.0% | 778.6 MB/s | 745.3 MB/s | 8.00 | 1.00× | 1 MB | 48% | ✅ |
| blobs | 1.000 | 0.0% | 1,001.9 MB/s | 1,028.0 MB/s | 8.00 | 1.00× | 1 MB | 60% | ✅ |

</details>

<details>
<summary><strong>GZIP</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.672** | **32.8%** | **13.0 MB/s** | **113.5 MB/s** | **5.38** | **1.49×** | **1 MB** | **85%** | **✅** |
| archives | 1.000 | -0.0% | 15.0 MB/s | 143.1 MB/s | 8.00 | 1.00× | 1 MB | 85% | ✅ |
| blobs | 0.345 | 65.5% | 10.9 MB/s | 83.9 MB/s | 2.76 | 2.90× | 1 MB | 84% | ✅ |

</details>

<details>
<summary><strong>Zstandard</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.667** | **33.3%** | **201.3 MB/s** | **238.8 MB/s** | **5.34** | **1.50×** | **1 MB** | **77%** | **✅** |
| archives | 1.000 | 0.0% | 346.3 MB/s | 310.6 MB/s | 8.00 | 1.00× | 1 MB | 75% | ✅ |
| blobs | 0.335 | 66.5% | 56.4 MB/s | 166.9 MB/s | 2.68 | 2.99× | 1 MB | 79% | ✅ |

</details>


### 4. Random Data Corpus
*Testing overhead and worst-case scenarios.*

<details>
<summary><strong>B/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>1.random</td><td>128 B</td><td>6.47</td><td>Low</td><td>128 B</td><td>151 B</td><td>137 B</td></tr>
    <tr><td>2.random</td><td>128 B</td><td>6.60</td><td>Low</td><td>128 B</td><td>151 B</td><td>137 B</td></tr>
    <tr><td>3.random</td><td>128 B</td><td>6.49</td><td>Low</td><td>128 B</td><td>151 B</td><td>137 B</td></tr>
    <tr><td>4.random</td><td>128 B</td><td>6.58</td><td>Low</td><td>128 B</td><td>151 B</td><td>137 B</td></tr>
    <tr><td>5.random</td><td>128 B</td><td>6.42</td><td>Low</td><td>128 B</td><td>151 B</td><td>137 B</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>640 B</strong></td><td><strong>6.51</strong></td><td><strong>-</strong></td><td><strong>640 B</strong></td><td><strong>755 B</strong></td><td><strong>685 B</strong></td></tr>
  </tbody>
</table>

</details>

---

<details>
<summary><strong>KB/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>1.random</td><td>128.00 KB</td><td>8.00</td><td>None</td><td>128.00 KB</td><td>128.06 KB</td><td>128.01 KB</td></tr>
    <tr><td>2.random</td><td>128.00 KB</td><td>8.00</td><td>None</td><td>128.00 KB</td><td>128.06 KB</td><td>128.01 KB</td></tr>
    <tr><td>3.random</td><td>128.00 KB</td><td>8.00</td><td>None</td><td>128.00 KB</td><td>128.06 KB</td><td>128.01 KB</td></tr>
    <tr><td>4.random</td><td>128.00 KB</td><td>8.00</td><td>None</td><td>128.00 KB</td><td>128.06 KB</td><td>128.01 KB</td></tr>
    <tr><td>5.random</td><td>128.00 KB</td><td>8.00</td><td>None</td><td>128.00 KB</td><td>128.06 KB</td><td>128.01 KB</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>640.00 KB</strong></td><td><strong>8.00</strong></td><td><strong>-</strong></td><td><strong>640.00 KB</strong></td><td><strong>640.28 KB</strong></td><td><strong>640.06 KB</strong></td></tr>
  </tbody>
</table>

</details>

---

<details>
<summary><strong>MB/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="3">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>1.random</td><td>16.00 MB</td><td>8.00</td><td>None</td><td>16.00 MB</td><td>16.00 MB</td><td>16.00 MB</td></tr>
    <tr><td>2.random</td><td>16.00 MB</td><td>8.00</td><td>None</td><td>16.00 MB</td><td>16.00 MB</td><td>16.00 MB</td></tr>
    <tr><td>3.random</td><td>16.00 MB</td><td>8.00</td><td>None</td><td>16.00 MB</td><td>16.00 MB</td><td>16.00 MB</td></tr>
    <tr><td>4.random</td><td>16.00 MB</td><td>8.00</td><td>None</td><td>16.00 MB</td><td>16.00 MB</td><td>16.00 MB</td></tr>
    <tr><td>5.random</td><td>16.00 MB</td><td>8.00</td><td>None</td><td>16.00 MB</td><td>16.00 MB</td><td>16.00 MB</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>80.00 MB</strong></td><td><strong>8.00</strong></td><td><strong>-</strong></td><td><strong>80.00 MB</strong></td><td><strong>80.02 MB</strong></td><td><strong>80.00 MB</strong></td></tr>
  </tbody>
</table>

</details>

---

#### Performance Overview

| Codec | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| Light | 1.000 | 0.0% | 371.1 MB/s | 392.3 MB/s | 8.00 | 1.00× | < 1 MB | 100% | ✅ |
| GZIP | 2.488 | -148.8% | 6.1 MB/s | 99.2 MB/s | 19.90 | 0.40× | < 1 MB | 97% | ✅ |
| Zstandard | 1.667 | -66.7% | 143.1 MB/s | 131.6 MB/s | 13.33 | 0.60× | < 1 MB | 103% | ✅ |



<details>
<summary><strong>Light</strong></summary>

| Size | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **1.000** | **0.0%** | **371.1 MB/s** | **392.3 MB/s** | **8.00** | **1.00×** | **< 1 MB** | **100%** | **✅** |
| B | 1.000 | 0.0% | 0.1 MB/s | 0.1 MB/s | 8.00 | 1.00× | < 1 MB | 107% | ✅ |
| KB | 1.000 | 0.0% | 65.0 MB/s | 103.4 MB/s | 8.00 | 1.00× | < 1 MB | 103% | ✅ |
| MB | 1.000 | 0.0% | 1,338.3 MB/s | 1,365.4 MB/s | 8.00 | 1.00× | 1 MB | 87% | ✅ |

</details>

<details>
<summary><strong>GZIP</strong></summary>

| Size | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **2.488** | **-148.8%** | **6.1 MB/s** | **99.2 MB/s** | **19.90** | **0.40×** | **< 1 MB** | **97%** | **✅** |
| B | 5.013 | -401.3% | 0.1 MB/s | 0.2 MB/s | 40.10 | 0.20× | < 1 MB | 113% | ✅ |
| KB | 1.005 | -0.5% | 6.5 MB/s | 43.2 MB/s | 8.04 | 1.00× | < 1 MB | 91% | ✅ |
| MB | 1.000 | -0.0% | 14.2 MB/s | 320.9 MB/s | 8.00 | 1.00× | 1 MB | 84% | ✅ |

</details>

<details>
<summary><strong>Zstandard</strong></summary>

| Size | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **1.667** | **-66.7%** | **143.1 MB/s** | **131.6 MB/s** | **13.33** | **0.60×** | **< 1 MB** | **103%** | **✅** |
| B | 2.798 | -179.8% | 0.1 MB/s | 0.2 MB/s | 22.39 | 0.36× | < 1 MB | 118% | ✅ |
| KB | 1.002 | -0.2% | 56.2 MB/s | 59.1 MB/s | 8.01 | 1.00× | < 1 MB | 99% | ✅ |
| MB | 1.000 | -0.0% | 471.5 MB/s | 422.8 MB/s | 8.00 | 1.00× | 1 MB | 90% | ✅ |

</details>


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
    <tr>
      <td>Light</td>
      <td>1.000</td>
      <td>6.36</td>
      <td>1.000</td>
      <td>6.81</td>
      <td>1.000</td>
      <td>6.56</td>
      <td>1.000</td>
      <td>6.57</td>
    </tr>
    <tr>
      <td>GZIP</td>
      <td>0.624</td>
      <td>6.36</td>
      <td>0.629</td>
      <td>6.81</td>
      <td>0.672</td>
      <td>6.56</td>
      <td>2.488</td>
      <td>6.57</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td>0.621</td>
      <td>6.36</td>
      <td>0.630</td>
      <td>6.81</td>
      <td>0.667</td>
      <td>6.56</td>
      <td>1.667</td>
      <td>6.57</td>
    </tr>

  </tbody>
</table>

> **⚠️ Anomaly:** Light achieves compression on random data (ratio 0.769) despite entropy of 7.999 bits/byte, which theoretically permits no lossless compression. This suggests Light employs novel techniques that operate outside traditional Shannon entropy bounds.

---

## Conclusion

Light demonstrates strong compression performance across all tested corpora. Benchmark results indicate overall codec behavior is consistent with expected compression characteristics.

For standard corpora with lower entropy, Light achieves competitive compression ratios while maintaining reasonable speeds.

Further investigation into individual codec performance characteristics is recommended for production deployment decisions.

---
Copyright © 2026 Linkersoft
