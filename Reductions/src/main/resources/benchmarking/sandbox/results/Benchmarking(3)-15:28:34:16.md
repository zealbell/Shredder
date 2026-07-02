# Light Benchmarking Report

> **Report Generated:** February 16, 2026 15:28:33  
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

This report presents comprehensive benchmarking results for Harmattan against standard compression algorithms. The benchmarks evaluate performance against standardized test corpora (`Canterbury`, `Silesia`, `Wikipedia`) and a `Random` dataset. Key metrics include compression ratio, space savings, compression speed, decompression speed, and theoretical entropy analysis.

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
      <td>0.000</td>
    </tr>
    <tr>
      <td><strong>Best Space Savings</strong></td>
      <td>Zstandard</td>
      <td>100.0%</td>
    </tr>
    <tr>
      <td><strong>Fastest Compression</strong></td>
      <td>Light</td>
      <td>1,805.6 MB/s</td>
    </tr>
    <tr>
      <td><strong>Fastest Decompression</strong></td>
      <td>Light</td>
      <td>1,827.7 MB/s</td>
    </tr>
  </tbody>
</table>

---

## Participating Codecs

The following codecs were included in this benchmark run:

<table>
  <thead>
    <tr>
      <th>Codec</th>
      <th>Library</th>
      <th>Version</th>
      <th>Streaming</th>
      <th>Structure</th>
      <th>Notes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Light</td>
      <td>Custom</td>
      <td>1.0</td>
      <td>Yes</td>
      <td>-</td>
      <td>Entropy-defying custom codec</td>
    </tr>
    <tr>
      <td>GZIP</td>
      <td>Apache Commons Compress</td>
      <td>1.26.0</td>
      <td>Yes</td>
      <td>{ [header(10B)][payload(8391168B)][trailer(8B)] }</td>
      <td>DEFLATE algorithm with sliding window.</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td>zstd-jni</td>
      <td>1.5.5-11</td>
      <td>Yes</td>
      <td>{ [magic(4B)][frame_header(2-14B)][blocks(variable)][checksum(0-4B)] }</td>
      <td>Dictionary-based algorithm with finite state entropy. Excellent real-time speed and high compression ratio. Larger memory footprint.</td>
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
    <tr><td>artificl/a.txt</td><td>1 B</td><td>0.00</td><td>Excellent</td><td>1 B</td><td>21 B</td><td>10 B</td></tr>
    <tr><td>artificl/aaa.txt</td><td>97.66 KB</td><td>0.00</td><td>Excellent</td><td>97.66 KB</td><td>133 B</td><td>19 B</td></tr>
    <tr><td>artificl/alphabet.txt</td><td>97.66 KB</td><td>4.70</td><td>High</td><td>97.66 KB</td><td>302 B</td><td>43 B</td></tr>
    <tr><td>artificl/random.txt</td><td>97.66 KB</td><td>6.00</td><td>Moderate</td><td>97.66 KB</td><td>73.97 KB</td><td>73.29 KB</td></tr>
    <tr><td>calgary/bib</td><td>108.65 KB</td><td>5.20</td><td>Moderate</td><td>108.65 KB</td><td>34.41 KB</td><td>36.04 KB</td></tr>
    <tr><td>calgary/book1</td><td>750.75 KB</td><td>4.53</td><td>High</td><td>750.75 KB</td><td>306.24 KB</td><td>298.38 KB</td></tr>
    <tr><td>calgary/book2</td><td>596.54 KB</td><td>4.79</td><td>High</td><td>596.54 KB</td><td>201.83 KB</td><td>199.16 KB</td></tr>
    <tr><td>calgary/geo</td><td>100.00 KB</td><td>5.65</td><td>Moderate</td><td>100.00 KB</td><td>66.84 KB</td><td>67.21 KB</td></tr>
    <tr><td>calgary/news</td><td>368.27 KB</td><td>5.19</td><td>Moderate</td><td>368.27 KB</td><td>141.42 KB</td><td>134.78 KB</td></tr>
    <tr><td>calgary/obj1</td><td>21.00 KB</td><td>5.95</td><td>Moderate</td><td>21.00 KB</td><td>10.09 KB</td><td>10.52 KB</td></tr>
    <tr><td>calgary/obj2</td><td>241.03 KB</td><td>6.26</td><td>Low</td><td>241.03 KB</td><td>79.61 KB</td><td>82.36 KB</td></tr>
    <tr><td>calgary/paper1</td><td>51.92 KB</td><td>4.98</td><td>High</td><td>51.92 KB</td><td>18.13 KB</td><td>19.02 KB</td></tr>
    <tr><td>calgary/paper2</td><td>80.27 KB</td><td>4.60</td><td>High</td><td>80.27 KB</td><td>29.07 KB</td><td>29.85 KB</td></tr>
    <tr><td>calgary/paper3</td><td>45.44 KB</td><td>4.67</td><td>High</td><td>45.44 KB</td><td>17.67 KB</td><td>18.26 KB</td></tr>
    <tr><td>calgary/paper4</td><td>12.97 KB</td><td>4.70</td><td>High</td><td>12.97 KB</td><td>5.40 KB</td><td>5.52 KB</td></tr>
    <tr><td>calgary/paper5</td><td>11.67 KB</td><td>4.94</td><td>High</td><td>11.67 KB</td><td>4.87 KB</td><td>5.08 KB</td></tr>
    <tr><td>calgary/paper6</td><td>37.21 KB</td><td>5.01</td><td>Moderate</td><td>37.21 KB</td><td>13.00 KB</td><td>13.69 KB</td></tr>
    <tr><td>calgary/pic</td><td>501.19 KB</td><td>1.21</td><td>Excellent</td><td>501.19 KB</td><td>55.15 KB</td><td>53.17 KB</td></tr>
    <tr><td>calgary/progc</td><td>38.68 KB</td><td>5.20</td><td>Moderate</td><td>38.68 KB</td><td>13.04 KB</td><td>13.80 KB</td></tr>
    <tr><td>calgary/progl</td><td>69.97 KB</td><td>4.77</td><td>High</td><td>69.97 KB</td><td>15.89 KB</td><td>17.17 KB</td></tr>
    <tr><td>calgary/progp</td><td>48.22 KB</td><td>4.87</td><td>High</td><td>48.22 KB</td><td>10.98 KB</td><td>11.86 KB</td></tr>
    <tr><td>calgary/trans</td><td>91.50 KB</td><td>5.53</td><td>Moderate</td><td>91.50 KB</td><td>18.61 KB</td><td>20.08 KB</td></tr>
    <tr><td>cantrbry/alice29.txt</td><td>148.52 KB</td><td>4.57</td><td>High</td><td>148.52 KB</td><td>53.14 KB</td><td>54.52 KB</td></tr>
    <tr><td>cantrbry/asyoulik.txt</td><td>122.25 KB</td><td>4.81</td><td>High</td><td>122.25 KB</td><td>47.76 KB</td><td>48.85 KB</td></tr>
    <tr><td>cantrbry/cp.html</td><td>24.03 KB</td><td>5.23</td><td>Moderate</td><td>24.03 KB</td><td>7.79 KB</td><td>8.27 KB</td></tr>
    <tr><td>cantrbry/fields.c</td><td>10.89 KB</td><td>5.01</td><td>Moderate</td><td>10.89 KB</td><td>3.06 KB</td><td>3.33 KB</td></tr>
    <tr><td>cantrbry/grammar.lsp</td><td>3.63 KB</td><td>4.63</td><td>High</td><td>3.63 KB</td><td>1.21 KB</td><td>1.27 KB</td></tr>
    <tr><td>cantrbry/kennedy.xls</td><td>1005.61 KB</td><td>3.57</td><td>Very High</td><td>1005.61 KB</td><td>199.22 KB</td><td>109.12 KB</td></tr>
    <tr><td>cantrbry/lcet10.txt</td><td>416.75 KB</td><td>4.67</td><td>High</td><td>416.75 KB</td><td>141.52 KB</td><td>137.88 KB</td></tr>
    <tr><td>cantrbry/plrabn12.txt</td><td>470.57 KB</td><td>4.53</td><td>High</td><td>470.57 KB</td><td>190.70 KB</td><td>187.28 KB</td></tr>
    <tr><td>cantrbry/ptt5</td><td>501.19 KB</td><td>1.21</td><td>Excellent</td><td>501.19 KB</td><td>55.15 KB</td><td>53.17 KB</td></tr>
    <tr><td>cantrbry/sum</td><td>37.34 KB</td><td>5.33</td><td>Moderate</td><td>37.34 KB</td><td>12.70 KB</td><td>13.08 KB</td></tr>
    <tr><td>cantrbry/xargs.1</td><td>4.13 KB</td><td>4.90</td><td>High</td><td>4.13 KB</td><td>1.71 KB</td><td>1.79 KB</td></tr>
    <tr><td>large/E.coli</td><td>4.42 MB</td><td>2.00</td><td>Excellent</td><td>4.42 MB</td><td>1.28 MB</td><td>1.34 MB</td></tr>
    <tr><td>large/bible.txt</td><td>3.86 MB</td><td>4.34</td><td>High</td><td>3.86 MB</td><td>1.14 MB</td><td>1.12 MB</td></tr>
    <tr><td>large/world192.txt</td><td>2.36 MB</td><td>5.00</td><td>High</td><td>2.36 MB</td><td>707.99 KB</td><td>642.31 KB</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>16.71 MB</strong></td><td><strong>4.40</strong></td><td><strong>-</strong></td><td><strong>16.71 MB</strong></td><td><strong>4.90 MB</strong></td><td><strong>4.77 MB</strong></td></tr>
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
| Light | 1.000 | 0.0% | 260.0 MB/s | 298.4 MB/s | 8.00 | 1.00× | 9 MB | 93% | ✅ |
| GZIP | 0.851 | 14.9% | 7.5 MB/s | 46.1 MB/s | 6.81 | 1.18× | 9 MB | 70% | ✅ |
| Zstandard | 0.604 | 39.6% | 45.5 MB/s | 74.2 MB/s | 4.83 | 1.66× | 9 MB | 78% | ✅ |



<details>
<summary><strong>Light</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **1.000** | **0.0%** | **260.0 MB/s** | **298.4 MB/s** | **8.00** | **1.00×** | **9 MB** | **93%** | **✅** |
| archives | 1.000 | 0.0% | 553.3 MB/s | 497.3 MB/s | 8.00 | 1.00× | 8 MB | 81% | ✅ |
| contents | 1.000 | 0.0% | 177.8 MB/s | 224.2 MB/s | 8.00 | 1.00× | 9 MB | 98% | ✅ |
| blobs | 1.000 | 0.0% | 707.0 MB/s | 767.4 MB/s | 8.00 | 1.00× | 9 MB | 67% | ✅ |

</details>

<details>
<summary><strong>GZIP</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.851** | **14.9%** | **7.5 MB/s** | **46.1 MB/s** | **6.81** | **1.18×** | **9 MB** | **70%** | **✅** |
| archives | 0.963 | 3.7% | 4.7 MB/s | 74.1 MB/s | 7.70 | 1.04× | 8 MB | 48% | ✅ |
| contents | 0.901 | 9.9% | 7.8 MB/s | 39.6 MB/s | 7.21 | 1.11× | 9 MB | 73% | ✅ |
| blobs | 0.284 | 71.6% | 7.8 MB/s | 77.1 MB/s | 2.27 | 3.52× | 9 MB | 66% | ✅ |

</details>

<details>
<summary><strong>Zstandard</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.604** | **39.6%** | **45.5 MB/s** | **74.2 MB/s** | **4.83** | **1.66×** | **9 MB** | **78%** | **✅** |
| archives | 0.968 | 3.2% | 68.6 MB/s | 117.3 MB/s | 7.74 | 1.03× | 9 MB | 40% | ✅ |
| contents | 0.600 | 40.0% | 38.8 MB/s | 63.7 MB/s | 4.80 | 1.67× | 9 MB | 83% | ✅ |
| blobs | 0.275 | 72.5% | 81.9 MB/s | 125.5 MB/s | 2.20 | 3.64× | 10 MB | 72% | ✅ |

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
    <tr><td>extra/README.md</td><td>1.18 KB</td><td>4.40</td><td>High</td><td>1.18 KB</td><td>616 B</td><td>641 B</td></tr>
    <tr><td>extra/dickens</td><td>9.72 MB</td><td>4.53</td><td>High</td><td>9.72 MB</td><td>3.69 MB</td><td>3.51 MB</td></tr>
    <tr><td>extra/mr</td><td>9.51 MB</td><td>3.68</td><td>Very High</td><td>9.51 MB</td><td>3.51 MB</td><td>3.38 MB</td></tr>
    <tr><td>extra/nci</td><td>32.00 MB</td><td>2.43</td><td>Very High</td><td>32.00 MB</td><td>3.05 MB</td><td>2.72 MB</td></tr>
    <tr><td>extra/ooffice</td><td>5.87 MB</td><td>6.64</td><td>Low</td><td>5.87 MB</td><td>2.95 MB</td><td>3.00 MB</td></tr>
    <tr><td>extra/osdb</td><td>9.62 MB</td><td>6.59</td><td>Low</td><td>9.62 MB</td><td>3.52 MB</td><td>3.35 MB</td></tr>
    <tr><td>extra/reymont</td><td>6.32 MB</td><td>4.84</td><td>High</td><td>6.32 MB</td><td>1.77 MB</td><td>1.86 MB</td></tr>
    <tr><td>extra/sao</td><td>6.92 MB</td><td>7.53</td><td>None</td><td>6.92 MB</td><td>5.08 MB</td><td>5.30 MB</td></tr>
    <tr><td>extra/webster</td><td>39.54 MB</td><td>4.97</td><td>High</td><td>39.54 MB</td><td>11.65 MB</td><td>11.61 MB</td></tr>
    <tr><td>extra/x-ray</td><td>8.08 MB</td><td>6.60</td><td>Low</td><td>8.08 MB</td><td>5.77 MB</td><td>5.80 MB</td></tr>
    <tr><td>mozilla/TestGtkEmbed</td><td>40.00 KB</td><td>4.47</td><td>High</td><td>40.00 KB</td><td>11.11 KB</td><td>11.59 KB</td></tr>
    <tr><td>mozilla/TestGtkEmbedChild</td><td>24.00 KB</td><td>3.19</td><td>Very High</td><td>24.00 KB</td><td>5.76 KB</td><td>6.00 KB</td></tr>
    <tr><td>mozilla/TestGtkEmbedNotebook</td><td>24.00 KB</td><td>2.48</td><td>Very High</td><td>24.00 KB</td><td>4.44 KB</td><td>4.54 KB</td></tr>
    <tr><td>mozilla/TestGtkEmbedSocket</td><td>24.00 KB</td><td>2.65</td><td>Very High</td><td>24.00 KB</td><td>4.80 KB</td><td>4.97 KB</td></tr>
    <tr><td>mozilla/bloaturls.txt</td><td>217 B</td><td>4.17</td><td>High</td><td>217 B</td><td>108 B</td><td>102 B</td></tr>
    <tr><td>mozilla/chrome/US.jar</td><td>22.36 KB</td><td>7.78</td><td>None</td><td>22.36 KB</td><td>16.91 KB</td><td>16.84 KB</td></tr>
    <tr><td>mozilla/chrome/chatzilla.jar</td><td>104.19 KB</td><td>7.96</td><td>None</td><td>104.19 KB</td><td>97.51 KB</td><td>97.09 KB</td></tr>
    <tr><td>mozilla/chrome/chromelist.txt</td><td>188.25 KB</td><td>4.51</td><td>High</td><td>188.25 KB</td><td>17.28 KB</td><td>18.23 KB</td></tr>
    <tr><td>mozilla/chrome/classic.jar</td><td>284.25 KB</td><td>7.72</td><td>None</td><td>284.25 KB</td><td>217.97 KB</td><td>214.06 KB</td></tr>
    <tr><td>mozilla/chrome/comm.jar</td><td>866.28 KB</td><td>7.95</td><td>None</td><td>866.28 KB</td><td>801.73 KB</td><td>796.42 KB</td></tr>
    <tr><td>mozilla/chrome/content-packs.jar</td><td>3.49 KB</td><td>7.82</td><td>None</td><td>3.49 KB</td><td>3.26 KB</td><td>3.19 KB</td></tr>
    <tr><td>mozilla/chrome/embed-sample.jar</td><td>10.68 KB</td><td>7.81</td><td>None</td><td>10.68 KB</td><td>9.06 KB</td><td>9.02 KB</td></tr>
    <tr><td>mozilla/chrome/en-US.jar</td><td>667.22 KB</td><td>7.92</td><td>None</td><td>667.22 KB</td><td>603.79 KB</td><td>600.04 KB</td></tr>
    <tr><td>mozilla/chrome/en-mac.jar</td><td>2.69 KB</td><td>7.28</td><td>Low</td><td>2.69 KB</td><td>1.95 KB</td><td>1.89 KB</td></tr>
    <tr><td>mozilla/chrome/en-unix.jar</td><td>4.57 KB</td><td>7.20</td><td>Low</td><td>4.57 KB</td><td>2.97 KB</td><td>2.95 KB</td></tr>
    <tr><td>mozilla/chrome/en-win.jar</td><td>3.76 KB</td><td>7.22</td><td>Low</td><td>3.76 KB</td><td>2.53 KB</td><td>2.49 KB</td></tr>
    <tr><td>mozilla/chrome/forms.jar</td><td>20.89 KB</td><td>7.87</td><td>None</td><td>20.89 KB</td><td>18.50 KB</td><td>18.54 KB</td></tr>
    <tr><td>mozilla/chrome/help.jar</td><td>11.78 KB</td><td>7.94</td><td>None</td><td>11.78 KB</td><td>11.28 KB</td><td>11.24 KB</td></tr>
    <tr><td>mozilla/chrome/inspector.jar</td><td>186.61 KB</td><td>7.88</td><td>None</td><td>186.61 KB</td><td>156.16 KB</td><td>153.88 KB</td></tr>
    <tr><td>mozilla/chrome/installed-chrome.txt</td><td>5.25 KB</td><td>4.41</td><td>High</td><td>5.25 KB</td><td>560 B</td><td>608 B</td></tr>
    <tr><td>mozilla/chrome/messenger.jar</td><td>456.25 KB</td><td>7.96</td><td>None</td><td>456.25 KB</td><td>431.97 KB</td><td>428.40 KB</td></tr>
    <tr><td>mozilla/chrome/modern.jar</td><td>537.74 KB</td><td>7.88</td><td>None</td><td>537.74 KB</td><td>457.35 KB</td><td>451.18 KB</td></tr>
    <tr><td>mozilla/chrome/pipnss.jar</td><td>485 B</td><td>6.99</td><td>Low</td><td>485 B</td><td>452 B</td><td>440 B</td></tr>
    <tr><td>mozilla/chrome/pippki.jar</td><td>90.05 KB</td><td>7.92</td><td>None</td><td>90.05 KB</td><td>82.49 KB</td><td>81.79 KB</td></tr>
    <tr><td>mozilla/chrome/toolkit.jar</td><td>221.90 KB</td><td>7.97</td><td>None</td><td>221.90 KB</td><td>211.80 KB</td><td>210.37 KB</td></tr>
    <tr><td>mozilla/chrome/venkman.jar</td><td>171.22 KB</td><td>7.90</td><td>None</td><td>171.22 KB</td><td>146.37 KB</td><td>145.71 KB</td></tr>
    <tr><td>mozilla/components/absync.xpt</td><td>1.80 KB</td><td>4.91</td><td>High</td><td>1.80 KB</td><td>818 B</td><td>856 B</td></tr>
    <tr><td>mozilla/components/accessibility.xpt</td><td>6.49 KB</td><td>5.32</td><td>Moderate</td><td>6.49 KB</td><td>2.56 KB</td><td>2.70 KB</td></tr>
    <tr><td>mozilla/components/addrbook.xpt</td><td>13.12 KB</td><td>5.38</td><td>Moderate</td><td>13.12 KB</td><td>5.38 KB</td><td>5.57 KB</td></tr>
    <tr><td>mozilla/components/appshell.xpt</td><td>3.05 KB</td><td>5.01</td><td>Moderate</td><td>3.05 KB</td><td>1.48 KB</td><td>1.52 KB</td></tr>
    <tr><td>mozilla/components/autocomplete.xpt</td><td>1.76 KB</td><td>4.97</td><td>High</td><td>1.76 KB</td><td>890 B</td><td>903 B</td></tr>
    <tr><td>mozilla/components/autoconfig.xpt</td><td>392 B</td><td>4.16</td><td>High</td><td>392 B</td><td>246 B</td><td>257 B</td></tr>
    <tr><td>mozilla/components/bookmarks.xpt</td><td>801 B</td><td>4.88</td><td>High</td><td>801 B</td><td>419 B</td><td>442 B</td></tr>
    <tr><td>mozilla/components/caps.xpt</td><td>2.56 KB</td><td>5.28</td><td>Moderate</td><td>2.56 KB</td><td>1.28 KB</td><td>1.32 KB</td></tr>
    <tr><td>mozilla/components/chardet.xpt</td><td>514 B</td><td>4.54</td><td>High</td><td>514 B</td><td>290 B</td><td>300 B</td></tr>
    <tr><td>mozilla/components/chatzilla-service.js</td><td>10.86 KB</td><td>5.05</td><td>Moderate</td><td>10.86 KB</td><td>2.94 KB</td><td>3.10 KB</td></tr>
    <tr><td>mozilla/components/chrome.xpt</td><td>1.36 KB</td><td>4.83</td><td>High</td><td>1.36 KB</td><td>657 B</td><td>679 B</td></tr>
    <tr><td>mozilla/components/commandhandler.xpt</td><td>1.73 KB</td><td>4.96</td><td>High</td><td>1.73 KB</td><td>811 B</td><td>848 B</td></tr>
    <tr><td>mozilla/components/composer.xpt</td><td>492 B</td><td>3.80</td><td>Very High</td><td>492 B</td><td>245 B</td><td>260 B</td></tr>
    <tr><td>mozilla/components/content.xpt</td><td>223 B</td><td>4.58</td><td>High</td><td>223 B</td><td>171 B</td><td>186 B</td></tr>
    <tr><td>mozilla/components/content_base.xpt</td><td>5.09 KB</td><td>5.05</td><td>Moderate</td><td>5.09 KB</td><td>2.33 KB</td><td>2.38 KB</td></tr>
    <tr><td>mozilla/components/content_html.xpt</td><td>847 B</td><td>4.22</td><td>High</td><td>847 B</td><td>400 B</td><td>408 B</td></tr>
    <tr><td>mozilla/components/content_xsl.xpt</td><td>355 B</td><td>3.26</td><td>Very High</td><td>355 B</td><td>179 B</td><td>192 B</td></tr>
    <tr><td>mozilla/components/cookie.xpt</td><td>1.25 KB</td><td>4.87</td><td>High</td><td>1.25 KB</td><td>691 B</td><td>718 B</td></tr>
    <tr><td>mozilla/components/directory.xpt</td><td>445 B</td><td>3.36</td><td>Very High</td><td>445 B</td><td>222 B</td><td>223 B</td></tr>
    <tr><td>mozilla/components/docshell.xpt</td><td>7.77 KB</td><td>5.25</td><td>Moderate</td><td>7.77 KB</td><td>3.25 KB</td><td>3.40 KB</td></tr>
    <tr><td>mozilla/components/dom.xpt</td><td>688 B</td><td>4.18</td><td>High</td><td>688 B</td><td>326 B</td><td>335 B</td></tr>
    <tr><td>mozilla/components/dom_base.xpt</td><td>8.06 KB</td><td>4.51</td><td>High</td><td>8.06 KB</td><td>2.86 KB</td><td>2.92 KB</td></tr>
    <tr><td>mozilla/components/dom_core.xpt</td><td>4.98 KB</td><td>5.26</td><td>Moderate</td><td>4.98 KB</td><td>2.05 KB</td><td>2.12 KB</td></tr>
    <tr><td>mozilla/components/dom_css.xpt</td><td>10.28 KB</td><td>5.41</td><td>Moderate</td><td>10.28 KB</td><td>3.41 KB</td><td>3.41 KB</td></tr>
    <tr><td>mozilla/components/dom_events.xpt</td><td>5.10 KB</td><td>5.17</td><td>Moderate</td><td>5.10 KB</td><td>2.39 KB</td><td>2.42 KB</td></tr>
    <tr><td>mozilla/components/dom_html.xpt</td><td>17.17 KB</td><td>5.20</td><td>Moderate</td><td>17.17 KB</td><td>5.35 KB</td><td>5.42 KB</td></tr>
    <tr><td>mozilla/components/dom_range.xpt</td><td>1.45 KB</td><td>4.83</td><td>High</td><td>1.45 KB</td><td>739 B</td><td>772 B</td></tr>
    <tr><td>mozilla/components/dom_stylesheets.xpt</td><td>812 B</td><td>4.57</td><td>High</td><td>812 B</td><td>460 B</td><td>470 B</td></tr>
    <tr><td>mozilla/components/dom_svg.xpt</td><td>14.27 KB</td><td>5.58</td><td>Moderate</td><td>14.27 KB</td><td>5.38 KB</td><td>5.57 KB</td></tr>
    <tr><td>mozilla/components/dom_traversal.xpt</td><td>1.32 KB</td><td>5.03</td><td>Moderate</td><td>1.32 KB</td><td>721 B</td><td>734 B</td></tr>
    <tr><td>mozilla/components/dom_views.xpt</td><td>262 B</td><td>4.25</td><td>High</td><td>262 B</td><td>181 B</td><td>197 B</td></tr>
    <tr><td>mozilla/components/dom_xbl.xpt</td><td>570 B</td><td>3.82</td><td>Very High</td><td>570 B</td><td>273 B</td><td>284 B</td></tr>
    <tr><td>mozilla/components/dom_xpath.xpt</td><td>1.40 KB</td><td>4.92</td><td>High</td><td>1.40 KB</td><td>703 B</td><td>718 B</td></tr>
    <tr><td>mozilla/components/dom_xul.xpt</td><td>5.99 KB</td><td>5.17</td><td>Moderate</td><td>5.99 KB</td><td>2.35 KB</td><td>2.38 KB</td></tr>
    <tr><td>mozilla/components/downloadmanager.xpt</td><td>1.06 KB</td><td>3.94</td><td>Very High</td><td>1.06 KB</td><td>470 B</td><td>480 B</td></tr>
    <tr><td>mozilla/components/editor.xpt</td><td>12.64 KB</td><td>5.16</td><td>Moderate</td><td>12.64 KB</td><td>4.72 KB</td><td>4.95 KB</td></tr>
    <tr><td>mozilla/components/embed_base.xpt</td><td>246 B</td><td>3.73</td><td>Very High</td><td>246 B</td><td>162 B</td><td>172 B</td></tr>
    <tr><td>mozilla/components/exthandler.xpt</td><td>1.13 KB</td><td>4.40</td><td>High</td><td>1.13 KB</td><td>566 B</td><td>583 B</td></tr>
    <tr><td>mozilla/components/filepicker.xpt</td><td>483 B</td><td>4.30</td><td>High</td><td>483 B</td><td>289 B</td><td>287 B</td></tr>
    <tr><td>mozilla/components/find.xpt</td><td>1.09 KB</td><td>4.60</td><td>High</td><td>1.09 KB</td><td>504 B</td><td>525 B</td></tr>
    <tr><td>mozilla/components/gfx.xpt</td><td>5.95 KB</td><td>5.22</td><td>Moderate</td><td>5.95 KB</td><td>2.34 KB</td><td>2.39 KB</td></tr>
    <tr><td>mozilla/components/gfx2.xpt</td><td>1018 B</td><td>4.91</td><td>High</td><td>1018 B</td><td>553 B</td><td>564 B</td></tr>
    <tr><td>mozilla/components/helperAppDlg.xpt</td><td>369 B</td><td>3.84</td><td>Very High</td><td>369 B</td><td>216 B</td><td>220 B</td></tr>
    <tr><td>mozilla/components/history.xpt</td><td>490 B</td><td>4.57</td><td>High</td><td>490 B</td><td>328 B</td><td>340 B</td></tr>
    <tr><td>mozilla/components/htmlparser.xpt</td><td>401 B</td><td>4.57</td><td>High</td><td>401 B</td><td>266 B</td><td>268 B</td></tr>
    <tr><td>mozilla/components/imglib2.xpt</td><td>2.40 KB</td><td>4.76</td><td>High</td><td>2.40 KB</td><td>1.16 KB</td><td>1.22 KB</td></tr>
    <tr><td>mozilla/components/impComm4xMail.xpt</td><td>202 B</td><td>4.08</td><td>High</td><td>202 B</td><td>159 B</td><td>159 B</td></tr>
    <tr><td>mozilla/components/import.xpt</td><td>3.39 KB</td><td>4.82</td><td>High</td><td>3.39 KB</td><td>1.35 KB</td><td>1.40 KB</td></tr>
    <tr><td>mozilla/components/inspector.xpt</td><td>3.57 KB</td><td>4.97</td><td>High</td><td>3.57 KB</td><td>1.66 KB</td><td>1.73 KB</td></tr>
    <tr><td>mozilla/components/intl.xpt</td><td>598 B</td><td>4.76</td><td>High</td><td>598 B</td><td>338 B</td><td>345 B</td></tr>
    <tr><td>mozilla/components/jar.xpt</td><td>1018 B</td><td>4.37</td><td>High</td><td>1018 B</td><td>557 B</td><td>565 B</td></tr>
    <tr><td>mozilla/components/jsconsole-clhandler.js</td><td>3.92 KB</td><td>4.98</td><td>High</td><td>3.92 KB</td><td>1.44 KB</td><td>1.51 KB</td></tr>
    <tr><td>mozilla/components/jsconsole.xpt</td><td>222 B</td><td>3.51</td><td>Very High</td><td>222 B</td><td>150 B</td><td>159 B</td></tr>
    <tr><td>mozilla/components/jsdservice.xpt</td><td>5.83 KB</td><td>5.46</td><td>Moderate</td><td>5.83 KB</td><td>2.74 KB</td><td>2.87 KB</td></tr>
    <tr><td>mozilla/components/jsurl.xpt</td><td>273 B</td><td>3.77</td><td>Very High</td><td>273 B</td><td>180 B</td><td>183 B</td></tr>
    <tr><td>mozilla/components/layout_base.xpt</td><td>244 B</td><td>3.79</td><td>Very High</td><td>244 B</td><td>161 B</td><td>165 B</td></tr>
    <tr><td>mozilla/components/layout_xul.xpt</td><td>2.36 KB</td><td>4.94</td><td>High</td><td>2.36 KB</td><td>1.10 KB</td><td>1.13 KB</td></tr>
    <tr><td>mozilla/components/layout_xul_tree.xpt</td><td>2.83 KB</td><td>4.98</td><td>High</td><td>2.83 KB</td><td>1.33 KB</td><td>1.38 KB</td></tr>
    <tr><td>mozilla/components/libabsyncsvc.so</td><td>160.00 KB</td><td>5.82</td><td>Moderate</td><td>160.00 KB</td><td>57.55 KB</td><td>60.78 KB</td></tr>
    <tr><td>mozilla/components/libaccessibility.so</td><td>448.00 KB</td><td>5.68</td><td>Moderate</td><td>448.00 KB</td><td>118.27 KB</td><td>117.40 KB</td></tr>
    <tr><td>mozilla/components/libaddrbook.so</td><td>912.00 KB</td><td>6.00</td><td>Low</td><td>912.00 KB</td><td>290.74 KB</td><td>287.64 KB</td></tr>
    <tr><td>mozilla/components/libappcomps.so</td><td>808.00 KB</td><td>6.14</td><td>Low</td><td>808.00 KB</td><td>282.57 KB</td><td>277.81 KB</td></tr>
    <tr><td>mozilla/components/libautoconfig.so</td><td>104.00 KB</td><td>5.40</td><td>Moderate</td><td>104.00 KB</td><td>31.86 KB</td><td>33.74 KB</td></tr>
    <tr><td>mozilla/components/libcaps.so</td><td>192.00 KB</td><td>5.81</td><td>Moderate</td><td>192.00 KB</td><td>64.54 KB</td><td>66.86 KB</td></tr>
    <tr><td>mozilla/components/libchardet.so</td><td>160.00 KB</td><td>5.67</td><td>Moderate</td><td>160.00 KB</td><td>47.50 KB</td><td>48.74 KB</td></tr>
    <tr><td>mozilla/components/libchrome.so</td><td>208.00 KB</td><td>6.12</td><td>Low</td><td>208.00 KB</td><td>73.87 KB</td><td>74.96 KB</td></tr>
    <tr><td>mozilla/components/libcomposer.so</td><td>360.00 KB</td><td>5.96</td><td>Moderate</td><td>360.00 KB</td><td>107.18 KB</td><td>105.32 KB</td></tr>
    <tr><td>mozilla/components/libcookie.so</td><td>168.00 KB</td><td>5.74</td><td>Moderate</td><td>168.00 KB</td><td>54.94 KB</td><td>57.07 KB</td></tr>
    <tr><td>mozilla/components/libdocshell.so</td><td>344.00 KB</td><td>5.91</td><td>Moderate</td><td>344.00 KB</td><td>114.40 KB</td><td>115.02 KB</td></tr>
    <tr><td>mozilla/components/libeditor.so</td><td>1.37 MB</td><td>6.34</td><td>Low</td><td>1.37 MB</td><td>507.13 KB</td><td>493.15 KB</td></tr>
    <tr><td>mozilla/components/libembedcomponents.so</td><td>344.00 KB</td><td>5.98</td><td>Moderate</td><td>344.00 KB</td><td>114.17 KB</td><td>114.30 KB</td></tr>
    <tr><td>mozilla/components/libfileview.so</td><td>48.00 KB</td><td>4.85</td><td>High</td><td>48.00 KB</td><td>16.09 KB</td><td>17.12 KB</td></tr>
    <tr><td>mozilla/components/libgfx2.so</td><td>32.00 KB</td><td>3.71</td><td>Very High</td><td>32.00 KB</td><td>7.78 KB</td><td>7.95 KB</td></tr>
    <tr><td>mozilla/components/libgfx_gtk.so</td><td>376.00 KB</td><td>5.94</td><td>Moderate</td><td>376.00 KB</td><td>143.41 KB</td><td>148.96 KB</td></tr>
    <tr><td>mozilla/components/libgfxps.so</td><td>448.00 KB</td><td>4.32</td><td>High</td><td>448.00 KB</td><td>106.35 KB</td><td>107.21 KB</td></tr>
    <tr><td>mozilla/components/libgkcontent.so</td><td>6.71 MB</td><td>5.95</td><td>Moderate</td><td>6.71 MB</td><td>1.96 MB</td><td>1.85 MB</td></tr>
    <tr><td>mozilla/components/libgklayout.so</td><td>4.09 MB</td><td>6.12</td><td>Low</td><td>4.09 MB</td><td>1.44 MB</td><td>1.43 MB</td></tr>
    <tr><td>mozilla/components/libgkplugin.so</td><td>328.00 KB</td><td>5.82</td><td>Moderate</td><td>328.00 KB</td><td>110.79 KB</td><td>113.34 KB</td></tr>
    <tr><td>mozilla/components/libgkview.so</td><td>152.00 KB</td><td>5.66</td><td>Moderate</td><td>152.00 KB</td><td>55.44 KB</td><td>58.35 KB</td></tr>
    <tr><td>mozilla/components/libhtmlpars.so</td><td>624.00 KB</td><td>5.98</td><td>Moderate</td><td>624.00 KB</td><td>224.76 KB</td><td>231.44 KB</td></tr>
    <tr><td>mozilla/components/libimgbmp.so</td><td>40.00 KB</td><td>4.88</td><td>High</td><td>40.00 KB</td><td>14.89 KB</td><td>15.49 KB</td></tr>
    <tr><td>mozilla/components/libimggif.so</td><td>40.00 KB</td><td>4.42</td><td>High</td><td>40.00 KB</td><td>14.10 KB</td><td>14.90 KB</td></tr>
    <tr><td>mozilla/components/libimgjpeg.so</td><td>104.00 KB</td><td>6.06</td><td>Low</td><td>104.00 KB</td><td>51.75 KB</td><td>55.39 KB</td></tr>
    <tr><td>mozilla/components/libimglib2.so</td><td>120.00 KB</td><td>5.55</td><td>Moderate</td><td>120.00 KB</td><td>39.28 KB</td><td>41.53 KB</td></tr>
    <tr><td>mozilla/components/libimgmng.so</td><td>400.00 KB</td><td>6.31</td><td>Low</td><td>400.00 KB</td><td>171.26 KB</td><td>179.63 KB</td></tr>
    <tr><td>mozilla/components/libimgpng.so</td><td>168.00 KB</td><td>6.24</td><td>Low</td><td>168.00 KB</td><td>84.80 KB</td><td>91.01 KB</td></tr>
    <tr><td>mozilla/components/libimgppm.so</td><td>24.00 KB</td><td>3.81</td><td>Very High</td><td>24.00 KB</td><td>6.96 KB</td><td>7.29 KB</td></tr>
    <tr><td>mozilla/components/libimpComm4xMail.so</td><td>64.00 KB</td><td>5.05</td><td>Moderate</td><td>64.00 KB</td><td>19.01 KB</td><td>19.93 KB</td></tr>
    <tr><td>mozilla/components/libimpText.so</td><td>72.00 KB</td><td>5.82</td><td>Moderate</td><td>72.00 KB</td><td>27.79 KB</td><td>29.65 KB</td></tr>
    <tr><td>mozilla/components/libimport.so</td><td>160.00 KB</td><td>5.80</td><td>Moderate</td><td>160.00 KB</td><td>54.56 KB</td><td>57.71 KB</td></tr>
    <tr><td>mozilla/components/libinspector.so</td><td>264.00 KB</td><td>5.88</td><td>Moderate</td><td>264.00 KB</td><td>97.10 KB</td><td>101.97 KB</td></tr>
    <tr><td>mozilla/components/libjar50.so</td><td>96.00 KB</td><td>5.62</td><td>Moderate</td><td>96.00 KB</td><td>35.18 KB</td><td>37.62 KB</td></tr>
    <tr><td>mozilla/components/libjsd.so</td><td>176.00 KB</td><td>5.56</td><td>Moderate</td><td>176.00 KB</td><td>53.84 KB</td><td>56.60 KB</td></tr>
    <tr><td>mozilla/components/libjsdom.so</td><td>776.00 KB</td><td>5.94</td><td>Moderate</td><td>776.00 KB</td><td>230.99 KB</td><td>228.87 KB</td></tr>
    <tr><td>mozilla/components/libjsloader.so</td><td>80.00 KB</td><td>5.63</td><td>Moderate</td><td>80.00 KB</td><td>27.48 KB</td><td>29.04 KB</td></tr>
    <tr><td>mozilla/components/libjsurl.so</td><td>56.00 KB</td><td>5.20</td><td>Moderate</td><td>56.00 KB</td><td>17.46 KB</td><td>18.20 KB</td></tr>
    <tr><td>mozilla/components/liblocalmail.so</td><td>560.00 KB</td><td>5.91</td><td>Moderate</td><td>560.00 KB</td><td>184.12 KB</td><td>184.18 KB</td></tr>
    <tr><td>mozilla/components/liblwbrk.so</td><td>40.00 KB</td><td>5.24</td><td>Moderate</td><td>40.00 KB</td><td>15.36 KB</td><td>16.13 KB</td></tr>
    <tr><td>mozilla/components/libmailnews.so</td><td>1.14 MB</td><td>6.02</td><td>Low</td><td>1.14 MB</td><td>375.18 KB</td><td>370.66 KB</td></tr>
    <tr><td>mozilla/components/libmime.so</td><td>392.00 KB</td><td>6.07</td><td>Low</td><td>392.00 KB</td><td>157.00 KB</td><td>163.33 KB</td></tr>
    <tr><td>mozilla/components/libmimeemitter.so</td><td>96.00 KB</td><td>5.46</td><td>Moderate</td><td>96.00 KB</td><td>31.60 KB</td><td>33.52 KB</td></tr>
    <tr><td>mozilla/components/libmork.so</td><td>440.00 KB</td><td>5.91</td><td>Moderate</td><td>440.00 KB</td><td>143.40 KB</td><td>147.96 KB</td></tr>
    <tr><td>mozilla/components/libmozbrwsr.so</td><td>56.00 KB</td><td>5.16</td><td>Moderate</td><td>56.00 KB</td><td>17.02 KB</td><td>17.70 KB</td></tr>
    <tr><td>mozilla/components/libmozfind.so</td><td>48.00 KB</td><td>4.88</td><td>High</td><td>48.00 KB</td><td>14.16 KB</td><td>14.71 KB</td></tr>
    <tr><td>mozilla/components/libmozldap.so</td><td>128.00 KB</td><td>5.75</td><td>Moderate</td><td>128.00 KB</td><td>42.42 KB</td><td>44.80 KB</td></tr>
    <tr><td>mozilla/components/libmozxfer.so</td><td>64.00 KB</td><td>5.08</td><td>Moderate</td><td>64.00 KB</td><td>20.18 KB</td><td>21.09 KB</td></tr>
    <tr><td>mozilla/components/libmsgcompose.so</td><td>768.00 KB</td><td>6.08</td><td>Low</td><td>768.00 KB</td><td>266.21 KB</td><td>264.31 KB</td></tr>
    <tr><td>mozilla/components/libmsgdb.so</td><td>264.00 KB</td><td>5.64</td><td>Moderate</td><td>264.00 KB</td><td>81.98 KB</td><td>85.13 KB</td></tr>
    <tr><td>mozilla/components/libmsgimap.so</td><td>1008.00 KB</td><td>6.18</td><td>Low</td><td>1008.00 KB</td><td>357.80 KB</td><td>357.30 KB</td></tr>
    <tr><td>mozilla/components/libmsgmdn.so</td><td>64.00 KB</td><td>5.32</td><td>Moderate</td><td>64.00 KB</td><td>21.27 KB</td><td>22.27 KB</td></tr>
    <tr><td>mozilla/components/libmsgnews.so</td><td>544.00 KB</td><td>5.98</td><td>Moderate</td><td>544.00 KB</td><td>179.06 KB</td><td>179.94 KB</td></tr>
    <tr><td>mozilla/components/libmsgsmime.so</td><td>64.00 KB</td><td>5.33</td><td>Moderate</td><td>64.00 KB</td><td>20.72 KB</td><td>21.81 KB</td></tr>
    <tr><td>mozilla/components/libnecko.so</td><td>1.43 MB</td><td>5.96</td><td>Moderate</td><td>1.43 MB</td><td>467.56 KB</td><td>461.22 KB</td></tr>
    <tr><td>mozilla/components/libnecko2.so</td><td>288.00 KB</td><td>5.68</td><td>Moderate</td><td>288.00 KB</td><td>85.57 KB</td><td>84.98 KB</td></tr>
    <tr><td>mozilla/components/libnkcache.so</td><td>176.00 KB</td><td>5.75</td><td>Moderate</td><td>176.00 KB</td><td>59.51 KB</td><td>63.56 KB</td></tr>
    <tr><td>mozilla/components/libnsappshell.so</td><td>328.00 KB</td><td>5.88</td><td>Moderate</td><td>328.00 KB</td><td>104.12 KB</td><td>105.58 KB</td></tr>
    <tr><td>mozilla/components/libnslocale.so</td><td>136.00 KB</td><td>5.60</td><td>Moderate</td><td>136.00 KB</td><td>43.61 KB</td><td>45.84 KB</td></tr>
    <tr><td>mozilla/components/libnsprefm.so</td><td>120.00 KB</td><td>5.88</td><td>Moderate</td><td>120.00 KB</td><td>40.08 KB</td><td>39.42 KB</td></tr>
    <tr><td>mozilla/components/liboji.so</td><td>136.00 KB</td><td>5.76</td><td>Moderate</td><td>136.00 KB</td><td>42.22 KB</td><td>43.27 KB</td></tr>
    <tr><td>mozilla/components/libpipboot.so</td><td>64.00 KB</td><td>5.57</td><td>Moderate</td><td>64.00 KB</td><td>21.91 KB</td><td>22.96 KB</td></tr>
    <tr><td>mozilla/components/libpipnss.so</td><td>616.00 KB</td><td>5.97</td><td>Moderate</td><td>616.00 KB</td><td>198.94 KB</td><td>200.96 KB</td></tr>
    <tr><td>mozilla/components/libpippki.so</td><td>104.00 KB</td><td>5.41</td><td>Moderate</td><td>104.00 KB</td><td>31.37 KB</td><td>32.77 KB</td></tr>
    <tr><td>mozilla/components/libpref.so</td><td>168.00 KB</td><td>5.52</td><td>Moderate</td><td>168.00 KB</td><td>50.09 KB</td><td>52.30 KB</td></tr>
    <tr><td>mozilla/components/libprofile.so</td><td>160.00 KB</td><td>5.99</td><td>Moderate</td><td>160.00 KB</td><td>59.91 KB</td><td>62.48 KB</td></tr>
    <tr><td>mozilla/components/librdf.so</td><td>384.00 KB</td><td>5.88</td><td>Moderate</td><td>384.00 KB</td><td>122.65 KB</td><td>124.19 KB</td></tr>
    <tr><td>mozilla/components/libregviewer.so</td><td>48.00 KB</td><td>5.40</td><td>Moderate</td><td>48.00 KB</td><td>16.17 KB</td><td>17.15 KB</td></tr>
    <tr><td>mozilla/components/libshistory.so</td><td>72.00 KB</td><td>5.10</td><td>Moderate</td><td>72.00 KB</td><td>22.16 KB</td><td>23.40 KB</td></tr>
    <tr><td>mozilla/components/libstrres.so</td><td>56.00 KB</td><td>5.40</td><td>Moderate</td><td>56.00 KB</td><td>18.76 KB</td><td>19.44 KB</td></tr>
    <tr><td>mozilla/components/libtransformiix.so</td><td>680.00 KB</td><td>6.00</td><td>Moderate</td><td>680.00 KB</td><td>213.63 KB</td><td>213.10 KB</td></tr>
    <tr><td>mozilla/components/libtxmgr.so</td><td>56.00 KB</td><td>5.08</td><td>Moderate</td><td>56.00 KB</td><td>15.79 KB</td><td>16.77 KB</td></tr>
    <tr><td>mozilla/components/libtxtsvc.so</td><td>120.00 KB</td><td>5.89</td><td>Moderate</td><td>120.00 KB</td><td>42.85 KB</td><td>44.00 KB</td></tr>
    <tr><td>mozilla/components/libuconv.so</td><td>184.00 KB</td><td>5.63</td><td>Moderate</td><td>184.00 KB</td><td>58.43 KB</td><td>60.75 KB</td></tr>
    <tr><td>mozilla/components/libucvcn.so</td><td>144.00 KB</td><td>6.57</td><td>Low</td><td>144.00 KB</td><td>77.36 KB</td><td>81.42 KB</td></tr>
    <tr><td>mozilla/components/libucvibm.so</td><td>72.00 KB</td><td>5.10</td><td>Moderate</td><td>72.00 KB</td><td>20.06 KB</td><td>19.97 KB</td></tr>
    <tr><td>mozilla/components/libucvja.so</td><td>224.00 KB</td><td>5.72</td><td>Moderate</td><td>224.00 KB</td><td>104.24 KB</td><td>111.89 KB</td></tr>
    <tr><td>mozilla/components/libucvko.so</td><td>160.00 KB</td><td>6.42</td><td>Low</td><td>160.00 KB</td><td>87.62 KB</td><td>92.25 KB</td></tr>
    <tr><td>mozilla/components/libucvlatin.so</td><td>336.00 KB</td><td>5.39</td><td>Moderate</td><td>336.00 KB</td><td>76.56 KB</td><td>73.52 KB</td></tr>
    <tr><td>mozilla/components/libucvmath.so</td><td>80.00 KB</td><td>5.12</td><td>Moderate</td><td>80.00 KB</td><td>22.35 KB</td><td>22.36 KB</td></tr>
    <tr><td>mozilla/components/libucvtw.so</td><td>160.00 KB</td><td>6.47</td><td>Low</td><td>160.00 KB</td><td>93.32 KB</td><td>99.52 KB</td></tr>
    <tr><td>mozilla/components/libucvtw2.so</td><td>216.00 KB</td><td>5.77</td><td>Moderate</td><td>216.00 KB</td><td>108.70 KB</td><td>115.22 KB</td></tr>
    <tr><td>mozilla/components/libunicharutil.so</td><td>72.00 KB</td><td>4.98</td><td>High</td><td>72.00 KB</td><td>22.90 KB</td><td>23.91 KB</td></tr>
    <tr><td>mozilla/components/libuniversalchardet.so</td><td>144.00 KB</td><td>5.71</td><td>Moderate</td><td>144.00 KB</td><td>66.13 KB</td><td>69.28 KB</td></tr>
    <tr><td>mozilla/components/liburiloader.so</td><td>240.00 KB</td><td>5.83</td><td>Moderate</td><td>240.00 KB</td><td>81.33 KB</td><td>83.87 KB</td></tr>
    <tr><td>mozilla/components/libvcard.so</td><td>88.00 KB</td><td>5.50</td><td>Moderate</td><td>88.00 KB</td><td>33.13 KB</td><td>35.49 KB</td></tr>
    <tr><td>mozilla/components/libwallet.so</td><td>240.00 KB</td><td>5.88</td><td>Moderate</td><td>240.00 KB</td><td>86.03 KB</td><td>89.57 KB</td></tr>
    <tr><td>mozilla/components/libwalletviewers.so</td><td>32.00 KB</td><td>4.53</td><td>High</td><td>32.00 KB</td><td>8.08 KB</td><td>8.22 KB</td></tr>
    <tr><td>mozilla/components/libwebbrwsr.so</td><td>184.00 KB</td><td>5.63</td><td>Moderate</td><td>184.00 KB</td><td>54.75 KB</td><td>55.89 KB</td></tr>
    <tr><td>mozilla/components/libwidget_gtk.so</td><td>440.00 KB</td><td>5.75</td><td>Moderate</td><td>440.00 KB</td><td>146.00 KB</td><td>147.61 KB</td></tr>
    <tr><td>mozilla/components/libxmlextras.so</td><td>848.00 KB</td><td>6.07</td><td>Low</td><td>848.00 KB</td><td>247.03 KB</td><td>237.13 KB</td></tr>
    <tr><td>mozilla/components/libxpconnect.so</td><td>536.00 KB</td><td>5.85</td><td>Moderate</td><td>536.00 KB</td><td>167.62 KB</td><td>166.98 KB</td></tr>
    <tr><td>mozilla/components/libxpinstall.so</td><td>408.00 KB</td><td>5.92</td><td>Moderate</td><td>408.00 KB</td><td>137.23 KB</td><td>139.09 KB</td></tr>
    <tr><td>mozilla/components/libxremote_client.so</td><td>32.00 KB</td><td>3.38</td><td>Very High</td><td>32.00 KB</td><td>7.95 KB</td><td>8.21 KB</td></tr>
    <tr><td>mozilla/components/libxremoteservice.so</td><td>64.00 KB</td><td>5.06</td><td>Moderate</td><td>64.00 KB</td><td>19.65 KB</td><td>20.50 KB</td></tr>
    <tr><td>mozilla/components/locale.xpt</td><td>1.61 KB</td><td>4.96</td><td>High</td><td>1.61 KB</td><td>758 B</td><td>789 B</td></tr>
    <tr><td>mozilla/components/mailnews.xpt</td><td>200 B</td><td>3.95</td><td>Very High</td><td>200 B</td><td>158 B</td><td>164 B</td></tr>
    <tr><td>mozilla/components/mdn-service.js</td><td>3.64 KB</td><td>5.09</td><td>Moderate</td><td>3.64 KB</td><td>1.34 KB</td><td>1.41 KB</td></tr>
    <tr><td>mozilla/components/mime.xpt</td><td>2.94 KB</td><td>4.92</td><td>High</td><td>2.94 KB</td><td>1.32 KB</td><td>1.39 KB</td></tr>
    <tr><td>mozilla/components/mimetype.xpt</td><td>1.51 KB</td><td>4.79</td><td>High</td><td>1.51 KB</td><td>710 B</td><td>752 B</td></tr>
    <tr><td>mozilla/components/mozbrwsr.xpt</td><td>536 B</td><td>4.18</td><td>High</td><td>536 B</td><td>329 B</td><td>334 B</td></tr>
    <tr><td>mozilla/components/mozfind.xpt</td><td>1.14 KB</td><td>4.80</td><td>High</td><td>1.14 KB</td><td>558 B</td><td>580 B</td></tr>
    <tr><td>mozilla/components/mozldap.xpt</td><td>3.68 KB</td><td>5.33</td><td>Moderate</td><td>3.68 KB</td><td>1.99 KB</td><td>2.10 KB</td></tr>
    <tr><td>mozilla/components/mozxfer.xpt</td><td>1020 B</td><td>4.36</td><td>High</td><td>1020 B</td><td>532 B</td><td>546 B</td></tr>
    <tr><td>mozilla/components/msgbase.xpt</td><td>30.01 KB</td><td>5.25</td><td>Moderate</td><td>30.01 KB</td><td>11.56 KB</td><td>12.13 KB</td></tr>
    <tr><td>mozilla/components/msgcompose.xpt</td><td>11.97 KB</td><td>5.23</td><td>Moderate</td><td>11.97 KB</td><td>5.00 KB</td><td>5.22 KB</td></tr>
    <tr><td>mozilla/components/msgdb.xpt</td><td>6.65 KB</td><td>5.23</td><td>Moderate</td><td>6.65 KB</td><td>2.61 KB</td><td>2.73 KB</td></tr>
    <tr><td>mozilla/components/msgimap.xpt</td><td>14.17 KB</td><td>5.32</td><td>Moderate</td><td>14.17 KB</td><td>5.48 KB</td><td>5.79 KB</td></tr>
    <tr><td>mozilla/components/msglocal.xpt</td><td>4.05 KB</td><td>5.04</td><td>Moderate</td><td>4.05 KB</td><td>1.73 KB</td><td>1.81 KB</td></tr>
    <tr><td>mozilla/components/msgnews.xpt</td><td>5.58 KB</td><td>5.15</td><td>Moderate</td><td>5.58 KB</td><td>2.34 KB</td><td>2.42 KB</td></tr>
    <tr><td>mozilla/components/msgsearch.xpt</td><td>7.87 KB</td><td>5.08</td><td>Moderate</td><td>7.87 KB</td><td>3.48 KB</td><td>3.58 KB</td></tr>
    <tr><td>mozilla/components/msgsmime.xpt</td><td>658 B</td><td>4.49</td><td>High</td><td>658 B</td><td>386 B</td><td>395 B</td></tr>
    <tr><td>mozilla/components/necko.xpt</td><td>13.63 KB</td><td>5.42</td><td>Moderate</td><td>13.63 KB</td><td>6.02 KB</td><td>6.32 KB</td></tr>
    <tr><td>mozilla/components/necko_about.xpt</td><td>441 B</td><td>3.25</td><td>Very High</td><td>441 B</td><td>227 B</td><td>237 B</td></tr>
    <tr><td>mozilla/components/necko_cache.xpt</td><td>2.20 KB</td><td>5.03</td><td>Moderate</td><td>2.20 KB</td><td>1.14 KB</td><td>1.17 KB</td></tr>
    <tr><td>mozilla/components/necko_cookie.xpt</td><td>493 B</td><td>4.29</td><td>High</td><td>493 B</td><td>272 B</td><td>282 B</td></tr>
    <tr><td>mozilla/components/necko_data.xpt</td><td>146 B</td><td>4.17</td><td>High</td><td>146 B</td><td>118 B</td><td>125 B</td></tr>
    <tr><td>mozilla/components/necko_dns.xpt</td><td>584 B</td><td>4.46</td><td>High</td><td>584 B</td><td>371 B</td><td>384 B</td></tr>
    <tr><td>mozilla/components/necko_file.xpt</td><td>284 B</td><td>3.71</td><td>Very High</td><td>284 B</td><td>169 B</td><td>177 B</td></tr>
    <tr><td>mozilla/components/necko_ftp.xpt</td><td>347 B</td><td>3.99</td><td>Very High</td><td>347 B</td><td>223 B</td><td>236 B</td></tr>
    <tr><td>mozilla/components/necko_http.xpt</td><td>2.14 KB</td><td>4.97</td><td>High</td><td>2.14 KB</td><td>1.04 KB</td><td>1.07 KB</td></tr>
    <tr><td>mozilla/components/necko_jar.xpt</td><td>625 B</td><td>4.38</td><td>High</td><td>625 B</td><td>348 B</td><td>372 B</td></tr>
    <tr><td>mozilla/components/necko_res.xpt</td><td>321 B</td><td>4.36</td><td>High</td><td>321 B</td><td>204 B</td><td>215 B</td></tr>
    <tr><td>mozilla/components/necko_socket.xpt</td><td>1.42 KB</td><td>4.95</td><td>High</td><td>1.42 KB</td><td>688 B</td><td>708 B</td></tr>
    <tr><td>mozilla/components/necko_strconv.xpt</td><td>1.47 KB</td><td>4.99</td><td>High</td><td>1.47 KB</td><td>827 B</td><td>858 B</td></tr>
    <tr><td>mozilla/components/necko_viewsource.xpt</td><td>182 B</td><td>4.48</td><td>High</td><td>182 B</td><td>151 B</td><td>163 B</td></tr>
    <tr><td>mozilla/components/nsDictionary.js</td><td>3.79 KB</td><td>4.94</td><td>High</td><td>3.79 KB</td><td>1.39 KB</td><td>1.47 KB</td></tr>
    <tr><td>mozilla/components/nsDownloadProgressListener.js</td><td>10.66 KB</td><td>4.76</td><td>High</td><td>10.66 KB</td><td>3.34 KB</td><td>3.55 KB</td></tr>
    <tr><td>mozilla/components/nsFilePicker.js</td><td>9.23 KB</td><td>4.96</td><td>High</td><td>9.23 KB</td><td>2.94 KB</td><td>3.08 KB</td></tr>
    <tr><td>mozilla/components/nsHelperAppDlg.js</td><td>24.06 KB</td><td>4.53</td><td>High</td><td>24.06 KB</td><td>6.79 KB</td><td>7.35 KB</td></tr>
    <tr><td>mozilla/components/nsKillAll.js</td><td>7.69 KB</td><td>4.74</td><td>High</td><td>7.69 KB</td><td>2.52 KB</td><td>2.65 KB</td></tr>
    <tr><td>mozilla/components/nsLDAPPrefsService.js</td><td>9.73 KB</td><td>4.85</td><td>High</td><td>9.73 KB</td><td>2.80 KB</td><td>3.04 KB</td></tr>
    <tr><td>mozilla/components/nsProgressDialog.js</td><td>30.86 KB</td><td>4.39</td><td>High</td><td>30.86 KB</td><td>8.25 KB</td><td>8.85 KB</td></tr>
    <tr><td>mozilla/components/nsProxyAutoConfig.js</td><td>15.78 KB</td><td>4.87</td><td>High</td><td>15.78 KB</td><td>4.67 KB</td><td>5.04 KB</td></tr>
    <tr><td>mozilla/components/nsResetPref.js</td><td>7.01 KB</td><td>4.65</td><td>High</td><td>7.01 KB</td><td>2.37 KB</td><td>2.50 KB</td></tr>
    <tr><td>mozilla/components/nsSidebar.js</td><td>17.43 KB</td><td>4.85</td><td>High</td><td>17.43 KB</td><td>4.46 KB</td><td>4.82 KB</td></tr>
    <tr><td>mozilla/components/nsUpdateNotifier.js</td><td>15.76 KB</td><td>4.94</td><td>High</td><td>15.76 KB</td><td>4.62 KB</td><td>4.92 KB</td></tr>
    <tr><td>mozilla/components/nsXmlRpcClient.js</td><td>47.99 KB</td><td>4.45</td><td>High</td><td>47.99 KB</td><td>10.63 KB</td><td>11.69 KB</td></tr>
    <tr><td>mozilla/components/oji.xpt</td><td>591 B</td><td>4.17</td><td>High</td><td>591 B</td><td>356 B</td><td>356 B</td></tr>
    <tr><td>mozilla/components/pipboot.xpt</td><td>684 B</td><td>4.61</td><td>High</td><td>684 B</td><td>408 B</td><td>413 B</td></tr>
    <tr><td>mozilla/components/pipnss.xpt</td><td>9.53 KB</td><td>5.50</td><td>Moderate</td><td>9.53 KB</td><td>4.47 KB</td><td>4.72 KB</td></tr>
    <tr><td>mozilla/components/pippki.xpt</td><td>437 B</td><td>4.61</td><td>High</td><td>437 B</td><td>289 B</td><td>319 B</td></tr>
    <tr><td>mozilla/components/plugin.xpt</td><td>626 B</td><td>4.53</td><td>High</td><td>626 B</td><td>393 B</td><td>399 B</td></tr>
    <tr><td>mozilla/components/pref.xpt</td><td>3.26 KB</td><td>4.98</td><td>High</td><td>3.26 KB</td><td>1.33 KB</td><td>1.35 KB</td></tr>
    <tr><td>mozilla/components/prefmigr.xpt</td><td>553 B</td><td>4.60</td><td>High</td><td>553 B</td><td>358 B</td><td>366 B</td></tr>
    <tr><td>mozilla/components/profile.xpt</td><td>1.63 KB</td><td>4.94</td><td>High</td><td>1.63 KB</td><td>837 B</td><td>877 B</td></tr>
    <tr><td>mozilla/components/progressDlg.xpt</td><td>258 B</td><td>4.03</td><td>High</td><td>258 B</td><td>180 B</td><td>191 B</td></tr>
    <tr><td>mozilla/components/proxyObjInst.xpt</td><td>436 B</td><td>4.39</td><td>High</td><td>436 B</td><td>271 B</td><td>268 B</td></tr>
    <tr><td>mozilla/components/rdf.xpt</td><td>4.63 KB</td><td>5.17</td><td>Moderate</td><td>4.63 KB</td><td>2.04 KB</td><td>2.13 KB</td></tr>
    <tr><td>mozilla/components/regviewer.xpt</td><td>317 B</td><td>3.94</td><td>Very High</td><td>317 B</td><td>209 B</td><td>206 B</td></tr>
    <tr><td>mozilla/components/related.xpt</td><td>183 B</td><td>3.87</td><td>Very High</td><td>183 B</td><td>145 B</td><td>143 B</td></tr>
    <tr><td>mozilla/components/search.xpt</td><td>1.11 KB</td><td>5.00</td><td>Moderate</td><td>1.11 KB</td><td>639 B</td><td>654 B</td></tr>
    <tr><td>mozilla/components/shistory.xpt</td><td>2.34 KB</td><td>4.81</td><td>High</td><td>2.34 KB</td><td>1.06 KB</td><td>1.09 KB</td></tr>
    <tr><td>mozilla/components/sidebar.xpt</td><td>314 B</td><td>3.99</td><td>Very High</td><td>314 B</td><td>206 B</td><td>208 B</td></tr>
    <tr><td>mozilla/components/signonviewer.xpt</td><td>288 B</td><td>3.96</td><td>Very High</td><td>288 B</td><td>192 B</td><td>201 B</td></tr>
    <tr><td>mozilla/components/smime-service.js</td><td>3.70 KB</td><td>5.09</td><td>Moderate</td><td>3.70 KB</td><td>1.35 KB</td><td>1.42 KB</td></tr>
    <tr><td>mozilla/components/timebomb.xpt</td><td>416 B</td><td>4.58</td><td>High</td><td>416 B</td><td>278 B</td><td>278 B</td></tr>
    <tr><td>mozilla/components/transformiix.xpt</td><td>333 B</td><td>3.17</td><td>Very High</td><td>333 B</td><td>167 B</td><td>177 B</td></tr>
    <tr><td>mozilla/components/txmgr.xpt</td><td>1.29 KB</td><td>4.80</td><td>High</td><td>1.29 KB</td><td>592 B</td><td>638 B</td></tr>
    <tr><td>mozilla/components/txtsvc.xpt</td><td>492 B</td><td>4.41</td><td>High</td><td>492 B</td><td>289 B</td><td>289 B</td></tr>
    <tr><td>mozilla/components/uconv.xpt</td><td>1.08 KB</td><td>4.90</td><td>High</td><td>1.08 KB</td><td>574 B</td><td>608 B</td></tr>
    <tr><td>mozilla/components/unicharutil.xpt</td><td>818 B</td><td>4.80</td><td>High</td><td>818 B</td><td>473 B</td><td>488 B</td></tr>
    <tr><td>mozilla/components/uriloader.xpt</td><td>3.20 KB</td><td>4.80</td><td>High</td><td>3.20 KB</td><td>1.36 KB</td><td>1.43 KB</td></tr>
    <tr><td>mozilla/components/urlbarhistory.xpt</td><td>207 B</td><td>3.90</td><td>Very High</td><td>207 B</td><td>150 B</td><td>152 B</td></tr>
    <tr><td>mozilla/components/util.xpt</td><td>303 B</td><td>4.16</td><td>High</td><td>303 B</td><td>190 B</td><td>206 B</td></tr>
    <tr><td>mozilla/components/venkman-service.js</td><td>4.57 KB</td><td>5.02</td><td>Moderate</td><td>4.57 KB</td><td>1.69 KB</td><td>1.77 KB</td></tr>
    <tr><td>mozilla/components/wallet.xpt</td><td>1.32 KB</td><td>4.84</td><td>High</td><td>1.32 KB</td><td>694 B</td><td>733 B</td></tr>
    <tr><td>mozilla/components/walleteditor.xpt</td><td>251 B</td><td>3.71</td><td>Very High</td><td>251 B</td><td>169 B</td><td>178 B</td></tr>
    <tr><td>mozilla/components/walletpreview.xpt</td><td>259 B</td><td>3.81</td><td>Very High</td><td>259 B</td><td>176 B</td><td>186 B</td></tr>
    <tr><td>mozilla/components/webBrowser_core.xpt</td><td>4.50 KB</td><td>5.16</td><td>Moderate</td><td>4.50 KB</td><td>2.03 KB</td><td>2.09 KB</td></tr>
    <tr><td>mozilla/components/webbrowserpersist.xpt</td><td>1.60 KB</td><td>4.69</td><td>High</td><td>1.60 KB</td><td>722 B</td><td>761 B</td></tr>
    <tr><td>mozilla/components/webshell_idls.xpt</td><td>1.32 KB</td><td>4.23</td><td>High</td><td>1.32 KB</td><td>601 B</td><td>619 B</td></tr>
    <tr><td>mozilla/components/widget.xpt</td><td>5.15 KB</td><td>4.97</td><td>High</td><td>5.15 KB</td><td>2.36 KB</td><td>2.41 KB</td></tr>
    <tr><td>mozilla/components/windowwatcher.xpt</td><td>2.05 KB</td><td>4.91</td><td>High</td><td>2.05 KB</td><td>981 B</td><td>1010 B</td></tr>
    <tr><td>mozilla/components/xml-rpc.xpt</td><td>1.04 KB</td><td>4.82</td><td>High</td><td>1.04 KB</td><td>640 B</td><td>654 B</td></tr>
    <tr><td>mozilla/components/xmlextras.xpt</td><td>1.47 KB</td><td>4.28</td><td>High</td><td>1.47 KB</td><td>691 B</td><td>716 B</td></tr>
    <tr><td>mozilla/components/xmlschema.xpt</td><td>6.05 KB</td><td>5.41</td><td>Moderate</td><td>6.05 KB</td><td>2.36 KB</td><td>2.39 KB</td></tr>
    <tr><td>mozilla/components/xmlsoap.xpt</td><td>3.79 KB</td><td>4.98</td><td>High</td><td>3.79 KB</td><td>1.65 KB</td><td>1.74 KB</td></tr>
    <tr><td>mozilla/components/xpcom_base.xpt</td><td>2.34 KB</td><td>5.16</td><td>Moderate</td><td>2.34 KB</td><td>1.19 KB</td><td>1.23 KB</td></tr>
    <tr><td>mozilla/components/xpcom_components.xpt</td><td>5.21 KB</td><td>5.22</td><td>Moderate</td><td>5.21 KB</td><td>2.27 KB</td><td>2.37 KB</td></tr>
    <tr><td>mozilla/components/xpcom_ds.xpt</td><td>8.23 KB</td><td>5.39</td><td>Moderate</td><td>8.23 KB</td><td>3.39 KB</td><td>3.42 KB</td></tr>
    <tr><td>mozilla/components/xpcom_io.xpt</td><td>8.11 KB</td><td>5.29</td><td>Moderate</td><td>8.11 KB</td><td>3.55 KB</td><td>3.69 KB</td></tr>
    <tr><td>mozilla/components/xpcom_threads.xpt</td><td>2.58 KB</td><td>5.21</td><td>Moderate</td><td>2.58 KB</td><td>1.34 KB</td><td>1.38 KB</td></tr>
    <tr><td>mozilla/components/xpcom_xpti.xpt</td><td>1.64 KB</td><td>5.08</td><td>Moderate</td><td>1.64 KB</td><td>866 B</td><td>902 B</td></tr>
    <tr><td>mozilla/components/xpconnect.xpt</td><td>6.94 KB</td><td>5.47</td><td>Moderate</td><td>6.94 KB</td><td>3.08 KB</td><td>3.19 KB</td></tr>
    <tr><td>mozilla/components/xpinstall.xpt</td><td>1.05 KB</td><td>4.76</td><td>High</td><td>1.05 KB</td><td>645 B</td><td>657 B</td></tr>
    <tr><td>mozilla/components/xremoteservice.xpt</td><td>388 B</td><td>3.91</td><td>Very High</td><td>388 B</td><td>245 B</td><td>240 B</td></tr>
    <tr><td>mozilla/components/xuldoc.xpt</td><td>1.16 KB</td><td>4.57</td><td>High</td><td>1.16 KB</td><td>536 B</td><td>552 B</td></tr>
    <tr><td>mozilla/components/xultmpl.xpt</td><td>1.29 KB</td><td>4.67</td><td>High</td><td>1.29 KB</td><td>705 B</td><td>735 B</td></tr>
    <tr><td>mozilla/defaults/autoconfig/prefcalls.js</td><td>6.49 KB</td><td>4.69</td><td>High</td><td>6.49 KB</td><td>1.86 KB</td><td>2.00 KB</td></tr>
    <tr><td>mozilla/defaults/pref/all.js</td><td>31.76 KB</td><td>4.97</td><td>High</td><td>31.76 KB</td><td>8.78 KB</td><td>9.24 KB</td></tr>
    <tr><td>mozilla/defaults/pref/config.js</td><td>2.05 KB</td><td>4.85</td><td>High</td><td>2.05 KB</td><td>993 B</td><td>1.01 KB</td></tr>
    <tr><td>mozilla/defaults/pref/editor.js</td><td>3.51 KB</td><td>4.77</td><td>High</td><td>3.51 KB</td><td>1.32 KB</td><td>1.38 KB</td></tr>
    <tr><td>mozilla/defaults/pref/initpref.js</td><td>2.61 KB</td><td>4.90</td><td>High</td><td>2.61 KB</td><td>1.15 KB</td><td>1.20 KB</td></tr>
    <tr><td>mozilla/defaults/pref/inspector.js</td><td>1.99 KB</td><td>4.84</td><td>High</td><td>1.99 KB</td><td>903 B</td><td>946 B</td></tr>
    <tr><td>mozilla/defaults/pref/mailnews.js</td><td>20.25 KB</td><td>4.91</td><td>High</td><td>20.25 KB</td><td>5.59 KB</td><td>5.91 KB</td></tr>
    <tr><td>mozilla/defaults/pref/mdn.js</td><td>806 B</td><td>4.75</td><td>High</td><td>806 B</td><td>337 B</td><td>356 B</td></tr>
    <tr><td>mozilla/defaults/pref/security-prefs.js</td><td>1.34 KB</td><td>4.71</td><td>High</td><td>1.34 KB</td><td>359 B</td><td>374 B</td></tr>
    <tr><td>mozilla/defaults/pref/smime.js</td><td>267 B</td><td>4.62</td><td>High</td><td>267 B</td><td>151 B</td><td>141 B</td></tr>
    <tr><td>mozilla/defaults/pref/unix.js</td><td>14.06 KB</td><td>5.11</td><td>Moderate</td><td>14.06 KB</td><td>3.90 KB</td><td>4.04 KB</td></tr>
    <tr><td>mozilla/defaults/pref/xpinstall.js</td><td>219 B</td><td>4.52</td><td>High</td><td>219 B</td><td>122 B</td><td>115 B</td></tr>
    <tr><td>mozilla/defaults/profile/US/bookmarks.html</td><td>5.65 KB</td><td>5.44</td><td>Moderate</td><td>5.65 KB</td><td>1.54 KB</td><td>1.65 KB</td></tr>
    <tr><td>mozilla/defaults/profile/US/chrome/userChrome-example.css</td><td>1.05 KB</td><td>4.73</td><td>High</td><td>1.05 KB</td><td>582 B</td><td>588 B</td></tr>
    <tr><td>mozilla/defaults/profile/US/chrome/userContent-example.css</td><td>574 B</td><td>4.59</td><td>High</td><td>574 B</td><td>343 B</td><td>348 B</td></tr>
    <tr><td>mozilla/defaults/profile/US/localstore.rdf</td><td>5.98 KB</td><td>4.91</td><td>High</td><td>5.98 KB</td><td>677 B</td><td>766 B</td></tr>
    <tr><td>mozilla/defaults/profile/US/mimeTypes.rdf</td><td>287 B</td><td>5.18</td><td>Moderate</td><td>287 B</td><td>208 B</td><td>204 B</td></tr>
    <tr><td>mozilla/defaults/profile/US/panels.rdf</td><td>1.57 KB</td><td>5.00</td><td>Moderate</td><td>1.57 KB</td><td>498 B</td><td>518 B</td></tr>
    <tr><td>mozilla/defaults/profile/US/search.rdf</td><td>2.28 KB</td><td>5.15</td><td>Moderate</td><td>2.28 KB</td><td>917 B</td><td>949 B</td></tr>
    <tr><td>mozilla/defaults/profile/bookmarks.html</td><td>5.65 KB</td><td>5.44</td><td>Moderate</td><td>5.65 KB</td><td>1.54 KB</td><td>1.65 KB</td></tr>
    <tr><td>mozilla/defaults/profile/chrome/userChrome-example.css</td><td>1.05 KB</td><td>4.73</td><td>High</td><td>1.05 KB</td><td>582 B</td><td>588 B</td></tr>
    <tr><td>mozilla/defaults/profile/chrome/userContent-example.css</td><td>574 B</td><td>4.59</td><td>High</td><td>574 B</td><td>343 B</td><td>348 B</td></tr>
    <tr><td>mozilla/defaults/profile/localstore.rdf</td><td>5.98 KB</td><td>4.91</td><td>High</td><td>5.98 KB</td><td>677 B</td><td>766 B</td></tr>
    <tr><td>mozilla/defaults/profile/mimeTypes.rdf</td><td>287 B</td><td>5.18</td><td>Moderate</td><td>287 B</td><td>208 B</td><td>204 B</td></tr>
    <tr><td>mozilla/defaults/profile/panels.rdf</td><td>1.57 KB</td><td>5.00</td><td>Moderate</td><td>1.57 KB</td><td>498 B</td><td>518 B</td></tr>
    <tr><td>mozilla/defaults/profile/search.rdf</td><td>2.28 KB</td><td>5.15</td><td>Moderate</td><td>2.28 KB</td><td>917 B</td><td>949 B</td></tr>
    <tr><td>mozilla/defaults/wallet/DistinguishedSchema.tbl</td><td>201 B</td><td>4.32</td><td>High</td><td>201 B</td><td>105 B</td><td>108 B</td></tr>
    <tr><td>mozilla/defaults/wallet/FieldSchema.tbl</td><td>26.96 KB</td><td>4.70</td><td>High</td><td>26.96 KB</td><td>3.70 KB</td><td>4.11 KB</td></tr>
    <tr><td>mozilla/defaults/wallet/PositionalSchema.tbl</td><td>1.54 KB</td><td>4.68</td><td>High</td><td>1.54 KB</td><td>433 B</td><td>475 B</td></tr>
    <tr><td>mozilla/defaults/wallet/SchemaConcat.tbl</td><td>19.20 KB</td><td>4.63</td><td>High</td><td>19.20 KB</td><td>1.95 KB</td><td>2.36 KB</td></tr>
    <tr><td>mozilla/defaults/wallet/SchemaStrings.tbl</td><td>1.11 KB</td><td>4.35</td><td>High</td><td>1.11 KB</td><td>463 B</td><td>479 B</td></tr>
    <tr><td>mozilla/defaults/wallet/StateSchema.tbl</td><td>3.89 KB</td><td>4.71</td><td>High</td><td>3.89 KB</td><td>792 B</td><td>816 B</td></tr>
    <tr><td>mozilla/defaults/wallet/VcardSchema.tbl</td><td>835 B</td><td>4.53</td><td>High</td><td>835 B</td><td>311 B</td><td>321 B</td></tr>
    <tr><td>mozilla/dirver</td><td>24.00 KB</td><td>1.96</td><td>Excellent</td><td>24.00 KB</td><td>3.48 KB</td><td>3.55 KB</td></tr>
    <tr><td>mozilla/icons/mozicon16.xpm</td><td>1.63 KB</td><td>4.68</td><td>High</td><td>1.63 KB</td><td>728 B</td><td>684 B</td></tr>
    <tr><td>mozilla/icons/mozicon50.xpm</td><td>2.88 KB</td><td>2.50</td><td>Very High</td><td>2.88 KB</td><td>618 B</td><td>623 B</td></tr>
    <tr><td>mozilla/libgkgfx.so</td><td>248.00 KB</td><td>5.85</td><td>Moderate</td><td>248.00 KB</td><td>89.99 KB</td><td>93.76 KB</td></tr>
    <tr><td>mozilla/libgtkembedmoz.so</td><td>160.00 KB</td><td>5.65</td><td>Moderate</td><td>160.00 KB</td><td>49.39 KB</td><td>51.88 KB</td></tr>
    <tr><td>mozilla/libgtksuperwin.so</td><td>32.00 KB</td><td>4.78</td><td>High</td><td>32.00 KB</td><td>10.60 KB</td><td>11.08 KB</td></tr>
    <tr><td>mozilla/libgtkxtbin.so</td><td>32.00 KB</td><td>3.51</td><td>Very High</td><td>32.00 KB</td><td>7.82 KB</td><td>8.04 KB</td></tr>
    <tr><td>mozilla/libjsj.so</td><td>144.00 KB</td><td>5.96</td><td>Moderate</td><td>144.00 KB</td><td>53.70 KB</td><td>57.44 KB</td></tr>
    <tr><td>mozilla/libldap50.so</td><td>224.00 KB</td><td>6.02</td><td>Low</td><td>224.00 KB</td><td>99.04 KB</td><td>103.45 KB</td></tr>
    <tr><td>mozilla/libmoz_art_lgpl.so</td><td>104.00 KB</td><td>6.24</td><td>Low</td><td>104.00 KB</td><td>57.81 KB</td><td>61.60 KB</td></tr>
    <tr><td>mozilla/libmozjs.so</td><td>656.00 KB</td><td>6.27</td><td>Low</td><td>656.00 KB</td><td>307.94 KB</td><td>321.17 KB</td></tr>
    <tr><td>mozilla/libmozz.so</td><td>80.00 KB</td><td>5.84</td><td>Moderate</td><td>80.00 KB</td><td>40.41 KB</td><td>42.65 KB</td></tr>
    <tr><td>mozilla/libmsgbaseutil.so</td><td>472.00 KB</td><td>5.85</td><td>Moderate</td><td>472.00 KB</td><td>150.98 KB</td><td>151.61 KB</td></tr>
    <tr><td>mozilla/libnspr4.so</td><td>256.00 KB</td><td>6.14</td><td>Low</td><td>256.00 KB</td><td>113.06 KB</td><td>120.73 KB</td></tr>
    <tr><td>mozilla/libnss3.so</td><td>584.00 KB</td><td>6.05</td><td>Low</td><td>584.00 KB</td><td>238.40 KB</td><td>252.05 KB</td></tr>
    <tr><td>mozilla/libnssckbi.so</td><td>368.00 KB</td><td>5.74</td><td>Moderate</td><td>368.00 KB</td><td>105.51 KB</td><td>106.83 KB</td></tr>
    <tr><td>mozilla/libnullplugin.so</td><td>40.00 KB</td><td>4.12</td><td>High</td><td>40.00 KB</td><td>11.07 KB</td><td>11.61 KB</td></tr>
    <tr><td>mozilla/libplc4.so</td><td>32.00 KB</td><td>4.32</td><td>High</td><td>32.00 KB</td><td>9.94 KB</td><td>10.29 KB</td></tr>
    <tr><td>mozilla/libplds4.so</td><td>24.00 KB</td><td>3.00</td><td>Very High</td><td>24.00 KB</td><td>5.14 KB</td><td>5.32 KB</td></tr>
    <tr><td>mozilla/libprldap50.so</td><td>32.00 KB</td><td>3.51</td><td>Very High</td><td>32.00 KB</td><td>7.69 KB</td><td>8.02 KB</td></tr>
    <tr><td>mozilla/libsmime3.so</td><td>208.00 KB</td><td>5.67</td><td>Moderate</td><td>208.00 KB</td><td>81.09 KB</td><td>85.56 KB</td></tr>
    <tr><td>mozilla/libsoftokn3.so</td><td>632.00 KB</td><td>6.13</td><td>Low</td><td>632.00 KB</td><td>273.35 KB</td><td>290.42 KB</td></tr>
    <tr><td>mozilla/libssl3.so</td><td>176.00 KB</td><td>6.01</td><td>Low</td><td>176.00 KB</td><td>79.57 KB</td><td>84.85 KB</td></tr>
    <tr><td>mozilla/libxpcom.so</td><td>1.55 MB</td><td>5.94</td><td>Moderate</td><td>1.55 MB</td><td>495.63 KB</td><td>495.92 KB</td></tr>
    <tr><td>mozilla/libxpistub.so</td><td>32.00 KB</td><td>4.19</td><td>High</td><td>32.00 KB</td><td>8.96 KB</td><td>9.38 KB</td></tr>
    <tr><td>mozilla/mozilla-bin</td><td>296.00 KB</td><td>4.80</td><td>High</td><td>296.00 KB</td><td>62.96 KB</td><td>69.15 KB</td></tr>
    <tr><td>mozilla/mozilla-config</td><td>3.88 KB</td><td>5.46</td><td>Moderate</td><td>3.88 KB</td><td>1.46 KB</td><td>1.58 KB</td></tr>
    <tr><td>mozilla/mozilla-xremote-client</td><td>32.00 KB</td><td>3.30</td><td>Very High</td><td>32.00 KB</td><td>7.91 KB</td><td>8.32 KB</td></tr>
    <tr><td>mozilla/mozilla/mozilla</td><td>2.86 KB</td><td>5.02</td><td>Moderate</td><td>2.86 KB</td><td>1.30 KB</td><td>1.34 KB</td></tr>
    <tr><td>mozilla/plugins/libnullplugin.so</td><td>40.00 KB</td><td>4.12</td><td>High</td><td>40.00 KB</td><td>11.07 KB</td><td>11.61 KB</td></tr>
    <tr><td>mozilla/regExport</td><td>32.00 KB</td><td>3.28</td><td>Very High</td><td>32.00 KB</td><td>7.00 KB</td><td>7.15 KB</td></tr>
    <tr><td>mozilla/regchrome</td><td>24.00 KB</td><td>2.08</td><td>Very High</td><td>24.00 KB</td><td>3.87 KB</td><td>3.96 KB</td></tr>
    <tr><td>mozilla/regxpcom</td><td>24.00 KB</td><td>3.69</td><td>Very High</td><td>24.00 KB</td><td>6.04 KB</td><td>6.24 KB</td></tr>
    <tr><td>mozilla/res/arrow.gif</td><td>49 B</td><td>3.81</td><td>Very High</td><td>49 B</td><td>62 B</td><td>58 B</td></tr>
    <tr><td>mozilla/res/arrowd.gif</td><td>52 B</td><td>4.05</td><td>High</td><td>52 B</td><td>66 B</td><td>61 B</td></tr>
    <tr><td>mozilla/res/broken-image.gif</td><td>165 B</td><td>5.27</td><td>Moderate</td><td>165 B</td><td>165 B</td><td>162 B</td></tr>
    <tr><td>mozilla/res/builtin/htmlBindings.xml</td><td>5.82 KB</td><td>4.85</td><td>High</td><td>5.82 KB</td><td>861 B</td><td>986 B</td></tr>
    <tr><td>mozilla/res/builtin/platformHTMLBindings.xml</td><td>13.05 KB</td><td>4.74</td><td>High</td><td>13.05 KB</td><td>1.27 KB</td><td>1.52 KB</td></tr>
    <tr><td>mozilla/res/builtin/xbl-forms.css</td><td>1.73 KB</td><td>4.72</td><td>High</td><td>1.73 KB</td><td>818 B</td><td>853 B</td></tr>
    <tr><td>mozilla/res/charsetData.properties</td><td>5.60 KB</td><td>4.52</td><td>High</td><td>5.60 KB</td><td>1.44 KB</td><td>1.50 KB</td></tr>
    <tr><td>mozilla/res/charsetalias.properties</td><td>10.00 KB</td><td>5.34</td><td>Moderate</td><td>10.00 KB</td><td>3.02 KB</td><td>3.13 KB</td></tr>
    <tr><td>mozilla/res/cmessage.txt</td><td>93 B</td><td>3.92</td><td>Very High</td><td>93 B</td><td>93 B</td><td>80 B</td></tr>
    <tr><td>mozilla/res/dtd/mathml.dtd</td><td>62.17 KB</td><td>5.24</td><td>Moderate</td><td>62.17 KB</td><td>13.27 KB</td><td>13.81 KB</td></tr>
    <tr><td>mozilla/res/dtd/svg.dtd</td><td>1.77 KB</td><td>4.75</td><td>High</td><td>1.77 KB</td><td>819 B</td><td>852 B</td></tr>
    <tr><td>mozilla/res/dtd/xhtml11.dtd</td><td>7.27 KB</td><td>5.21</td><td>Moderate</td><td>7.27 KB</td><td>2.12 KB</td><td>2.11 KB</td></tr>
    <tr><td>mozilla/res/entityTables/html40Latin1.properties</td><td>2.72 KB</td><td>5.05</td><td>Moderate</td><td>2.72 KB</td><td>1.10 KB</td><td>1.13 KB</td></tr>
    <tr><td>mozilla/res/entityTables/html40Special.properties</td><td>1.47 KB</td><td>5.14</td><td>Moderate</td><td>1.47 KB</td><td>736 B</td><td>767 B</td></tr>
    <tr><td>mozilla/res/entityTables/html40Symbols.properties</td><td>3.12 KB</td><td>5.03</td><td>Moderate</td><td>3.12 KB</td><td>1.21 KB</td><td>1.26 KB</td></tr>
    <tr><td>mozilla/res/entityTables/htmlEntityVersions.properties</td><td>1.04 KB</td><td>4.86</td><td>High</td><td>1.04 KB</td><td>613 B</td><td>615 B</td></tr>
    <tr><td>mozilla/res/entityTables/transliterate.properties</td><td>64.23 KB</td><td>5.24</td><td>Moderate</td><td>64.23 KB</td><td>13.63 KB</td><td>13.48 KB</td></tr>
    <tr><td>mozilla/res/fonts/mathfont.properties</td><td>37.95 KB</td><td>4.95</td><td>High</td><td>37.95 KB</td><td>5.71 KB</td><td>6.17 KB</td></tr>
    <tr><td>mozilla/res/fonts/mathfontCMEX10.properties</td><td>5.46 KB</td><td>4.69</td><td>High</td><td>5.46 KB</td><td>1.76 KB</td><td>1.87 KB</td></tr>
    <tr><td>mozilla/res/fonts/mathfontCMSY10.properties</td><td>3.66 KB</td><td>4.90</td><td>High</td><td>3.66 KB</td><td>1.29 KB</td><td>1.37 KB</td></tr>
    <tr><td>mozilla/res/fonts/mathfontMTExtra.properties</td><td>1.42 KB</td><td>5.20</td><td>Moderate</td><td>1.42 KB</td><td>781 B</td><td>805 B</td></tr>
    <tr><td>mozilla/res/fonts/mathfontMath1.properties</td><td>2.92 KB</td><td>4.67</td><td>High</td><td>2.92 KB</td><td>1.04 KB</td><td>1.08 KB</td></tr>
    <tr><td>mozilla/res/fonts/mathfontMath2.properties</td><td>4.60 KB</td><td>4.58</td><td>High</td><td>4.60 KB</td><td>1.38 KB</td><td>1.48 KB</td></tr>
    <tr><td>mozilla/res/fonts/mathfontMath4.properties</td><td>5.75 KB</td><td>4.87</td><td>High</td><td>5.75 KB</td><td>1.66 KB</td><td>1.81 KB</td></tr>
    <tr><td>mozilla/res/fonts/mathfontPUA.properties</td><td>14.46 KB</td><td>5.24</td><td>Moderate</td><td>14.46 KB</td><td>4.83 KB</td><td>4.90 KB</td></tr>
    <tr><td>mozilla/res/fonts/mathfontSymbol.properties</td><td>2.99 KB</td><td>4.69</td><td>High</td><td>2.99 KB</td><td>1.13 KB</td><td>1.18 KB</td></tr>
    <tr><td>mozilla/res/forms.css</td><td>9.49 KB</td><td>5.05</td><td>Moderate</td><td>9.49 KB</td><td>2.61 KB</td><td>2.78 KB</td></tr>
    <tr><td>mozilla/res/gfx/icon_0.gif</td><td>341 B</td><td>4.53</td><td>High</td><td>341 B</td><td>198 B</td><td>203 B</td></tr>
    <tr><td>mozilla/res/gfx/icon_1.gif</td><td>297 B</td><td>4.51</td><td>High</td><td>297 B</td><td>169 B</td><td>172 B</td></tr>
    <tr><td>mozilla/res/html.css</td><td>10.15 KB</td><td>4.87</td><td>High</td><td>10.15 KB</td><td>3.13 KB</td><td>3.27 KB</td></tr>
    <tr><td>mozilla/res/html/gopher-audio.gif</td><td>163 B</td><td>6.03</td><td>Low</td><td>163 B</td><td>181 B</td><td>172 B</td></tr>
    <tr><td>mozilla/res/html/gopher-binary.gif</td><td>165 B</td><td>6.07</td><td>Low</td><td>165 B</td><td>183 B</td><td>174 B</td></tr>
    <tr><td>mozilla/res/html/gopher-find.gif</td><td>178 B</td><td>6.25</td><td>Low</td><td>178 B</td><td>197 B</td><td>187 B</td></tr>
    <tr><td>mozilla/res/html/gopher-image.gif</td><td>188 B</td><td>5.68</td><td>Moderate</td><td>188 B</td><td>192 B</td><td>185 B</td></tr>
    <tr><td>mozilla/res/html/gopher-menu.gif</td><td>135 B</td><td>5.83</td><td>Moderate</td><td>135 B</td><td>155 B</td><td>144 B</td></tr>
    <tr><td>mozilla/res/html/gopher-movie.gif</td><td>180 B</td><td>6.25</td><td>Low</td><td>180 B</td><td>198 B</td><td>189 B</td></tr>
    <tr><td>mozilla/res/html/gopher-sound.gif</td><td>163 B</td><td>6.03</td><td>Low</td><td>163 B</td><td>181 B</td><td>172 B</td></tr>
    <tr><td>mozilla/res/html/gopher-telnet.gif</td><td>189 B</td><td>5.68</td><td>Moderate</td><td>189 B</td><td>191 B</td><td>185 B</td></tr>
    <tr><td>mozilla/res/html/gopher-text.gif</td><td>154 B</td><td>6.00</td><td>Low</td><td>154 B</td><td>173 B</td><td>163 B</td></tr>
    <tr><td>mozilla/res/html/gopher-unknown.gif</td><td>132 B</td><td>5.71</td><td>Moderate</td><td>132 B</td><td>149 B</td><td>141 B</td></tr>
    <tr><td>mozilla/res/inspector/search-registry.rdf</td><td>377 B</td><td>5.05</td><td>Moderate</td><td>377 B</td><td>247 B</td><td>241 B</td></tr>
    <tr><td>mozilla/res/inspector/viewer-registry.rdf</td><td>1.63 KB</td><td>4.90</td><td>High</td><td>1.63 KB</td><td>466 B</td><td>508 B</td></tr>
    <tr><td>mozilla/res/langGroups.properties</td><td>1.42 KB</td><td>4.89</td><td>High</td><td>1.42 KB</td><td>679 B</td><td>703 B</td></tr>
    <tr><td>mozilla/res/language.properties</td><td>3.84 KB</td><td>3.96</td><td>Very High</td><td>3.84 KB</td><td>586 B</td><td>600 B</td></tr>
    <tr><td>mozilla/res/loading-image.gif</td><td>157 B</td><td>5.18</td><td>Moderate</td><td>157 B</td><td>156 B</td><td>154 B</td></tr>
    <tr><td>mozilla/res/mathml.css</td><td>11.13 KB</td><td>4.87</td><td>High</td><td>11.13 KB</td><td>3.17 KB</td><td>3.32 KB</td></tr>
    <tr><td>mozilla/res/quirk.css</td><td>8.27 KB</td><td>4.91</td><td>High</td><td>8.27 KB</td><td>2.77 KB</td><td>2.88 KB</td></tr>
    <tr><td>mozilla/res/rdf/article.gif</td><td>164 B</td><td>5.42</td><td>Moderate</td><td>164 B</td><td>164 B</td><td>161 B</td></tr>
    <tr><td>mozilla/res/rdf/document.gif</td><td>113 B</td><td>5.32</td><td>Moderate</td><td>113 B</td><td>121 B</td><td>118 B</td></tr>
    <tr><td>mozilla/res/rdf/dom-test-1.xul</td><td>4.08 KB</td><td>4.84</td><td>High</td><td>4.08 KB</td><td>1.36 KB</td><td>1.42 KB</td></tr>
    <tr><td>mozilla/res/rdf/dom-test-2.xul</td><td>3.00 KB</td><td>5.04</td><td>Moderate</td><td>3.00 KB</td><td>1.26 KB</td><td>1.31 KB</td></tr>
    <tr><td>mozilla/res/rdf/dom-test-3.xul</td><td>2.10 KB</td><td>5.10</td><td>Moderate</td><td>2.10 KB</td><td>930 B</td><td>978 B</td></tr>
    <tr><td>mozilla/res/rdf/dom-test-4.css</td><td>130 B</td><td>4.58</td><td>High</td><td>130 B</td><td>115 B</td><td>106 B</td></tr>
    <tr><td>mozilla/res/rdf/dom-test-4.xul</td><td>1.27 KB</td><td>5.03</td><td>Moderate</td><td>1.27 KB</td><td>571 B</td><td>584 B</td></tr>
    <tr><td>mozilla/res/rdf/dom-test-5.xul</td><td>1.01 KB</td><td>4.97</td><td>High</td><td>1.01 KB</td><td>521 B</td><td>538 B</td></tr>
    <tr><td>mozilla/res/rdf/dom-test-6.xul</td><td>1.23 KB</td><td>4.96</td><td>High</td><td>1.23 KB</td><td>619 B</td><td>643 B</td></tr>
    <tr><td>mozilla/res/rdf/dom-test-7.xul</td><td>1.37 KB</td><td>5.14</td><td>Moderate</td><td>1.37 KB</td><td>696 B</td><td>720 B</td></tr>
    <tr><td>mozilla/res/rdf/dom-test-8.xul</td><td>3.97 KB</td><td>4.81</td><td>High</td><td>3.97 KB</td><td>1.35 KB</td><td>1.41 KB</td></tr>
    <tr><td>mozilla/res/rdf/folder-closed.gif</td><td>120 B</td><td>5.47</td><td>Moderate</td><td>120 B</td><td>139 B</td><td>129 B</td></tr>
    <tr><td>mozilla/res/rdf/folder-open.gif</td><td>159 B</td><td>5.40</td><td>Moderate</td><td>159 B</td><td>155 B</td><td>150 B</td></tr>
    <tr><td>mozilla/res/rdf/ignore-test.xul</td><td>1.21 KB</td><td>5.10</td><td>Moderate</td><td>1.21 KB</td><td>609 B</td><td>639 B</td></tr>
    <tr><td>mozilla/res/rdf/loading.gif</td><td>288 B</td><td>6.04</td><td>Low</td><td>288 B</td><td>247 B</td><td>245 B</td></tr>
    <tr><td>mozilla/res/rdf/xpidl-test-1.xul</td><td>1.40 KB</td><td>5.07</td><td>Moderate</td><td>1.40 KB</td><td>656 B</td><td>697 B</td></tr>
    <tr><td>mozilla/res/sample.unixpsfonts.properties</td><td>1.21 KB</td><td>4.85</td><td>High</td><td>1.21 KB</td><td>598 B</td><td>605 B</td></tr>
    <tr><td>mozilla/res/samples/Anieyes.gif</td><td>13.22 KB</td><td>7.79</td><td>None</td><td>13.22 KB</td><td>7.37 KB</td><td>7.47 KB</td></tr>
    <tr><td>mozilla/res/samples/aform.css</td><td>1.13 KB</td><td>4.99</td><td>High</td><td>1.13 KB</td><td>387 B</td><td>420 B</td></tr>
    <tr><td>mozilla/res/samples/beeptest.html</td><td>368 B</td><td>4.96</td><td>High</td><td>368 B</td><td>256 B</td><td>262 B</td></tr>
    <tr><td>mozilla/res/samples/bform.css</td><td>720 B</td><td>4.95</td><td>High</td><td>720 B</td><td>265 B</td><td>268 B</td></tr>
    <tr><td>mozilla/res/samples/bg.jpg</td><td>9.19 KB</td><td>7.67</td><td>None</td><td>9.19 KB</td><td>8.79 KB</td><td>8.78 KB</td></tr>
    <tr><td>mozilla/res/samples/cform.css</td><td>715 B</td><td>4.93</td><td>High</td><td>715 B</td><td>264 B</td><td>269 B</td></tr>
    <tr><td>mozilla/res/samples/checkboxTest.xul</td><td>1.01 KB</td><td>5.08</td><td>Moderate</td><td>1.01 KB</td><td>486 B</td><td>500 B</td></tr>
    <tr><td>mozilla/res/samples/colorpicker.xul</td><td>3.45 KB</td><td>5.13</td><td>Moderate</td><td>3.45 KB</td><td>1.25 KB</td><td>1.29 KB</td></tr>
    <tr><td>mozilla/res/samples/demoform.css</td><td>1.38 KB</td><td>4.78</td><td>High</td><td>1.38 KB</td><td>408 B</td><td>452 B</td></tr>
    <tr><td>mozilla/res/samples/dexopenchrome.xul</td><td>4.19 KB</td><td>4.62</td><td>High</td><td>4.19 KB</td><td>1.14 KB</td><td>1.18 KB</td></tr>
    <tr><td>mozilla/res/samples/dexparamdialog.html</td><td>7.20 KB</td><td>4.59</td><td>High</td><td>7.20 KB</td><td>1.90 KB</td><td>2.04 KB</td></tr>
    <tr><td>mozilla/res/samples/dexparamdialog.xul</td><td>7.86 KB</td><td>4.68</td><td>High</td><td>7.86 KB</td><td>2.14 KB</td><td>2.29 KB</td></tr>
    <tr><td>mozilla/res/samples/find.html</td><td>2.45 KB</td><td>4.92</td><td>High</td><td>2.45 KB</td><td>900 B</td><td>963 B</td></tr>
    <tr><td>mozilla/res/samples/gear1.gif</td><td>9.27 KB</td><td>6.34</td><td>Low</td><td>9.27 KB</td><td>6.49 KB</td><td>6.56 KB</td></tr>
    <tr><td>mozilla/res/samples/hidetoolicon.css</td><td>218 B</td><td>4.53</td><td>High</td><td>218 B</td><td>105 B</td><td>99 B</td></tr>
    <tr><td>mozilla/res/samples/hidetoolicon.xul</td><td>1.44 KB</td><td>5.20</td><td>Moderate</td><td>1.44 KB</td><td>534 B</td><td>546 B</td></tr>
    <tr><td>mozilla/res/samples/image_props.html</td><td>1.26 KB</td><td>5.21</td><td>Moderate</td><td>1.26 KB</td><td>573 B</td><td>603 B</td></tr>
    <tr><td>mozilla/res/samples/mozform.css</td><td>4.47 KB</td><td>4.94</td><td>High</td><td>4.47 KB</td><td>1.02 KB</td><td>1.14 KB</td></tr>
    <tr><td>mozilla/res/samples/printsetup.html</td><td>5.97 KB</td><td>4.96</td><td>High</td><td>5.97 KB</td><td>1.28 KB</td><td>1.38 KB</td></tr>
    <tr><td>mozilla/res/samples/raptor.jpg</td><td>48.47 KB</td><td>7.47</td><td>Low</td><td>48.47 KB</td><td>39.15 KB</td><td>40.62 KB</td></tr>
    <tr><td>mozilla/res/samples/rock_gra.gif</td><td>23.22 KB</td><td>7.66</td><td>None</td><td>23.22 KB</td><td>22.47 KB</td><td>22.40 KB</td></tr>
    <tr><td>mozilla/res/samples/sampleimages/bongo.gif</td><td>33.96 KB</td><td>7.89</td><td>None</td><td>33.96 KB</td><td>21.25 KB</td><td>21.21 KB</td></tr>
    <tr><td>mozilla/res/samples/sampleimages/down.gif</td><td>600 B</td><td>7.19</td><td>Low</td><td>600 B</td><td>613 B</td><td>593 B</td></tr>
    <tr><td>mozilla/res/samples/sampleimages/left.gif</td><td>566 B</td><td>7.13</td><td>Low</td><td>566 B</td><td>578 B</td><td>559 B</td></tr>
    <tr><td>mozilla/res/samples/sampleimages/right.gif</td><td>432 B</td><td>7.05</td><td>Low</td><td>432 B</td><td>450 B</td><td>441 B</td></tr>
    <tr><td>mozilla/res/samples/sampleimages/up.gif</td><td>464 B</td><td>7.04</td><td>Low</td><td>464 B</td><td>484 B</td><td>473 B</td></tr>
    <tr><td>mozilla/res/samples/scrollbarTest1.xul</td><td>1.66 KB</td><td>4.94</td><td>High</td><td>1.66 KB</td><td>576 B</td><td>626 B</td></tr>
    <tr><td>mozilla/res/samples/scrollbarTest2.xul</td><td>541 B</td><td>4.97</td><td>High</td><td>541 B</td><td>306 B</td><td>318 B</td></tr>
    <tr><td>mozilla/res/samples/sliderTest1.xul</td><td>327 B</td><td>5.04</td><td>Moderate</td><td>327 B</td><td>251 B</td><td>255 B</td></tr>
    <tr><td>mozilla/res/samples/soundtest.html</td><td>529 B</td><td>5.00</td><td>Moderate</td><td>529 B</td><td>307 B</td><td>313 B</td></tr>
    <tr><td>mozilla/res/samples/tab.xul</td><td>5.02 KB</td><td>4.48</td><td>High</td><td>5.02 KB</td><td>1.29 KB</td><td>1.37 KB</td></tr>
    <tr><td>mozilla/res/samples/test.wav</td><td>27.15 KB</td><td>5.84</td><td>Moderate</td><td>27.15 KB</td><td>18.25 KB</td><td>19.16 KB</td></tr>
    <tr><td>mozilla/res/samples/test0.html</td><td>5.99 KB</td><td>5.09</td><td>Moderate</td><td>5.99 KB</td><td>1.81 KB</td><td>1.88 KB</td></tr>
    <tr><td>mozilla/res/samples/test1.html</td><td>2.04 KB</td><td>5.11</td><td>Moderate</td><td>2.04 KB</td><td>818 B</td><td>867 B</td></tr>
    <tr><td>mozilla/res/samples/test10.html</td><td>369 B</td><td>5.39</td><td>Moderate</td><td>369 B</td><td>186 B</td><td>189 B</td></tr>
    <tr><td>mozilla/res/samples/test11.html</td><td>2.10 KB</td><td>4.84</td><td>High</td><td>2.10 KB</td><td>1002 B</td><td>1.01 KB</td></tr>
    <tr><td>mozilla/res/samples/test12.html</td><td>2.50 KB</td><td>4.61</td><td>High</td><td>2.50 KB</td><td>961 B</td><td>1012 B</td></tr>
    <tr><td>mozilla/res/samples/test13.html</td><td>2.62 KB</td><td>5.28</td><td>Moderate</td><td>2.62 KB</td><td>822 B</td><td>825 B</td></tr>
    <tr><td>mozilla/res/samples/test14.html</td><td>139 B</td><td>5.24</td><td>Moderate</td><td>139 B</td><td>139 B</td><td>133 B</td></tr>
    <tr><td>mozilla/res/samples/test15.html</td><td>142 B</td><td>5.21</td><td>Moderate</td><td>142 B</td><td>145 B</td><td>139 B</td></tr>
    <tr><td>mozilla/res/samples/test16.html</td><td>3.01 KB</td><td>5.24</td><td>Moderate</td><td>3.01 KB</td><td>844 B</td><td>905 B</td></tr>
    <tr><td>mozilla/res/samples/test2.html</td><td>1.45 KB</td><td>4.82</td><td>High</td><td>1.45 KB</td><td>491 B</td><td>512 B</td></tr>
    <tr><td>mozilla/res/samples/test3.html</td><td>2.31 KB</td><td>4.44</td><td>High</td><td>2.31 KB</td><td>673 B</td><td>718 B</td></tr>
    <tr><td>mozilla/res/samples/test4.html</td><td>8.89 KB</td><td>5.26</td><td>Moderate</td><td>8.89 KB</td><td>1.96 KB</td><td>2.05 KB</td></tr>
    <tr><td>mozilla/res/samples/test5.html</td><td>5.97 KB</td><td>4.82</td><td>High</td><td>5.97 KB</td><td>956 B</td><td>986 B</td></tr>
    <tr><td>mozilla/res/samples/test6.html</td><td>4.53 KB</td><td>4.69</td><td>High</td><td>4.53 KB</td><td>706 B</td><td>760 B</td></tr>
    <tr><td>mozilla/res/samples/test7.html</td><td>196 B</td><td>5.12</td><td>Moderate</td><td>196 B</td><td>172 B</td><td>166 B</td></tr>
    <tr><td>mozilla/res/samples/test8-1.html</td><td>1.56 KB</td><td>5.08</td><td>Moderate</td><td>1.56 KB</td><td>519 B</td><td>544 B</td></tr>
    <tr><td>mozilla/res/samples/test8.html</td><td>17.37 KB</td><td>4.98</td><td>High</td><td>17.37 KB</td><td>2.72 KB</td><td>3.02 KB</td></tr>
    <tr><td>mozilla/res/samples/test8dom.html</td><td>1.49 KB</td><td>5.16</td><td>Moderate</td><td>1.49 KB</td><td>588 B</td><td>634 B</td></tr>
    <tr><td>mozilla/res/samples/test8sca.html</td><td>5.37 KB</td><td>3.59</td><td>Very High</td><td>5.37 KB</td><td>142 B</td><td>111 B</td></tr>
    <tr><td>mozilla/res/samples/test8siz.html</td><td>5.57 KB</td><td>4.65</td><td>High</td><td>5.57 KB</td><td>660 B</td><td>635 B</td></tr>
    <tr><td>mozilla/res/samples/test8tab.html</td><td>155 B</td><td>4.47</td><td>High</td><td>155 B</td><td>125 B</td><td>115 B</td></tr>
    <tr><td>mozilla/res/samples/test9.html</td><td>218 B</td><td>4.59</td><td>High</td><td>218 B</td><td>142 B</td><td>141 B</td></tr>
    <tr><td>mozilla/res/samples/test9a.html</td><td>1.43 KB</td><td>4.85</td><td>High</td><td>1.43 KB</td><td>337 B</td><td>358 B</td></tr>
    <tr><td>mozilla/res/samples/test9b.html</td><td>507 B</td><td>4.63</td><td>High</td><td>507 B</td><td>197 B</td><td>198 B</td></tr>
    <tr><td>mozilla/res/samples/test_ed.html</td><td>31 B</td><td>3.48</td><td>Very High</td><td>31 B</td><td>39 B</td><td>40 B</td></tr>
    <tr><td>mozilla/res/samples/test_form.html</td><td>897 B</td><td>4.76</td><td>High</td><td>897 B</td><td>270 B</td><td>282 B</td></tr>
    <tr><td>mozilla/res/samples/test_gfx.html</td><td>3.62 KB</td><td>5.06</td><td>Moderate</td><td>3.62 KB</td><td>601 B</td><td>658 B</td></tr>
    <tr><td>mozilla/res/samples/test_lbox.html</td><td>2.23 KB</td><td>4.49</td><td>High</td><td>2.23 KB</td><td>539 B</td><td>593 B</td></tr>
    <tr><td>mozilla/res/samples/test_pr.html</td><td>1.56 KB</td><td>5.35</td><td>Moderate</td><td>1.56 KB</td><td>659 B</td><td>690 B</td></tr>
    <tr><td>mozilla/res/samples/test_weight.html</td><td>1.36 KB</td><td>5.09</td><td>Moderate</td><td>1.36 KB</td><td>363 B</td><td>380 B</td></tr>
    <tr><td>mozilla/res/samples/toolbarTest1.xul</td><td>1.53 KB</td><td>4.84</td><td>High</td><td>1.53 KB</td><td>709 B</td><td>746 B</td></tr>
    <tr><td>mozilla/res/samples/treeTest1.css</td><td>883 B</td><td>4.77</td><td>High</td><td>883 B</td><td>384 B</td><td>400 B</td></tr>
    <tr><td>mozilla/res/samples/treeTest1.xul</td><td>3.55 KB</td><td>4.78</td><td>High</td><td>3.55 KB</td><td>672 B</td><td>702 B</td></tr>
    <tr><td>mozilla/res/samples/widgets.xul</td><td>5.94 KB</td><td>4.79</td><td>High</td><td>5.94 KB</td><td>1.63 KB</td><td>1.71 KB</td></tr>
    <tr><td>mozilla/res/samples/xpmenu.xul</td><td>4.18 KB</td><td>4.77</td><td>High</td><td>4.18 KB</td><td>1.08 KB</td><td>1.16 KB</td></tr>
    <tr><td>mozilla/res/samples/xulTest.css</td><td>2.09 KB</td><td>4.94</td><td>High</td><td>2.09 KB</td><td>655 B</td><td>708 B</td></tr>
    <tr><td>mozilla/res/throbber/anim.gif</td><td>7.70 KB</td><td>7.75</td><td>None</td><td>7.70 KB</td><td>7.14 KB</td><td>7.16 KB</td></tr>
    <tr><td>mozilla/res/throbber/anims00.gif</td><td>266 B</td><td>6.78</td><td>Low</td><td>266 B</td><td>283 B</td><td>275 B</td></tr>
    <tr><td>mozilla/res/throbber/anims01.gif</td><td>305 B</td><td>7.00</td><td>Low</td><td>305 B</td><td>323 B</td><td>314 B</td></tr>
    <tr><td>mozilla/res/throbber/anims02.gif</td><td>308 B</td><td>6.92</td><td>Low</td><td>308 B</td><td>327 B</td><td>317 B</td></tr>
    <tr><td>mozilla/res/throbber/anims03.gif</td><td>307 B</td><td>7.02</td><td>Low</td><td>307 B</td><td>328 B</td><td>316 B</td></tr>
    <tr><td>mozilla/res/throbber/anims04.gif</td><td>313 B</td><td>6.96</td><td>Low</td><td>313 B</td><td>336 B</td><td>322 B</td></tr>
    <tr><td>mozilla/res/throbber/anims05.gif</td><td>307 B</td><td>6.95</td><td>Low</td><td>307 B</td><td>328 B</td><td>316 B</td></tr>
    <tr><td>mozilla/res/throbber/anims06.gif</td><td>324 B</td><td>7.00</td><td>Low</td><td>324 B</td><td>347 B</td><td>333 B</td></tr>
    <tr><td>mozilla/res/throbber/anims07.gif</td><td>324 B</td><td>6.98</td><td>Low</td><td>324 B</td><td>347 B</td><td>333 B</td></tr>
    <tr><td>mozilla/res/throbber/anims08.gif</td><td>327 B</td><td>6.98</td><td>Low</td><td>327 B</td><td>346 B</td><td>336 B</td></tr>
    <tr><td>mozilla/res/throbber/anims09.gif</td><td>317 B</td><td>7.03</td><td>Low</td><td>317 B</td><td>336 B</td><td>326 B</td></tr>
    <tr><td>mozilla/res/throbber/anims10.gif</td><td>322 B</td><td>7.03</td><td>Low</td><td>322 B</td><td>345 B</td><td>331 B</td></tr>
    <tr><td>mozilla/res/throbber/anims11.gif</td><td>317 B</td><td>6.93</td><td>Low</td><td>317 B</td><td>336 B</td><td>326 B</td></tr>
    <tr><td>mozilla/res/throbber/anims12.gif</td><td>305 B</td><td>6.84</td><td>Low</td><td>305 B</td><td>326 B</td><td>314 B</td></tr>
    <tr><td>mozilla/res/throbber/anims13.gif</td><td>287 B</td><td>6.96</td><td>Low</td><td>287 B</td><td>305 B</td><td>296 B</td></tr>
    <tr><td>mozilla/res/throbber/anims14.gif</td><td>285 B</td><td>6.92</td><td>Low</td><td>285 B</td><td>303 B</td><td>294 B</td></tr>
    <tr><td>mozilla/res/throbber/anims15.gif</td><td>290 B</td><td>6.86</td><td>Low</td><td>290 B</td><td>307 B</td><td>299 B</td></tr>
    <tr><td>mozilla/res/throbber/anims16.gif</td><td>277 B</td><td>6.78</td><td>Low</td><td>277 B</td><td>294 B</td><td>286 B</td></tr>
    <tr><td>mozilla/res/throbber/anims17.gif</td><td>303 B</td><td>6.89</td><td>Low</td><td>303 B</td><td>326 B</td><td>312 B</td></tr>
    <tr><td>mozilla/res/throbber/anims18.gif</td><td>298 B</td><td>6.85</td><td>Low</td><td>298 B</td><td>317 B</td><td>307 B</td></tr>
    <tr><td>mozilla/res/throbber/anims19.gif</td><td>297 B</td><td>6.94</td><td>Low</td><td>297 B</td><td>316 B</td><td>306 B</td></tr>
    <tr><td>mozilla/res/throbber/anims20.gif</td><td>281 B</td><td>6.85</td><td>Low</td><td>281 B</td><td>300 B</td><td>290 B</td></tr>
    <tr><td>mozilla/res/throbber/anims21.gif</td><td>284 B</td><td>6.88</td><td>Low</td><td>284 B</td><td>301 B</td><td>293 B</td></tr>
    <tr><td>mozilla/res/throbber/anims22.gif</td><td>296 B</td><td>6.90</td><td>Low</td><td>296 B</td><td>316 B</td><td>305 B</td></tr>
    <tr><td>mozilla/res/throbber/anims23.gif</td><td>255 B</td><td>6.81</td><td>Low</td><td>255 B</td><td>273 B</td><td>264 B</td></tr>
    <tr><td>mozilla/res/throbber/anims24.gif</td><td>254 B</td><td>6.67</td><td>Low</td><td>254 B</td><td>272 B</td><td>263 B</td></tr>
    <tr><td>mozilla/res/throbber/anims25.gif</td><td>214 B</td><td>6.67</td><td>Low</td><td>214 B</td><td>237 B</td><td>223 B</td></tr>
    <tr><td>mozilla/res/throbber/anims26.gif</td><td>249 B</td><td>6.82</td><td>Low</td><td>249 B</td><td>269 B</td><td>258 B</td></tr>
    <tr><td>mozilla/res/throbber/anims27.gif</td><td>240 B</td><td>6.97</td><td>Low</td><td>240 B</td><td>263 B</td><td>249 B</td></tr>
    <tr><td>mozilla/res/throbber/anims28.gif</td><td>263 B</td><td>6.78</td><td>Low</td><td>263 B</td><td>279 B</td><td>272 B</td></tr>
    <tr><td>mozilla/res/throbber/anims29.gif</td><td>282 B</td><td>6.84</td><td>Low</td><td>282 B</td><td>300 B</td><td>291 B</td></tr>
    <tr><td>mozilla/res/ua.css</td><td>2.30 KB</td><td>4.86</td><td>High</td><td>2.30 KB</td><td>1.02 KB</td><td>1.07 KB</td></tr>
    <tr><td>mozilla/res/unixcharset.properties</td><td>17.63 KB</td><td>5.13</td><td>Moderate</td><td>17.63 KB</td><td>3.52 KB</td><td>3.81 KB</td></tr>
    <tr><td>mozilla/res/viewer.properties</td><td>11 B</td><td>3.46</td><td>Very High</td><td>11 B</td><td>31 B</td><td>20 B</td></tr>
    <tr><td>mozilla/res/viewsource.css</td><td>2.75 KB</td><td>4.95</td><td>High</td><td>2.75 KB</td><td>1.19 KB</td><td>1.24 KB</td></tr>
    <tr><td>mozilla/run-mozilla.sh</td><td>10.08 KB</td><td>5.40</td><td>Moderate</td><td>10.08 KB</td><td>3.23 KB</td><td>3.39 KB</td></tr>
    <tr><td>mozilla/searchplugins/NetscapeSearch.gif</td><td>250 B</td><td>5.77</td><td>Moderate</td><td>250 B</td><td>263 B</td><td>251 B</td></tr>
    <tr><td>mozilla/searchplugins/NetscapeSearch.src</td><td>1.45 KB</td><td>4.93</td><td>High</td><td>1.45 KB</td><td>574 B</td><td>612 B</td></tr>
    <tr><td>mozilla/searchplugins/bugzilla.gif</td><td>572 B</td><td>6.38</td><td>Low</td><td>572 B</td><td>479 B</td><td>492 B</td></tr>
    <tr><td>mozilla/searchplugins/bugzilla.src</td><td>1.42 KB</td><td>5.46</td><td>Moderate</td><td>1.42 KB</td><td>594 B</td><td>599 B</td></tr>
    <tr><td>mozilla/searchplugins/dmoz.gif</td><td>605 B</td><td>7.03</td><td>Low</td><td>605 B</td><td>591 B</td><td>563 B</td></tr>
    <tr><td>mozilla/searchplugins/dmoz.src</td><td>722 B</td><td>5.19</td><td>Moderate</td><td>722 B</td><td>369 B</td><td>376 B</td></tr>
    <tr><td>mozilla/searchplugins/google.gif</td><td>1.05 KB</td><td>6.51</td><td>Low</td><td>1.05 KB</td><td>928 B</td><td>905 B</td></tr>
    <tr><td>mozilla/searchplugins/google.src</td><td>563 B</td><td>4.90</td><td>High</td><td>563 B</td><td>290 B</td><td>301 B</td></tr>
    <tr><td>mozilla/searchplugins/lxrmozilla.gif</td><td>572 B</td><td>6.38</td><td>Low</td><td>572 B</td><td>479 B</td><td>492 B</td></tr>
    <tr><td>mozilla/searchplugins/lxrmozilla.src</td><td>506 B</td><td>5.06</td><td>Moderate</td><td>506 B</td><td>303 B</td><td>306 B</td></tr>
    <tr><td>mozilla/searchplugins/mozilla.gif</td><td>572 B</td><td>6.38</td><td>Low</td><td>572 B</td><td>479 B</td><td>492 B</td></tr>
    <tr><td>mozilla/searchplugins/mozilla.src</td><td>815 B</td><td>4.93</td><td>High</td><td>815 B</td><td>433 B</td><td>443 B</td></tr>
    <tr><td>mozilla/timebombgen</td><td>24.00 KB</td><td>1.93</td><td>Excellent</td><td>24.00 KB</td><td>3.53 KB</td><td>3.61 KB</td></tr>
    <tr><td>mozilla/xpcshell</td><td>40.00 KB</td><td>4.15</td><td>High</td><td>40.00 KB</td><td>11.57 KB</td><td>12.24 KB</td></tr>
    <tr><td>mozilla/xpicleanup</td><td>48.00 KB</td><td>5.04</td><td>Moderate</td><td>48.00 KB</td><td>17.98 KB</td><td>19.29 KB</td></tr>
    <tr><td>mozilla/xpidl</td><td>112.00 KB</td><td>6.03</td><td>Low</td><td>112.00 KB</td><td>47.13 KB</td><td>50.59 KB</td></tr>
    <tr><td>mozilla/xpt_dump</td><td>48.00 KB</td><td>5.28</td><td>Moderate</td><td>48.00 KB</td><td>18.81 KB</td><td>20.03 KB</td></tr>
    <tr><td>mozilla/xpt_link</td><td>40.00 KB</td><td>5.20</td><td>Moderate</td><td>40.00 KB</td><td>16.62 KB</td><td>17.76 KB</td></tr>
    <tr><td>samba/COPYING</td><td>17.56 KB</td><td>4.69</td><td>High</td><td>17.56 KB</td><td>6.68 KB</td><td>6.93 KB</td></tr>
    <tr><td>samba/Manifest</td><td>3.71 KB</td><td>4.85</td><td>High</td><td>3.71 KB</td><td>1.68 KB</td><td>1.75 KB</td></tr>
    <tr><td>samba/README</td><td>6.39 KB</td><td>4.81</td><td>High</td><td>6.39 KB</td><td>2.94 KB</td><td>3.00 KB</td></tr>
    <tr><td>samba/Roadmap</td><td>1.85 KB</td><td>4.71</td><td>High</td><td>1.85 KB</td><td>935 B</td><td>969 B</td></tr>
    <tr><td>samba/WHATSNEW.txt</td><td>23.83 KB</td><td>4.83</td><td>High</td><td>23.83 KB</td><td>9.13 KB</td><td>9.47 KB</td></tr>
    <tr><td>xml/elts.xml</td><td>110.48 KB</td><td>5.21</td><td>Moderate</td><td>110.48 KB</td><td>8.52 KB</td><td>9.64 KB</td></tr>
    <tr><td>xml/pcc1.xml</td><td>47.60 KB</td><td>4.57</td><td>High</td><td>47.60 KB</td><td>3.14 KB</td><td>3.90 KB</td></tr>
    <tr><td>xml/pcc2.xml</td><td>246.76 KB</td><td>4.53</td><td>High</td><td>246.76 KB</td><td>9.58 KB</td><td>11.68 KB</td></tr>
    <tr><td>xml/pcc3.xml</td><td>175.42 KB</td><td>4.62</td><td>High</td><td>175.42 KB</td><td>7.90 KB</td><td>8.88 KB</td></tr>
    <tr><td>xml/play1.xml</td><td>245.99 KB</td><td>5.17</td><td>Moderate</td><td>245.99 KB</td><td>66.63 KB</td><td>69.44 KB</td></tr>
    <tr><td>xml/play2.xml</td><td>133.63 KB</td><td>5.16</td><td>Moderate</td><td>133.63 KB</td><td>35.98 KB</td><td>38.01 KB</td></tr>
    <tr><td>xml/play3.xml</td><td>273.11 KB</td><td>5.15</td><td>Moderate</td><td>273.11 KB</td><td>77.59 KB</td><td>80.00 KB</td></tr>
    <tr><td>xml/sprot.xml</td><td>10.01 KB</td><td>5.48</td><td>Moderate</td><td>10.01 KB</td><td>2.53 KB</td><td>2.64 KB</td></tr>
    <tr><td>xml/stats1.xml</td><td>653.62 KB</td><td>4.85</td><td>High</td><td>653.62 KB</td><td>65.30 KB</td><td>64.50 KB</td></tr>
    <tr><td>xml/stats2.xml</td><td>601.65 KB</td><td>4.86</td><td>High</td><td>601.65 KB</td><td>56.40 KB</td><td>54.62 KB</td></tr>
    <tr><td>xml/tal1.xml</td><td>717.32 KB</td><td>4.77</td><td>High</td><td>717.32 KB</td><td>27.99 KB</td><td>28.83 KB</td></tr>
    <tr><td>xml/tal2.xml</td><td>498.10 KB</td><td>4.78</td><td>High</td><td>498.10 KB</td><td>19.96 KB</td><td>22.71 KB</td></tr>
    <tr><td>xml/tal3.xml</td><td>245.75 KB</td><td>4.75</td><td>High</td><td>245.75 KB</td><td>10.07 KB</td><td>10.40 KB</td></tr>
    <tr><td>xml/tpc.xml</td><td>281.16 KB</td><td>5.27</td><td>Moderate</td><td>281.16 KB</td><td>51.89 KB</td><td>51.81 KB</td></tr>
    <tr><td>xml/treebank.xml</td><td>6.15 KB</td><td>3.52</td><td>Very High</td><td>6.15 KB</td><td>1.36 KB</td><td>1.48 KB</td></tr>
    <tr><td>xml/w3c1.xml</td><td>215.69 KB</td><td>5.27</td><td>Moderate</td><td>215.69 KB</td><td>51.03 KB</td><td>53.33 KB</td></tr>
    <tr><td>xml/w3c2.xml</td><td>191.71 KB</td><td>5.22</td><td>Moderate</td><td>191.71 KB</td><td>46.77 KB</td><td>49.12 KB</td></tr>
    <tr><td>xml/w3c3.xml</td><td>197.19 KB</td><td>5.22</td><td>Moderate</td><td>197.19 KB</td><td>52.91 KB</td><td>55.34 KB</td></tr>
    <tr><td>xml/w3c4.xml</td><td>102.55 KB</td><td>4.93</td><td>High</td><td>102.55 KB</td><td>23.14 KB</td><td>25.13 KB</td></tr>
    <tr><td>xml/w3c5.xml</td><td>241.74 KB</td><td>4.93</td><td>High</td><td>241.74 KB</td><td>55.15 KB</td><td>58.32 KB</td></tr>
    <tr><td>xml/weblog.xml</td><td>2.19 KB</td><td>5.19</td><td>Moderate</td><td>2.19 KB</td><td>565 B</td><td>599 B</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>181.03 MB</strong></td><td><strong>5.22</strong></td><td><strong>-</strong></td><td><strong>181.03 MB</strong></td><td><strong>59.91 MB</strong></td><td><strong>59.55 MB</strong></td></tr>
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
| Light | 1.000 | 0.0% | 81.5 MB/s | 89.0 MB/s | 8.00 | 1.00× | 9 MB | 100% | ✅ |
| GZIP | 0.498 | 50.2% | 4.1 MB/s | 19.1 MB/s | 3.98 | 2.01× | 9 MB | 89% | ✅ |
| Zstandard | 0.503 | 49.7% | 17.1 MB/s | 35.2 MB/s | 4.02 | 1.99× | 9 MB | 100% | ✅ |



<details>
<summary><strong>Light</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **1.000** | **0.0%** | **81.5 MB/s** | **89.0 MB/s** | **8.00** | **1.00×** | **9 MB** | **100%** | **✅** |
| archives | 1.000 | 0.0% | 790.2 MB/s | 951.7 MB/s | 8.00 | 1.00× | 9 MB | 80% | ✅ |
| contents | 1.000 | 0.0% | 72.6 MB/s | 77.0 MB/s | 8.00 | 1.00× | 9 MB | 100% | ✅ |
| blobs | 1.000 | 0.0% | 627.5 MB/s | 907.3 MB/s | 8.00 | 1.00× | 9 MB | 81% | ✅ |

</details>

<details>
<summary><strong>GZIP</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.498** | **50.2%** | **4.1 MB/s** | **19.1 MB/s** | **3.98** | **2.01×** | **9 MB** | **89%** | **✅** |
| archives | 0.958 | 4.2% | 9.4 MB/s | 110.3 MB/s | 7.67 | 1.04× | 9 MB | 65% | ✅ |
| contents | 0.496 | 50.4% | 4.1 MB/s | 18.2 MB/s | 3.97 | 2.02× | 9 MB | 89% | ✅ |
| blobs | 0.300 | 70.0% | 11.5 MB/s | 52.2 MB/s | 2.40 | 3.33× | 9 MB | 61% | ✅ |

</details>

<details>
<summary><strong>Zstandard</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.503** | **49.7%** | **17.1 MB/s** | **35.2 MB/s** | **4.02** | **1.99×** | **9 MB** | **100%** | **✅** |
| archives | 0.961 | 3.9% | 155.8 MB/s | 215.5 MB/s | 7.69 | 1.04× | 10 MB | 66% | ✅ |
| contents | 0.501 | 49.9% | 15.9 MB/s | 33.3 MB/s | 4.01 | 2.00× | 9 MB | 101% | ✅ |
| blobs | 0.299 | 70.1% | 43.4 MB/s | 127.6 MB/s | 2.39 | 3.34× | 9 MB | 58% | ✅ |

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
    <tr><td>enwik8/enwik8</td><td>95.37 MB</td><td>5.08</td><td>Moderate</td><td>95.37 MB</td><td>34.86 MB</td><td>33.94 MB</td></tr>
    <tr><td>enwik9/enwik9</td><td>953.67 MB</td><td>5.16</td><td>Moderate</td><td>953.67 MB</td><td>308.93 MB</td><td>298.76 MB</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>1.02 GB</strong></td><td><strong>5.12</strong></td><td><strong>-</strong></td><td><strong>1.02 GB</strong></td><td><strong>343.79 MB</strong></td><td><strong>332.70 MB</strong></td></tr>
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
| Light | 1.000 | 0.0% | 860.1 MB/s | 751.9 MB/s | 8.00 | 1.00× | 9 MB | 46% | ✅ |
| GZIP | 0.563 | 43.7% | 6.5 MB/s | 75.4 MB/s | 4.51 | 1.78× | 9 MB | 52% | ✅ |
| Zstandard | 0.556 | 44.4% | 61.6 MB/s | 105.4 MB/s | 4.45 | 1.80× | 9 MB | 42% | ✅ |



<details>
<summary><strong>Light</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **1.000** | **0.0%** | **860.1 MB/s** | **751.9 MB/s** | **8.00** | **1.00×** | **9 MB** | **46%** | **✅** |
| archives | 1.000 | 0.0% | 751.0 MB/s | 756.3 MB/s | 8.00 | 1.00× | 9 MB | 46% | ✅ |
| contents | 1.000 | 0.0% | 822.6 MB/s | 778.9 MB/s | 8.00 | 1.00× | 9 MB | 43% | ✅ |
| blobs | 1.000 | 0.0% | 1,006.8 MB/s | 720.5 MB/s | 8.00 | 1.00× | 9 MB | 48% | ✅ |

</details>

<details>
<summary><strong>GZIP</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.563** | **43.7%** | **6.5 MB/s** | **75.4 MB/s** | **4.51** | **1.78×** | **9 MB** | **52%** | **✅** |
| archives | 1.000 | -0.0% | 7.1 MB/s | 147.3 MB/s | 8.00 | 1.00× | 9 MB | 51% | ✅ |
| contents | 0.345 | 65.5% | 5.8 MB/s | 46.7 MB/s | 2.76 | 2.90× | 9 MB | 52% | ✅ |
| blobs | 0.345 | 65.5% | 6.6 MB/s | 32.3 MB/s | 2.76 | 2.90× | 9 MB | 53% | ✅ |

</details>

<details>
<summary><strong>Zstandard</strong></summary>

| Directory | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **0.556** | **44.4%** | **61.6 MB/s** | **105.4 MB/s** | **4.45** | **1.80×** | **9 MB** | **42%** | **✅** |
| archives | 1.000 | 0.0% | 139.4 MB/s | 201.1 MB/s | 8.00 | 1.00× | 9 MB | 42% | ✅ |
| contents | 0.335 | 66.5% | 18.9 MB/s | 52.8 MB/s | 2.68 | 2.99× | 9 MB | 37% | ✅ |
| blobs | 0.335 | 66.5% | 26.5 MB/s | 62.2 MB/s | 2.68 | 2.99× | 9 MB | 46% | ✅ |

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
| Light | 1.000 | 0.0% | 263.4 MB/s | 282.5 MB/s | 8.00 | 1.00× | 9 MB | 90% | ✅ |
| GZIP | 2.488 | -148.8% | 3.9 MB/s | 75.3 MB/s | 19.90 | 0.40× | 9 MB | 81% | ✅ |
| Zstandard | 1.667 | -66.7% | 95.4 MB/s | 93.6 MB/s | 13.33 | 0.60× | 9 MB | 96% | ✅ |



<details>
<summary><strong>Light</strong></summary>

| Size | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **1.000** | **0.0%** | **263.4 MB/s** | **282.5 MB/s** | **8.00** | **1.00×** | **9 MB** | **90%** | **✅** |
| B | 1.000 | 0.0% | 0.1 MB/s | 0.1 MB/s | 8.00 | 1.00× | 9 MB | 109% | ✅ |
| KB | 1.000 | 0.0% | 60.4 MB/s | 59.0 MB/s | 8.00 | 1.00× | 9 MB | 84% | ✅ |
| MB | 1.000 | 0.0% | 929.5 MB/s | 1,005.3 MB/s | 8.00 | 1.00× | 9 MB | 74% | ✅ |

</details>

<details>
<summary><strong>GZIP</strong></summary>

| Size | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **2.488** | **-148.8%** | **3.9 MB/s** | **75.3 MB/s** | **19.90** | **0.40×** | **9 MB** | **81%** | **✅** |
| B | 5.013 | -401.3% | 0.1 MB/s | 0.1 MB/s | 40.10 | 0.20× | 9 MB | 105% | ✅ |
| KB | 1.005 | -0.5% | 4.6 MB/s | 50.9 MB/s | 8.04 | 1.00× | 9 MB | 73% | ✅ |
| MB | 1.000 | -0.0% | 8.5 MB/s | 217.8 MB/s | 8.00 | 1.00× | 9 MB | 58% | ✅ |

</details>

<details>
<summary><strong>Zstandard</strong></summary>

| Size | `Ratio` | `Savings` | `Cospeed` | `Despeed` | `BPB` | `Factor` | `Mutilization` | `Putilization` | `Integrity` |
|---|---|---|---|---|---|---|---|---|---|
| **average** | **1.667** | **-66.7%** | **95.4 MB/s** | **93.6 MB/s** | **13.33** | **0.60×** | **9 MB** | **96%** | **✅** |
| B | 2.798 | -179.8% | 0.1 MB/s | 0.1 MB/s | 22.39 | 0.36× | 9 MB | 108% | ✅ |
| KB | 1.002 | -0.2% | 53.8 MB/s | 59.5 MB/s | 8.01 | 1.00× | 9 MB | 103% | ✅ |
| MB | 1.000 | -0.0% | 291.1 MB/s | 275.9 MB/s | 8.00 | 1.00× | 9 MB | 68% | ✅ |

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
      <td>4.76</td>
      <td>1.000</td>
      <td>5.24</td>
      <td>1.000</td>
      <td>6.08</td>
      <td>1.000</td>
      <td>6.57</td>
    </tr>
    <tr>
      <td>GZIP</td>
      <td>0.851</td>
      <td>4.76</td>
      <td>0.498</td>
      <td>5.24</td>
      <td>0.563</td>
      <td>6.08</td>
      <td>2.488</td>
      <td>6.57</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td>0.604</td>
      <td>4.76</td>
      <td>0.503</td>
      <td>5.24</td>
      <td>0.556</td>
      <td>6.08</td>
      <td>1.667</td>
      <td>6.57</td>
    </tr>

  </tbody>
</table>

> **⚠️ Anomaly:** Light achieves compression on random data (ratio 0.769) despite entropy of 7.999 bits/byte, which theoretically permits no lossless compression. This suggests Light employs novel techniques that operate outside traditional Shannon entropy bounds.

---

## Conclusion

Zstandard demonstrates strong compression performance across all tested corpora. Benchmark results indicate overall codec behavior is consistent with expected compression characteristics.

For standard corpora with lower entropy, Zstandard achieves competitive compression ratios while maintaining reasonable speeds.

Further investigation into individual codec performance characteristics is recommended for production deployment decisions.

---
Copyright © 2026 Linkersoft
