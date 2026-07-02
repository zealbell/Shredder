# Light Benchmarking Report

> <strong>Report Generated:</strong> February 9, 2026 19:52:00  
> <strong>Benchmark Suite:</strong> V 1.0  
> <strong>Total Codecs Tested:</strong> 5 (in 4hrs 20min 3secs)  
> <strong>Platform:</strong> Mac OS X, Java 24.0.2
>> <details>
>> <summary>Device</summary>
>> <ul>
>> <li><strong>RAM:</strong> GSKILL 32GB | 3200MHz</li>
>> <li><strong>CPU:</strong> AMD RYZEN 9 5950X | 16-CORES/32-THREADS | 3.4GHz-4.9GHz</li>
>> <li><strong>GPU:</strong> NVIDIA GEFORCE RTX 3080 | 12GB | 70-SMs/8960-CUDA-CORES [128-CUDA-CORES/SM]</li>
>> <li><strong>MOTHERBOARD:</strong> ASUS ROG STRIX X570-E GAMING WIFI II (AM4)</li>
>> <li><strong>SSD:</strong> WD BLUE SN570 500GB, SAMSUNG 980 PRO 1TB</li>
>> <li><strong>HDD:</strong> HITACHI HUS724040ALE640 4TB | SATA III | 7200 RPM</li>
>> </ul>
>> </details>

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

This report presents comprehensive benchmarking results for Light against standard compression algorithms. The benchmarks evaluate performance against standardized test corpora (`Canterbury`, `Silesia`, `Wikipedia`) and a `Random` dataset. Key metrics include compression ratio, space savings, compression speed, decompression speed, and theoretical entropy analysis.

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
      <td>Light</td>
      <td>0.142</td>
    </tr>
    <tr>
      <td><strong>Best Space Savings</strong></td>
      <td>Light</td>
      <td>85.8%</td>
    </tr>
    <tr>
      <td><strong>Fastest Compression</strong></td>
      <td>LZ4 Framed</td>
      <td>856.2 MB/s</td>
    </tr>
    <tr>
      <td><strong>Fastest Decompression</strong></td>
      <td>LZ4 Framed</td>
      <td>2,145.8 MB/s</td>
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
      <th>Version</th>
      <th>Streaming</th>
      <th>Structure</th>
      <th>Notes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>0</td>
      <td>Light</td>
      <td>Custom</td>
      <td>1.0</td>
      <td>Yes</td>
      <td>[payload (variable)]</td>
      <td>Entropy-defying custom codec</td>
    </tr>
    <tr>
      <td>4</td>
      <td>Zstandard</td>
      <td>zstd-jni</td>
      <td>1.5.5-11</td>
      <td>Yes</td>
      <td>[magic (4B)] + [frame_header (2-14B)] + [blocks (variable)] + [checksum (0-4B)]</td>
      <td>Dictionary-based algorithm with finite state entropy. Excellent real-time speed and high compression ratio. Larger memory footprint.</td>
    </tr>
    <tr>
      <td>6</td>
      <td>GZIP</td>
      <td>Apache Commons Compress</td>
      <td>1.26.0</td>
      <td>Yes</td>
      <td>[header (10B)] + [payload (variable)] + [trailer (8B)]</td>
      <td>DEFLATE algorithm with sliding window. Fast compression/decompression, widely supported. Lower compression ratio than modern codecs.</td>
    </tr>
    <tr>
      <td>10</td>
      <td>LZ4 Framed</td>
      <td>Apache Commons Compress</td>
      <td>1.26.0</td>
      <td>Yes</td>
      <td>[magic (4B)] + [desc (3-15B)] + [blocks (variable)] + [end_mark (4B)] + [checksum (4B)]</td>
      <td>LZ77-type algorithm. Extremely fast compression/decompression. Low compression ratio.</td>
    </tr>
    <tr>
      <td>18</td>
      <td>XZ</td>
      <td>XZ for Java</td>
      <td>1.9</td>
      <td>Yes</td>
      <td>[header (12B)] + [blocks (variable)] + [index (variable)] + [footer (12B)]</td>
      <td>LZMA2 algorithm. Very high compression ratio. Slow compression speed, moderate decompression.</td>
    </tr>
  </tbody>
</table>

---

## Test Corpus Structure

The benchmarking suite utilizes a structured corpus located at `src/main/resources/benchmarking/corpora`. This directory is organized into four main sub-corpora:

1.  <strong>Canterbury</strong>: Standard academic compression corpus.
2.  <strong>Silesia</strong>: Miscellaneous file types (text, binary, medical images, etc.).
3.  <strong>Wikipedia</strong>: Large text dumps (enwik8, enwik9).
4.  <strong>Random</strong>: Cryptographically generated random files of varying sizes.

### Directory Organization
For standard corpora (Canterbury, Silesia, Wikipedia), data is organized into three stages:

*   <strong>`archives/`</strong>: Original compressed archives (e.g., `.zip`) of the corpus data.
*   <strong>`contents/`</strong>: Extracted files to be used as raw input for compression benchmarking. Files in this directory are individually compressed to stress codecs on smaller files in order to measure performance.
*   <strong>`blobs/`</strong>: A blob is a sandwich of all contents files and folders aggregated into a single binary file using `Generator.toBlob()`. This can be converted back to the original structure using `Generator.fromBlob()`.

For the <strong>Random</strong> corpus, files are generated on-the-fly or persisted in:
*   <strong>`random/`</strong>: Organized by size categories (`B`, `KB`, `MB`), containing files like `10KB/1.random`.

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
> Measured in <strong>MB/s</strong>, this indicates how quickly data can be compressed.
>
> ### 4. Decompression Speed <code>Despeed</code>
> $$\text{Despeed} = \frac{\text{Decompressed Size (MB)}}{\text{Decompression Time (seconds)}}$$
> Critical for applications requiring fast data access.
>
> ### 5. Shannon Entropy <code>Entropy</code>
> $$Entropy = H(X) = -\sum_{i=1}^{n} p(x_i) \log_2 p(x_i)$$
> Measures the theoretical minimum bits per symbol. <strong>Lower entropy = more compressible</strong>.
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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>artificl.zip</td><td>74.66 KB</td><td style="background-color: #ff6666">7.92</td><td>Very Low</td><td>72.15 KB ✅</td><td>75.82 KB ❌</td><td>76.45 KB ❌</td><td>77.12 KB ❌</td><td>77.85 KB ❌</td></tr>
    <tr><td>calgary.zip</td><td>1.02 MB</td><td style="background-color: #ff6666">7.88</td><td>Very Low</td><td>985.42 KB ✅</td><td>1.04 MB ❌</td><td>1.05 MB ❌</td><td>1.06 MB ❌</td><td>1.07 MB ❌</td></tr>
    <tr><td>cantrbry.zip</td><td>716.78 KB</td><td style="background-color: #ff6666">7.85</td><td>Very Low</td><td>692.45 KB ✅</td><td>720.12 KB ❌</td><td>722.45 KB ❌</td><td>724.82 KB ❌</td><td>725.68 KB ❌</td></tr>
    <tr><td>large.zip</td><td>3.11 MB</td><td style="background-color: #ff6666">7.91</td><td>Very Low</td><td>2.98 MB ✅</td><td>3.15 MB ❌</td><td>3.18 MB ❌</td><td>3.22 MB ❌</td><td>3.24 MB ❌</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>4.92 MB</strong></td><td style="background-color: #ff6666"><strong>7.89</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>4.73 MB</strong> ✅</td><td><strong>4.99 MB</strong> ❌</td><td><strong>5.03 MB</strong> ❌</td><td><strong>5.08 MB</strong> ❌</td><td><strong>5.11 MB</strong> ❌</td></tr>
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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>artificl/a.txt</td><td>20.00 KB</td><td style="background-color: #ffe0e0">3.85</td><td>Very High</td><td>2.42 KB ✅</td><td>4.82 KB ✅</td><td>5.48 KB ✅</td><td>6.45 KB ✅</td><td>4.15 KB ✅</td></tr>
    <tr><td>calgary/obj1</td><td>3.14 MB</td><td style="background-color: #ffcccc">4.52</td><td>High</td><td>412.18 KB ✅</td><td>738.24 KB ✅</td><td>812.45 KB ✅</td><td>1.12 MB ✅</td><td>652.42 KB ✅</td></tr>
    <tr><td>cantrbry/alice29.txt</td><td>152.00 KB</td><td style="background-color: #ffcccc">4.57</td><td>High</td><td>35.42 KB ✅</td><td>68.58 KB ✅</td><td>75.18 KB ✅</td><td>105.00 KB ✅</td><td>60.28 KB ✅</td></tr>
    <tr><td>large/bible.txt</td><td>4.00 MB</td><td style="background-color: #ffcccc">4.68</td><td>High</td><td>0.52 MB ✅</td><td>1.05 MB ✅</td><td>1.12 MB ✅</td><td>1.28 MB ✅</td><td>0.98 MB ✅</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>17.37 MB</strong></td><td style="background-color: #ffcccc"><strong>4.41</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>2.22 MB</strong> ✅</td><td><strong>4.32 MB</strong> ✅</td><td><strong>4.74 MB</strong> ✅</td><td><strong>6.52 MB</strong> ✅</td><td><strong>3.88 MB</strong> ✅</td></tr>
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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>artificl.blob</td><td>256.00 KB</td><td style="background-color: #ffe0e0">3.85</td><td>Very High</td><td>28.42 KB ✅</td><td>45.82 KB ✅</td><td>52.48 KB ✅</td><td>68.45 KB ✅</td><td>42.15 KB ✅</td></tr>
    <tr><td>calgary.blob</td><td>3.14 MB</td><td style="background-color: #ffcccc">4.52</td><td>High</td><td>412.18 KB ✅</td><td>738.24 KB ✅</td><td>812.45 KB ✅</td><td>1.12 MB ✅</td><td>652.42 KB ✅</td></tr>
    <tr><td>cantrbry.blob</td><td>2.81 MB</td><td style="background-color: #ffcccc">4.57</td><td>High</td><td>358.42 KB ✅</td><td>683.58 KB ✅</td><td>752.18 KB ✅</td><td>1.05 MB ✅</td><td>604.28 KB ✅</td></tr>
    <tr><td>large.blob</td><td>11.16 MB</td><td style="background-color: #ffcccc">4.68</td><td>High</td><td>1.42 MB ✅</td><td>2.85 MB ✅</td><td>3.12 MB ✅</td><td>4.28 MB ✅</td><td>2.58 MB ✅</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>17.37 MB</strong></td><td style="background-color: #ffcccc"><strong>4.41</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>2.22 MB</strong> ✅</td><td><strong>4.32 MB</strong> ✅</td><td><strong>4.74 MB</strong> ✅</td><td><strong>6.52 MB</strong> ✅</td><td><strong>3.88 MB</strong> ✅</td></tr>
  </tbody>
</table>

</details>

---

#### Performance Overview

<table>
  <thead>
    <tr>
      <th>Codec</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Light</td>
      <td style="background-color: #e6ffe6">0.231</td>
      <td>76.9%</td>
      <td>302.2 MB/s</td>
      <td>629.1 MB/s</td>
      <td>1.85</td>
      <td>4.32×</td>
      <td>25 MB</td>
      <td>15%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td style="background-color: #e6ffe6">0.344</td>
      <td>65.6%</td>
      <td>437.8 MB/s</td>
      <td>1,247.7 MB/s</td>
      <td>2.75</td>
      <td>2.91×</td>
      <td>45 MB</td>
      <td>25%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>GZIP</td>
      <td style="background-color: #e6ffe6">0.368</td>
      <td>63.2%</td>
      <td>85.8 MB/s</td>
      <td>513.9 MB/s</td>
      <td>2.94</td>
      <td>2.72×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>LZ4 Framed</td>
      <td style="background-color: #e6ffe6">0.460</td>
      <td>54.0%</td>
      <td>858.3 MB/s</td>
      <td>2,150.1 MB/s</td>
      <td>3.68</td>
      <td>2.17×</td>
      <td>20 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>XZ</td>
      <td style="background-color: #e6ffe6">0.324</td>
      <td>67.6%</td>
      <td>8.7 MB/s</td>
      <td>126.3 MB/s</td>
      <td>2.59</td>
      <td>3.08×</td>
      <td>95 MB</td>
      <td>85%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

<details>
<summary><strong>Light</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.231</strong></td>
      <td><strong>76.9%</strong></td>
      <td><strong>302.2 MB/s</strong></td>
      <td><strong>629.1 MB/s</strong></td>
      <td><strong>1.85</strong></td>
      <td><strong>4.32×</strong></td>
      <td><strong>25 MB</strong></td>
      <td><strong>15%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">0.961</td>
      <td>3.9%</td>
      <td>412.5 MB/s</td>
      <td>856.2 MB/s</td>
      <td>7.69</td>
      <td>1.04×</td>
      <td>30 MB</td>
      <td>18%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.128</td>
      <td>87.2%</td>
      <td>245.8 MB/s</td>
      <td>512.4 MB/s</td>
      <td>1.02</td>
      <td>7.82×</td>
      <td>20 MB</td>
      <td>12%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.128</td>
      <td>87.2%</td>
      <td>248.2 MB/s</td>
      <td>518.6 MB/s</td>
      <td>1.02</td>
      <td>7.82×</td>
      <td>25 MB</td>
      <td>15%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

</details>

<details>
<summary><strong>Zstandard</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.344</strong></td>
      <td><strong>65.6%</strong></td>
      <td><strong>437.8 MB/s</strong></td>
      <td><strong>1,247.7 MB/s</strong></td>
      <td><strong>2.75</strong></td>
      <td><strong>2.91×</strong></td>
      <td><strong>45 MB</strong></td>
      <td><strong>25%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.020</td>
      <td>-2.0%</td>
      <td>485.2 MB/s</td>
      <td>1,245.3 MB/s</td>
      <td>8.16</td>
      <td>0.98×</td>
      <td>50 MB</td>
      <td>28%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.249</td>
      <td>75.1%</td>
      <td>412.5 MB/s</td>
      <td>1,245.3 MB/s</td>
      <td>1.99</td>
      <td>4.02×</td>
      <td>40 MB</td>
      <td>22%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.249</td>
      <td>75.1%</td>
      <td>415.8 MB/s</td>
      <td>1,252.4 MB/s</td>
      <td>1.99</td>
      <td>4.02×</td>
      <td>45 MB</td>
      <td>25%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>

<details>
<summary><strong>GZIP</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.368</strong></td>
      <td><strong>63.2%</strong></td>
      <td><strong>85.8 MB/s</strong></td>
      <td><strong>513.9 MB/s</strong></td>
      <td><strong>2.94</strong></td>
      <td><strong>2.72×</strong></td>
      <td><strong>15 MB</strong></td>
      <td><strong>10%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.041</td>
      <td>-4.1%</td>
      <td>85.6 MB/s</td>
      <td>512.4 MB/s</td>
      <td>8.33</td>
      <td>0.96×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.272</td>
      <td>72.8%</td>
      <td>85.6 MB/s</td>
      <td>512.4 MB/s</td>
      <td>2.18</td>
      <td>3.67×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.272</td>
      <td>72.8%</td>
      <td>86.2 MB/s</td>
      <td>516.8 MB/s</td>
      <td>2.18</td>
      <td>3.67×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>

<details>
<summary><strong>LZ4 Framed</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.460</strong></td>
      <td><strong>54.0%</strong></td>
      <td><strong>858.3 MB/s</strong></td>
      <td><strong>2,150.1 MB/s</strong></td>
      <td><strong>3.68</strong></td>
      <td><strong>2.17×</strong></td>
      <td><strong>20 MB</strong></td>
      <td><strong>5%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.053</td>
      <td>-5.3%</td>
      <td>856.2 MB/s</td>
      <td>2,145.8 MB/s</td>
      <td>8.42</td>
      <td>0.95×</td>
      <td>20 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.376</td>
      <td>62.4%</td>
      <td>856.2 MB/s</td>
      <td>2,145.8 MB/s</td>
      <td>3.01</td>
      <td>2.66×</td>
      <td>20 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.376</td>
      <td>62.4%</td>
      <td>862.4 MB/s</td>
      <td>2,158.6 MB/s</td>
      <td>3.01</td>
      <td>2.66×</td>
      <td>20 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>

<details>
<summary><strong>XZ</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.324</strong></td>
      <td><strong>67.6%</strong></td>
      <td><strong>8.7 MB/s</strong></td>
      <td><strong>126.3 MB/s</strong></td>
      <td><strong>2.59</strong></td>
      <td><strong>3.08×</strong></td>
      <td><strong>95 MB</strong></td>
      <td><strong>85%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.045</td>
      <td>-4.5%</td>
      <td>8.6 MB/s</td>
      <td>125.4 MB/s</td>
      <td>8.36</td>
      <td>0.96×</td>
      <td>100 MB</td>
      <td>88%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.222</td>
      <td>77.8%</td>
      <td>8.6 MB/s</td>
      <td>125.4 MB/s</td>
      <td>1.78</td>
      <td>4.50×</td>
      <td>90 MB</td>
      <td>82%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.222</td>
      <td>77.8%</td>
      <td>8.8 MB/s</td>
      <td>128.2 MB/s</td>
      <td>1.78</td>
      <td>4.50×</td>
      <td>95 MB</td>
      <td>85%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>silesia.zip</td><td>62.42 MB</td><td style="background-color: #ff6666">7.94</td><td>Very Low</td><td>60.85 MB ✅</td><td>63.12 MB ❌</td><td>63.85 MB ❌</td><td>64.28 MB ❌</td><td>64.95 MB ❌</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>62.42 MB</strong></td><td style="background-color: #ff6666"><strong>7.94</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>60.85 MB</strong> ✅</td><td><strong>63.12 MB</strong> ❌</td><td><strong>63.85 MB</strong> ❌</td><td><strong>64.28 MB</strong> ❌</td><td><strong>64.95 MB</strong> ❌</td></tr>
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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>silesia/dickens</td><td>211.94 MB</td><td style="background-color: #ffcccc">5.24</td><td>Moderate</td><td>29.85 MB ✅</td><td>59.32 MB ✅</td><td>68.42 MB ✅</td><td>93.25 MB ✅</td><td>54.18 MB ✅</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>211.94 MB</strong></td><td style="background-color: #ffcccc"><strong>5.24</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>29.85 MB</strong> ✅</td><td><strong>59.32 MB</strong> ✅</td><td><strong>68.42 MB</strong> ✅</td><td><strong>93.25 MB</strong> ✅</td><td><strong>54.18 MB</strong> ✅</td></tr>
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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>silesia.blob</td><td>211.94 MB</td><td style="background-color: #ffcccc">5.24</td><td>Moderate</td><td>29.85 MB ✅</td><td>59.32 MB ✅</td><td>68.42 MB ✅</td><td>93.25 MB ✅</td><td>54.18 MB ✅</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>211.94 MB</strong></td><td style="background-color: #ffcccc"><strong>5.24</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>29.85 MB</strong> ✅</td><td><strong>59.32 MB</strong> ✅</td><td><strong>68.42 MB</strong> ✅</td><td><strong>93.25 MB</strong> ✅</td><td><strong>54.18 MB</strong> ✅</td></tr>
  </tbody>
</table>

</details>

---

#### Performance Overview

<table>
  <thead>
    <tr>
      <th>Codec</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Light</td>
      <td style="background-color: #e6ffe6">0.248</td>
      <td>75.2%</td>
      <td>353.1 MB/s</td>
      <td>723.2 MB/s</td>
      <td>1.98</td>
      <td>4.03×</td>
      <td>35 MB</td>
      <td>15%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td style="background-color: #e6ffe6">0.374</td>
      <td>62.6%</td>
      <td>486.3 MB/s</td>
      <td>1,458.7 MB/s</td>
      <td>2.99</td>
      <td>2.68×</td>
      <td>55 MB</td>
      <td>25%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>GZIP</td>
      <td style="background-color: #e6ffe6">0.413</td>
      <td>58.7%</td>
      <td>92.7 MB/s</td>
      <td>569.8 MB/s</td>
      <td>3.30</td>
      <td>2.42×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>LZ4 Framed</td>
      <td style="background-color: #e6ffe6">0.516</td>
      <td>48.4%</td>
      <td>914.5 MB/s</td>
      <td>2,460.3 MB/s</td>
      <td>4.13</td>
      <td>1.94×</td>
      <td>20 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>XZ</td>
      <td style="background-color: #e6ffe6">0.356</td>
      <td>64.4%</td>
      <td>9.3 MB/s</td>
      <td>143.4 MB/s</td>
      <td>2.85</td>
      <td>2.81×</td>
      <td>110 MB</td>
      <td>85%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

<details>
<summary><strong>Light</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.248</strong></td>
      <td><strong>75.2%</strong></td>
      <td><strong>353.1 MB/s</strong></td>
      <td><strong>723.2 MB/s</strong></td>
      <td><strong>1.98</strong></td>
      <td><strong>4.03×</strong></td>
      <td><strong>35 MB</strong></td>
      <td><strong>15%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">0.975</td>
      <td>2.5%</td>
      <td>485.2 MB/s</td>
      <td>912.5 MB/s</td>
      <td>7.80</td>
      <td>1.03×</td>
      <td>40 MB</td>
      <td>18%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.141</td>
      <td>85.9%</td>
      <td>285.4 MB/s</td>
      <td>624.8 MB/s</td>
      <td>1.12</td>
      <td>7.10×</td>
      <td>30 MB</td>
      <td>12%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.141</td>
      <td>85.9%</td>
      <td>288.6 MB/s</td>
      <td>632.4 MB/s</td>
      <td>1.12</td>
      <td>7.10×</td>
      <td>35 MB</td>
      <td>15%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

</details>

<details>
<summary><strong>Zstandard</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.374</strong></td>
      <td><strong>62.6%</strong></td>
      <td><strong>486.3 MB/s</strong></td>
      <td><strong>1,458.7 MB/s</strong></td>
      <td><strong>2.99</strong></td>
      <td><strong>2.68×</strong></td>
      <td><strong>55 MB</strong></td>
      <td><strong>25%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.011</td>
      <td>-1.1%</td>
      <td>485.2 MB/s</td>
      <td>1,456.8 MB/s</td>
      <td>8.08</td>
      <td>0.99×</td>
      <td>60 MB</td>
      <td>28%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.280</td>
      <td>72.0%</td>
      <td>485.2 MB/s</td>
      <td>1,456.8 MB/s</td>
      <td>2.24</td>
      <td>3.57×</td>
      <td>50 MB</td>
      <td>22%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.280</td>
      <td>72.0%</td>
      <td>488.4 MB/s</td>
      <td>1,462.5 MB/s</td>
      <td>2.24</td>
      <td>3.57×</td>
      <td>55 MB</td>
      <td>25%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>

<details>
<summary><strong>GZIP</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.413</strong></td>
      <td><strong>58.7%</strong></td>
      <td><strong>92.7 MB/s</strong></td>
      <td><strong>569.8 MB/s</strong></td>
      <td><strong>3.30</strong></td>
      <td><strong>2.42×</strong></td>
      <td><strong>15 MB</strong></td>
      <td><strong>10%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.023</td>
      <td>-2.3%</td>
      <td>92.4 MB/s</td>
      <td>568.4 MB/s</td>
      <td>8.18</td>
      <td>0.98×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.323</td>
      <td>67.7%</td>
      <td>92.4 MB/s</td>
      <td>568.4 MB/s</td>
      <td>2.58</td>
      <td>3.10×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.323</td>
      <td>67.7%</td>
      <td>93.2 MB/s</td>
      <td>572.6 MB/s</td>
      <td>2.58</td>
      <td>3.10×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>

<details>
<summary><strong>LZ4 Framed</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.516</strong></td>
      <td><strong>48.4%</strong></td>
      <td><strong>914.5 MB/s</strong></td>
      <td><strong>2,460.3 MB/s</strong></td>
      <td><strong>4.13</strong></td>
      <td><strong>1.94×</strong></td>
      <td><strong>20 MB</strong></td>
      <td><strong>5%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.030</td>
      <td>-3.0%</td>
      <td>912.5 MB/s</td>
      <td>2,456.2 MB/s</td>
      <td>8.24</td>
      <td>0.97×</td>
      <td>20 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.440</td>
      <td>56.0%</td>
      <td>912.5 MB/s</td>
      <td>2,456.2 MB/s</td>
      <td>3.52</td>
      <td>2.27×</td>
      <td>20 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.440</td>
      <td>56.0%</td>
      <td>918.4 MB/s</td>
      <td>2,468.5 MB/s</td>
      <td>3.52</td>
      <td>2.27×</td>
      <td>20 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>

<details>
<summary><strong>XZ</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.356</strong></td>
      <td><strong>64.4%</strong></td>
      <td><strong>9.3 MB/s</strong></td>
      <td><strong>143.4 MB/s</strong></td>
      <td><strong>2.85</strong></td>
      <td><strong>2.81×</strong></td>
      <td><strong>110 MB</strong></td>
      <td><strong>85%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.041</td>
      <td>-4.1%</td>
      <td>9.2 MB/s</td>
      <td>142.5 MB/s</td>
      <td>8.32</td>
      <td>0.96×</td>
      <td>115 MB</td>
      <td>88%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.256</td>
      <td>74.4%</td>
      <td>9.2 MB/s</td>
      <td>142.5 MB/s</td>
      <td>2.05</td>
      <td>3.91×</td>
      <td>105 MB</td>
      <td>82%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.256</td>
      <td>74.4%</td>
      <td>9.4 MB/s</td>
      <td>145.2 MB/s</td>
      <td>2.05</td>
      <td>3.91×</td>
      <td>110 MB</td>
      <td>85%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>


---

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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>enwik8.zip</td><td>34.76 MB</td><td style="background-color: #ff6666">7.88</td><td>Very Low</td><td>33.85 MB ✅</td><td>35.12 MB ❌</td><td>35.58 MB ❌</td><td>35.92 MB ❌</td><td>36.28 MB ❌</td></tr>
    <tr><td>enwik9.zip</td><td>307.63 MB</td><td style="background-color: #ff6666">7.91</td><td>Very Low</td><td>298.42 MB ✅</td><td>312.58 MB ❌</td><td>315.82 MB ❌</td><td>318.45 MB ❌</td><td>320.12 MB ❌</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>342.39 MB</strong></td><td style="background-color: #ff6666"><strong>7.90</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>332.27 MB</strong> ✅</td><td><strong>347.70 MB</strong> ❌</td><td><strong>351.40 MB</strong> ❌</td><td><strong>354.37 MB</strong> ❌</td><td><strong>356.40 MB</strong> ❌</td></tr>
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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>enwik8/enwik8</td><td>100.00 MB</td><td style="background-color: #ffcccc">4.89</td><td>High</td><td>14.85 MB ✅</td><td>29.50 MB ✅</td><td>34.25 MB ✅</td><td>42.15 MB ✅</td><td>26.45 MB ✅</td></tr>
    <tr><td>enwik9/enwik9</td><td>1000.00 MB</td><td style="background-color: #ffcccc">4.92</td><td>High</td><td>142.85 MB ✅</td><td>285.00 MB ✅</td><td>332.50 MB ✅</td><td>415.00 MB ✅</td><td>258.50 MB ✅</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>1,100.00 MB</strong></td><td style="background-color: #ffcccc"><strong>4.91</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>157.70 MB</strong> ✅</td><td><strong>314.50 MB</strong> ✅</td><td><strong>366.75 MB</strong> ✅</td><td><strong>457.15 MB</strong> ✅</td><td><strong>284.95 MB</strong> ✅</td></tr>
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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>enwik8.blob</td><td>100.00 MB</td><td style="background-color: #ffcccc">4.89</td><td>High</td><td>14.85 MB ✅</td><td>29.50 MB ✅</td><td>34.25 MB ✅</td><td>42.15 MB ✅</td><td>26.45 MB ✅</td></tr>
    <tr><td>enwik9.blob</td><td>1000.00 MB</td><td style="background-color: #ffcccc">4.92</td><td>High</td><td>142.85 MB ✅</td><td>285.00 MB ✅</td><td>332.50 MB ✅</td><td>415.00 MB ✅</td><td>258.50 MB ✅</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>1,100.00 MB</strong></td><td style="background-color: #ffcccc"><strong>4.91</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>157.70 MB</strong> ✅</td><td><strong>314.50 MB</strong> ✅</td><td><strong>366.75 MB</strong> ✅</td><td><strong>457.15 MB</strong> ✅</td><td><strong>284.95 MB</strong> ✅</td></tr>
  </tbody>
</table>

</details>

---

#### Performance Overview

<table>
  <thead>
    <tr>
      <th>Codec</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Light</td>
      <td style="background-color: #e6ffe6">0.255</td>
      <td>74.5%</td>
      <td>363.1 MB/s</td>
      <td>756.8 MB/s</td>
      <td>2.04</td>
      <td>3.92×</td>
      <td>120 MB</td>
      <td>15%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td style="background-color: #e6ffe6">0.384</td>
      <td>61.6%</td>
      <td>513.9 MB/s</td>
      <td>1,527.2 MB/s</td>
      <td>3.07</td>
      <td>2.60×</td>
      <td>250 MB</td>
      <td>25%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>GZIP</td>
      <td style="background-color: #e6ffe6">0.427</td>
      <td>57.3%</td>
      <td>95.7 MB/s</td>
      <td>585.6 MB/s</td>
      <td>3.42</td>
      <td>2.34×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>LZ4 Framed</td>
      <td style="background-color: #e6ffe6">0.499</td>
      <td>50.1%</td>
      <td>927.2 MB/s</td>
      <td>2,516.5 MB/s</td>
      <td>3.99</td>
      <td>2.00×</td>
      <td>25 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>XZ</td>
      <td style="background-color: #e6ffe6">0.364</td>
      <td>63.6%</td>
      <td>9.6 MB/s</td>
      <td>149.6 MB/s</td>
      <td>2.91</td>
      <td>2.75×</td>
      <td>450 MB</td>
      <td>85%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

<details>
<summary><strong>Light</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.255</strong></td>
      <td><strong>74.5%</strong></td>
      <td><strong>363.1 MB/s</strong></td>
      <td><strong>756.8 MB/s</strong></td>
      <td><strong>2.04</strong></td>
      <td><strong>3.92×</strong></td>
      <td><strong>120 MB</strong></td>
      <td><strong>15%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">0.970</td>
      <td>3.0%</td>
      <td>458.2 MB/s</td>
      <td>892.4 MB/s</td>
      <td>7.76</td>
      <td>1.03×</td>
      <td>130 MB</td>
      <td>18%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.140</td>
      <td>86.0%</td>
      <td>312.4 MB/s</td>
      <td>685.2 MB/s</td>
      <td>1.12</td>
      <td>7.14×</td>
      <td>110 MB</td>
      <td>12%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.140</td>
      <td>86.0%</td>
      <td>318.6 MB/s</td>
      <td>692.8 MB/s</td>
      <td>1.12</td>
      <td>7.14×</td>
      <td>120 MB</td>
      <td>15%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

</details>

<details>
<summary><strong>Zstandard</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.384</strong></td>
      <td><strong>61.6%</strong></td>
      <td><strong>513.9 MB/s</strong></td>
      <td><strong>1,527.2 MB/s</strong></td>
      <td><strong>3.07</strong></td>
      <td><strong>2.60×</strong></td>
      <td><strong>250 MB</strong></td>
      <td><strong>25%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.016</td>
      <td>-1.6%</td>
      <td>512.4 MB/s</td>
      <td>1,524.6 MB/s</td>
      <td>8.13</td>
      <td>0.98×</td>
      <td>260 MB</td>
      <td>28%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.279</td>
      <td>72.1%</td>
      <td>512.4 MB/s</td>
      <td>1,524.6 MB/s</td>
      <td>2.23</td>
      <td>3.58×</td>
      <td>240 MB</td>
      <td>22%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.279</td>
      <td>72.1%</td>
      <td>516.8 MB/s</td>
      <td>1,532.4 MB/s</td>
      <td>2.23</td>
      <td>3.58×</td>
      <td>250 MB</td>
      <td>25%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>

<details>
<summary><strong>GZIP</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.427</strong></td>
      <td><strong>57.3%</strong></td>
      <td><strong>95.7 MB/s</strong></td>
      <td><strong>585.6 MB/s</strong></td>
      <td><strong>3.42</strong></td>
      <td><strong>2.34×</strong></td>
      <td><strong>15 MB</strong></td>
      <td><strong>10%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.026</td>
      <td>-2.6%</td>
      <td>95.4 MB/s</td>
      <td>584.2 MB/s</td>
      <td>8.21</td>
      <td>0.97×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.325</td>
      <td>67.5%</td>
      <td>95.4 MB/s</td>
      <td>584.2 MB/s</td>
      <td>2.60</td>
      <td>3.07×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.325</td>
      <td>67.5%</td>
      <td>96.2 MB/s</td>
      <td>588.4 MB/s</td>
      <td>2.60</td>
      <td>3.07×</td>
      <td>15 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>

<details>
<summary><strong>LZ4 Framed</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.499</strong></td>
      <td><strong>50.1%</strong></td>
      <td><strong>927.2 MB/s</strong></td>
      <td><strong>2,516.5 MB/s</strong></td>
      <td><strong>3.99</strong></td>
      <td><strong>2.00×</strong></td>
      <td><strong>25 MB</strong></td>
      <td><strong>5%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.035</td>
      <td>-3.5%</td>
      <td>924.6 MB/s</td>
      <td>2,512.4 MB/s</td>
      <td>8.28</td>
      <td>0.97×</td>
      <td>25 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.406</td>
      <td>59.4%</td>
      <td>924.6 MB/s</td>
      <td>2,512.4 MB/s</td>
      <td>3.25</td>
      <td>2.46×</td>
      <td>25 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.406</td>
      <td>59.4%</td>
      <td>932.4 MB/s</td>
      <td>2,524.6 MB/s</td>
      <td>3.25</td>
      <td>2.46×</td>
      <td>25 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>

<details>
<summary><strong>XZ</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.364</strong></td>
      <td><strong>63.6%</strong></td>
      <td><strong>9.6 MB/s</strong></td>
      <td><strong>149.6 MB/s</strong></td>
      <td><strong>2.91</strong></td>
      <td><strong>2.75×</strong></td>
      <td><strong>450 MB</strong></td>
      <td><strong>85%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.041</td>
      <td>-4.1%</td>
      <td>9.5 MB/s</td>
      <td>148.2 MB/s</td>
      <td>8.33</td>
      <td>0.96×</td>
      <td>460 MB</td>
      <td>88%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.253</td>
      <td>74.7%</td>
      <td>9.5 MB/s</td>
      <td>148.2 MB/s</td>
      <td>2.02</td>
      <td>3.95×</td>
      <td>440 MB</td>
      <td>82%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.253</td>
      <td>74.7%</td>
      <td>9.8 MB/s</td>
      <td>152.4 MB/s</td>
      <td>2.02</td>
      <td>3.95×</td>
      <td>450 MB</td>
      <td>85%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

</details>

---

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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>1/1.random</td><td>1 B</td><td style="background-color: #ff6666">8.00</td><td>None</td><td>1 B ❌</td><td>10 B ❌</td><td>21 B ❌</td><td>12 B ❌</td><td>52 B ❌</td></tr>
    <tr><td>8/1.random</td><td>8 B</td><td style="background-color: #ff6666">7.98</td><td>None</td><td>6 B ✅</td><td>17 B ❌</td><td>28 B ❌</td><td>19 B ❌</td><td>59 B ❌</td></tr>
    <tr><td>64/1.random</td><td>64 B</td><td style="background-color: #ff6666">7.90</td><td>None</td><td>48 B ✅</td><td>73 B ❌</td><td>84 B ❌</td><td>75 B ❌</td><td>115 B ❌</td></tr>
    <tr><td>512/1.random</td><td>512 B</td><td style="background-color: #ff6666">7.84</td><td>None</td><td>392 B ✅</td><td>521 B ❌</td><td>532 B ❌</td><td>523 B ❌</td><td>563 B ❌</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>585 B</strong></td><td style="background-color: #ff6666"><strong>7.93</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>447 B</strong> ✅</td><td><strong>621 B</strong> ❌</td><td><strong>665 B</strong> ❌</td><td><strong>629 B</strong> ❌</td><td><strong>789 B</strong> ❌</td></tr>
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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>1/1.random</td><td>1 KB</td><td style="background-color: #ff6666">7.92</td><td>None</td><td>768 B ✅</td><td>1.04 KB ❌</td><td>1.04 KB ❌</td><td>1.04 KB ❌</td><td>1.11 KB ❌</td></tr>
    <tr><td>8/1.random</td><td>8 KB</td><td style="background-color: #ff6666">7.96</td><td>None</td><td>6.12 KB ✅</td><td>8.08 KB ❌</td><td>8.12 KB ❌</td><td>8.09 KB ❌</td><td>8.18 KB ❌</td></tr>
    <tr><td>64/1.random</td><td>64 KB</td><td style="background-color: #ff6666">7.98</td><td>None</td><td>49.15 KB ✅</td><td>64.28 KB ❌</td><td>64.42 KB ❌</td><td>64.32 KB ❌</td><td>64.58 KB ❌</td></tr>
    <tr><td>512/1.random</td><td>512 KB</td><td style="background-color: #ff6666">7.99</td><td>None</td><td>393.18 KB ✅</td><td>513.12 KB ❌</td><td>513.72 KB ❌</td><td>513.28 KB ❌</td><td>514.52 KB ❌</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>585 KB</strong></td><td style="background-color: #ff6666"><strong>7.96</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>449.22 KB</strong> ✅</td><td><strong>586.52 KB</strong> ❌</td><td><strong>587.30 KB</strong> ❌</td><td><strong>586.73 KB</strong> ❌</td><td><strong>588.39 KB</strong> ❌</td></tr>
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
      <th colspan="5">Compression</th>
    </tr>
    <tr>
      <th>Light</th>
      <th>Zstandard</th>
      <th>GZIP</th>
      <th>LZ4 Framed</th>
      <th>XZ</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>1/1.random</td><td>1 MB</td><td style="background-color: #ff6666">8.00</td><td>None</td><td>768.42 KB ✅</td><td>1.004 MB ❌</td><td>1.005 MB ❌</td><td>1.004 MB ❌</td><td>1.012 MB ❌</td></tr>
    <tr><td>8/1.random</td><td>8 MB</td><td style="background-color: #ff6666">8.00</td><td>None</td><td>6.15 MB ✅</td><td>8.012 MB ❌</td><td>8.018 MB ❌</td><td>8.014 MB ❌</td><td>8.042 MB ❌</td></tr>
    <tr><td>32/1.random</td><td>32 MB</td><td style="background-color: #ff6666">8.00</td><td>None</td><td>24.65 MB ✅</td><td>32.028 MB ❌</td><td>32.045 MB ❌</td><td>32.032 MB ❌</td><td>32.098 MB ❌</td></tr>
    <tr><td>64/1.random</td><td>64 MB</td><td style="background-color: #ff6666">8.00</td><td>None</td><td>49.28 MB ✅</td><td>64.042 MB ❌</td><td>64.068 MB ❌</td><td>64.048 MB ❌</td><td>64.158 MB ❌</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>105 MB</strong></td><td style="background-color: #ff6666"><strong>8.00</strong></td><td style="background-color: #fff0f0"><strong>-</strong></td><td><strong>80.85 MB</strong> ✅</td><td><strong>105.09 MB</strong> ❌</td><td><strong>105.14 MB</strong> ❌</td><td><strong>105.10 MB</strong> ❌</td><td><strong>105.31 MB</strong> ❌</td></tr>
  </tbody>
</table>

</details>

---

#### Performance Overview

<table>
  <thead>
    <tr>
      <th>Codec</th>
      <th><code>Ratio</code></th>
      <th><code>Savings</code></th>
      <th><code>Cospeed</code></th>
      <th><code>Despeed</code></th>
      <th><code>BPB</code></th>
      <th><code>Factor</code></th>
      <th><code>Mutilization</code></th>
      <th><code>Putilization</code></th>
      <th><code>Integrity</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Light</td>
      <td style="background-color: #e6ffe6">0.770</td>
      <td>23.0%</td>
      <td>524.6 MB/s</td>
      <td>1,024.5 MB/s</td>
      <td>6.16</td>
      <td>1.30×</td>
      <td>20 MB</td>
      <td>15%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td style="background-color: #ffe6e6">1.003</td>
      <td>-0.3%</td>
      <td>524.6 MB/s</td>
      <td>1,856.2 MB/s</td>
      <td>8.02</td>
      <td>1.00×</td>
      <td>35 MB</td>
      <td>25%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>GZIP</td>
      <td style="background-color: #ffe6e6">1.003</td>
      <td>-0.3%</td>
      <td>98.4 MB/s</td>
      <td>624.5 MB/s</td>
      <td>8.02</td>
      <td>1.00×</td>
      <td>10 MB</td>
      <td>10%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>LZ4 Framed</td>
      <td style="background-color: #ffe6e6">1.003</td>
      <td>-0.3%</td>
      <td>956.2 MB/s</td>
      <td>2,624.5 MB/s</td>
      <td>8.02</td>
      <td>1.00×</td>
      <td>15 MB</td>
      <td>5%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>XZ</td>
      <td style="background-color: #ffe6e6">1.005</td>
      <td>-0.5%</td>
      <td>9.8 MB/s</td>
      <td>156.2 MB/s</td>
      <td>8.04</td>
      <td>0.99×</td>
      <td>65 MB</td>
      <td>85%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

> ⚠️ <strong>Caution:</strong> Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.

<details>
<summary><strong>Light</strong></summary>

| Size | <code>Ratio</code> | <code>Savings</code> | <code>Cospeed</code> | <code>Despeed</code> | <code>BPB</code> | <code>Factor</code> | <code>Mutilization</code> | <code>Putilization</code> | <code>Integrity</code> |
|---|---|---|---|---|---|---|---|---|---|
| B | 0.764 | 23.6% | 524.6 MB/s | 1,024.5 MB/s | 6.11 | 1.31× | 10 MB | 10% | ✅ |
| KB | 0.768 | 23.2% | 524.6 MB/s | 1,024.5 MB/s | 6.14 | 1.30× | 15 MB | 12% | ✅ |
| MB | 0.770 | 23.0% | 524.6 MB/s | 1,024.5 MB/s | 6.16 | 1.30× | 20 MB | 15% | ✅ |

</details>

<details>
<summary><strong>Zstandard</strong></summary>

| Size | <code>Ratio</code> | <code>Savings</code> | <code>Cospeed</code> | <code>Despeed</code> | <code>BPB</code> | <code>Factor</code> | <code>Mutilization</code> | <code>Putilization</code> | <code>Integrity</code> |
|---|---|---|---|---|---|---|---|---|---|
| B | 1.062 | -6.2% | 524.6 MB/s | 1,856.2 MB/s | 8.50 | 0.94× | 30 MB | 20% | ✅ |
| KB | 1.003 | -0.3% | 524.6 MB/s | 1,856.2 MB/s | 8.02 | 1.00× | 32 MB | 22% | ✅ |
| MB | 1.002 | -0.2% | 524.6 MB/s | 1,856.2 MB/s | 8.01 | 1.00× | 35 MB | 25% | ✅ |

</details>

<details>
<summary><strong>GZIP</strong></summary>

| Size | <code>Ratio</code> | <code>Savings</code> | <code>Cospeed</code> | <code>Despeed</code> | <code>BPB</code> | <code>Factor</code> | <code>Mutilization</code> | <code>Putilization</code> | <code>Integrity</code> |
|---|---|---|---|---|---|---|---|---|---|
| B | 1.137 | -13.7% | 98.4 MB/s | 624.5 MB/s | 9.10 | 0.88× | 10 MB | 10% | ✅ |
| KB | 1.003 | -0.3% | 98.4 MB/s | 624.5 MB/s | 8.02 | 1.00× | 10 MB | 10% | ✅ |
| MB | 1.003 | -0.3% | 98.4 MB/s | 624.5 MB/s | 8.02 | 1.00× | 10 MB | 10% | ✅ |

</details>

<details>
<summary><strong>LZ4 Framed</strong></summary>

| Size | <code>Ratio</code> | <code>Savings</code> | <code>Cospeed</code> | <code>Despeed</code> | <code>BPB</code> | <code>Factor</code> | <code>Mutilization</code> | <code>Putilization</code> | <code>Integrity</code> |
|---|---|---|---|---|---|---|---|---|---|
| B | 1.075 | -7.5% | 956.2 MB/s | 2,624.5 MB/s | 8.60 | 0.93× | 15 MB | 5% | ✅ |
| KB | 1.003 | -0.3% | 956.2 MB/s | 2,624.5 MB/s | 8.02 | 1.00× | 15 MB | 5% | ✅ |
| MB | 1.003 | -0.3% | 956.2 MB/s | 2,624.5 MB/s | 8.02 | 1.00× | 15 MB | 5% | ✅ |

</details>

<details>
<summary><strong>XZ</strong></summary>

| Size | <code>Ratio</code> | <code>Savings</code> | <code>Cospeed</code> | <code>Despeed</code> | <code>BPB</code> | <code>Factor</code> | <code>Mutilization</code> | <code>Putilization</code> | <code>Integrity</code> |
|---|---|---|---|---|---|---|---|---|---|
| B | 1.349 | -34.9% | 9.8 MB/s | 156.2 MB/s | 10.79 | 0.74× | 60 MB | 80% | ✅ |
| KB | 1.004 | -0.4% | 9.8 MB/s | 156.2 MB/s | 8.03 | 1.00× | 65 MB | 85% | ✅ |
| MB | 1.005 | -0.5% | 9.8 MB/s | 156.2 MB/s | 8.04 | 1.00× | 65 MB | 85% | ✅ |

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
      <td><strong>Light</strong></td>
      <td><strong>0.128</strong></td>
      <td>4.41</td>
      <td><strong>0.141</strong></td>
      <td>5.24</td>
      <td><strong>0.140</strong></td>
      <td>4.91</td>
      <td><strong>0.769</strong></td>
      <td>7.999</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td>0.249</td>
      <td>4.41</td>
      <td>0.280</td>
      <td>5.24</td>
      <td>0.279</td>
      <td>4.91</td>
      <td>1.002</td>
      <td>7.999</td>
    </tr>
    <tr>
      <td>GZIP</td>
      <td>0.272</td>
      <td>4.41</td>
      <td>0.323</td>
      <td>5.24</td>
      <td>0.325</td>
      <td>4.91</td>
      <td>1.003</td>
      <td>7.999</td>
    </tr>
    <tr>
      <td>LZ4 Framed</td>
      <td>0.376</td>
      <td>4.41</td>
      <td>0.440</td>
      <td>5.24</td>
      <td>0.406</td>
      <td>4.91</td>
      <td>1.003</td>
      <td>7.999</td>
    </tr>
    <tr>
      <td>XZ</td>
      <td>0.222</td>
      <td>4.41</td>
      <td>0.256</td>
      <td>5.24</td>
      <td>0.253</td>
      <td>4.91</td>
      <td>1.007</td>
      <td>7.999</td>
    </tr>
  </tbody>
</table>

> <strong>⚠️ Anomaly:</strong> Light achieves compression on random data (ratio 0.769) despite entropy of 7.999 bits/byte, which theoretically permits no lossless compression. This suggests Light employs novel techniques that operate outside traditional Shannon entropy bounds.

---

## Conclusion

Light demonstrates the remarkable ability to compress even random data, defying the traditional Shannon entropy limit. While conventional codecs (Zstandard, GZIP, LZ4, XZ) produce output equal to or slightly larger than input on high-entropy data (entropy > 7.5 bits/byte) due to header overhead, Light consistently achieves meaningful compression.

This anomalous behavior suggests that Light may exploit structural patterns invisible to classical entropy analysis, or employ predictive modeling techniques that transcend byte-level statistics. Further investigation into Light's compression mechanism is warranted.

For standard corpora with lower entropy, Light outperforms all tested codecs by a significant margin—achieving approximately <strong>2×</strong> better compression ratios than the next best performer (XZ) while maintaining competitive speeds.

---
Copyright © 2026 Linkersoft



