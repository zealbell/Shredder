# Light Benchmarking Report

> **Report Generated:** February 17, 2026 00:17:47  
> **Benchmark Suite:** V 1.0  
> **Total Codecs Tested:** 3 (in 0hrs 8min 59secs)  
> **Platform:** Mac OS X, Java 24.0.2
>> <details>
>> <summary>Device</summary>
>> <ul>
>> <li><strong>RAM:</strong> 16 GiB | 2.1 GHz</li>
>> <li><strong>CPU:</strong> Intel(R) Core(TM) i7-7567U CPU @ 3.50GHz | 2-CORES/4-THREADS | 3.5 GHz</li>
>> <li><strong>GPU:</strong> Intel Iris Plus Graphics 650 | 1.5 GiB</li>
>> <li><strong>MOTHERBOARD:</strong> Apple Inc. Mac-CAD6701F7CEA0921</li>
>> <li><strong>STORAGE:</strong> APPLE SSD AP0256J 233.8 GiB, APPLE SSD AP0256J 233.5 GiB</li>
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
      <td>780.5 MB/s</td>
    </tr>
    <tr>
      <td><strong>Fastest Decompression</strong></td>
      <td>Light</td>
      <td>1,027.3 MB/s</td>
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
      <td>[header (10B)] + [payload (3260898B)] + [trailer (8B)]</td>
      <td>DEFLATE algorithm with sliding window.</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td>zstd-jni</td>
      <td>1.5.5-11</td>
      <td>Yes</td>
      <td>[magic (4B)] + [frame_header (2-14B)] + [blocks (variable)] + [checksum (0-4B)]</td>
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
    <tr><td>artificl.zip</td><td>74.66 KB</td><td style="background-color: #ff6666">8.00</td><td>None</td><td>74.66 KB ❌</td><td>74.33 KB ✅</td><td>74.67 KB ❌</td></tr>
    <tr><td>calgary.zip</td><td>1.02 MB</td><td style="background-color: #ff6666">8.00</td><td>None</td><td>1.02 MB ❌</td><td>1.02 MB ✅</td><td>1.02 MB ✅</td></tr>
    <tr><td>cantrbry.zip</td><td>716.77 KB</td><td style="background-color: #ff6666">7.96</td><td>None</td><td>716.77 KB ❌</td><td>614.17 KB ✅</td><td>623.80 KB ✅</td></tr>
    <tr><td>large.zip</td><td>3.11 MB</td><td style="background-color: #ff6666">8.00</td><td>None</td><td>3.11 MB ❌</td><td>3.11 MB ❌</td><td>3.11 MB ❌</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>4.90 MB</strong></td><td style="background-color: #ff6666"><strong>7.99</strong></td><td><strong>-</strong></td><td><strong>4.90 MB</strong></td><td><strong>4.80 MB</strong></td><td><strong>4.80 MB</strong></td></tr>
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
    <tr><td>artificl/a.txt</td><td>1 B</td><td style="background-color: #fff0f0">0.00</td><td>Excellent</td><td>1 B ❌</td><td>21 B ❌</td><td>10 B ❌</td></tr>
    <tr><td>artificl/aaa.txt</td><td>97.66 KB</td><td style="background-color: #fff0f0">0.00</td><td>Excellent</td><td>97.66 KB ❌</td><td>133 B ✅</td><td>19 B ✅</td></tr>
    <tr><td>artificl/alphabet.txt</td><td>97.66 KB</td><td style="background-color: #ffcccc">4.70</td><td>High</td><td>97.66 KB ❌</td><td>302 B ✅</td><td>43 B ✅</td></tr>
    <tr><td>artificl/random.txt</td><td>97.66 KB</td><td style="background-color: #ffcccc">6.00</td><td>Moderate</td><td>97.66 KB ❌</td><td>73.97 KB ✅</td><td>73.29 KB ✅</td></tr>
    <tr><td>calgary/bib</td><td>108.65 KB</td><td style="background-color: #ffcccc">5.20</td><td>Moderate</td><td>108.65 KB ❌</td><td>34.41 KB ✅</td><td>36.04 KB ✅</td></tr>
    <tr><td>calgary/book1</td><td>750.75 KB</td><td style="background-color: #ffcccc">4.53</td><td>High</td><td>750.75 KB ❌</td><td>306.24 KB ✅</td><td>298.38 KB ✅</td></tr>
    <tr><td>calgary/book2</td><td>596.54 KB</td><td style="background-color: #ffcccc">4.79</td><td>High</td><td>596.54 KB ❌</td><td>201.83 KB ✅</td><td>199.16 KB ✅</td></tr>
    <tr><td>calgary/geo</td><td>100.00 KB</td><td style="background-color: #ffcccc">5.65</td><td>Moderate</td><td>100.00 KB ❌</td><td>66.84 KB ✅</td><td>67.21 KB ✅</td></tr>
    <tr><td>calgary/news</td><td>368.27 KB</td><td style="background-color: #ffcccc">5.19</td><td>Moderate</td><td>368.27 KB ❌</td><td>141.42 KB ✅</td><td>134.78 KB ✅</td></tr>
    <tr><td>calgary/obj1</td><td>21.00 KB</td><td style="background-color: #ffcccc">5.95</td><td>Moderate</td><td>21.00 KB ❌</td><td>10.09 KB ✅</td><td>10.52 KB ✅</td></tr>
    <tr><td>calgary/obj2</td><td>241.03 KB</td><td style="background-color: #ff9999">6.26</td><td>Low</td><td>241.03 KB ❌</td><td>79.61 KB ✅</td><td>82.36 KB ✅</td></tr>
    <tr><td>calgary/paper1</td><td>51.92 KB</td><td style="background-color: #ffcccc">4.98</td><td>High</td><td>51.92 KB ❌</td><td>18.13 KB ✅</td><td>19.02 KB ✅</td></tr>
    <tr><td>calgary/paper2</td><td>80.27 KB</td><td style="background-color: #ffcccc">4.60</td><td>High</td><td>80.27 KB ❌</td><td>29.07 KB ✅</td><td>29.85 KB ✅</td></tr>
    <tr><td>calgary/paper3</td><td>45.44 KB</td><td style="background-color: #ffcccc">4.67</td><td>High</td><td>45.44 KB ❌</td><td>17.67 KB ✅</td><td>18.26 KB ✅</td></tr>
    <tr><td>calgary/paper4</td><td>12.97 KB</td><td style="background-color: #ffcccc">4.70</td><td>High</td><td>12.97 KB ❌</td><td>5.40 KB ✅</td><td>5.52 KB ✅</td></tr>
    <tr><td>calgary/paper5</td><td>11.67 KB</td><td style="background-color: #ffcccc">4.94</td><td>High</td><td>11.67 KB ❌</td><td>4.87 KB ✅</td><td>5.08 KB ✅</td></tr>
    <tr><td>calgary/paper6</td><td>37.21 KB</td><td style="background-color: #ffcccc">5.01</td><td>Moderate</td><td>37.21 KB ❌</td><td>13.00 KB ✅</td><td>13.69 KB ✅</td></tr>
    <tr><td>calgary/pic</td><td>501.19 KB</td><td style="background-color: #fff0f0">1.21</td><td>Excellent</td><td>501.19 KB ❌</td><td>55.15 KB ✅</td><td>53.17 KB ✅</td></tr>
    <tr><td>calgary/progc</td><td>38.68 KB</td><td style="background-color: #ffcccc">5.20</td><td>Moderate</td><td>38.68 KB ❌</td><td>13.04 KB ✅</td><td>13.80 KB ✅</td></tr>
    <tr><td>calgary/progl</td><td>69.97 KB</td><td style="background-color: #ffcccc">4.77</td><td>High</td><td>69.97 KB ❌</td><td>15.89 KB ✅</td><td>17.17 KB ✅</td></tr>
    <tr><td>calgary/progp</td><td>48.22 KB</td><td style="background-color: #ffcccc">4.87</td><td>High</td><td>48.22 KB ❌</td><td>10.98 KB ✅</td><td>11.86 KB ✅</td></tr>
    <tr><td>calgary/trans</td><td>91.50 KB</td><td style="background-color: #ffcccc">5.53</td><td>Moderate</td><td>91.50 KB ❌</td><td>18.61 KB ✅</td><td>20.08 KB ✅</td></tr>
    <tr><td>cantrbry/alice29.txt</td><td>148.52 KB</td><td style="background-color: #ffcccc">4.57</td><td>High</td><td>148.52 KB ❌</td><td>53.14 KB ✅</td><td>54.52 KB ✅</td></tr>
    <tr><td>cantrbry/asyoulik.txt</td><td>122.25 KB</td><td style="background-color: #ffcccc">4.81</td><td>High</td><td>122.25 KB ❌</td><td>47.76 KB ✅</td><td>48.85 KB ✅</td></tr>
    <tr><td>cantrbry/cp.html</td><td>24.03 KB</td><td style="background-color: #ffcccc">5.23</td><td>Moderate</td><td>24.03 KB ❌</td><td>7.79 KB ✅</td><td>8.27 KB ✅</td></tr>
    <tr><td>cantrbry/fields.c</td><td>10.89 KB</td><td style="background-color: #ffcccc">5.01</td><td>Moderate</td><td>10.89 KB ❌</td><td>3.06 KB ✅</td><td>3.33 KB ✅</td></tr>
    <tr><td>cantrbry/grammar.lsp</td><td>3.63 KB</td><td style="background-color: #ffcccc">4.63</td><td>High</td><td>3.63 KB ❌</td><td>1.21 KB ✅</td><td>1.27 KB ✅</td></tr>
    <tr><td>cantrbry/kennedy.xls</td><td>1005.61 KB</td><td style="background-color: #ffe0e0">3.57</td><td>Very High</td><td>1005.61 KB ❌</td><td>199.22 KB ✅</td><td>109.12 KB ✅</td></tr>
    <tr><td>cantrbry/lcet10.txt</td><td>416.75 KB</td><td style="background-color: #ffcccc">4.67</td><td>High</td><td>416.75 KB ❌</td><td>141.52 KB ✅</td><td>137.88 KB ✅</td></tr>
    <tr><td>cantrbry/plrabn12.txt</td><td>470.57 KB</td><td style="background-color: #ffcccc">4.53</td><td>High</td><td>470.57 KB ❌</td><td>190.70 KB ✅</td><td>187.28 KB ✅</td></tr>
    <tr><td>cantrbry/ptt5</td><td>501.19 KB</td><td style="background-color: #fff0f0">1.21</td><td>Excellent</td><td>501.19 KB ❌</td><td>55.15 KB ✅</td><td>53.17 KB ✅</td></tr>
    <tr><td>cantrbry/sum</td><td>37.34 KB</td><td style="background-color: #ffcccc">5.33</td><td>Moderate</td><td>37.34 KB ❌</td><td>12.70 KB ✅</td><td>13.08 KB ✅</td></tr>
    <tr><td>cantrbry/xargs.1</td><td>4.13 KB</td><td style="background-color: #ffcccc">4.90</td><td>High</td><td>4.13 KB ❌</td><td>1.71 KB ✅</td><td>1.79 KB ✅</td></tr>
    <tr><td>large/E.coli</td><td>4.42 MB</td><td style="background-color: #fff0f0">2.00</td><td>Excellent</td><td>4.42 MB ❌</td><td>1.28 MB ✅</td><td>1.34 MB ✅</td></tr>
    <tr><td>large/bible.txt</td><td>3.86 MB</td><td style="background-color: #ffcccc">4.34</td><td>High</td><td>3.86 MB ❌</td><td>1.14 MB ✅</td><td>1.12 MB ✅</td></tr>
    <tr><td>large/world192.txt</td><td>2.36 MB</td><td style="background-color: #ffcccc">5.00</td><td>High</td><td>2.36 MB ❌</td><td>707.99 KB ✅</td><td>642.31 KB ✅</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>16.71 MB</strong></td><td style="background-color: #ffcccc"><strong>4.40</strong></td><td><strong>-</strong></td><td><strong>16.71 MB</strong></td><td><strong>4.90 MB</strong></td><td><strong>4.77 MB</strong></td></tr>
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
    <tr><td>artificl.blob</td><td>293.03 KB</td><td style="background-color: #ffcccc">4.64</td><td>High</td><td>293.03 KB ❌</td><td>74.98 KB ✅</td><td>73.53 KB ✅</td></tr>
    <tr><td>calgary.blob</td><td>3.10 MB</td><td style="background-color: #ffcccc">5.36</td><td>Moderate</td><td>3.10 MB ❌</td><td>1.02 MB ✅</td><td>1.02 MB ✅</td></tr>
    <tr><td>cantrbry.blob</td><td>2.68 MB</td><td style="background-color: #ffcccc">4.71</td><td>High</td><td>2.68 MB ❌</td><td>716.60 KB ✅</td><td>625.41 KB ✅</td></tr>
    <tr><td>large.blob</td><td>10.64 MB</td><td style="background-color: #ffcccc">4.23</td><td>High</td><td>10.64 MB ❌</td><td>3.11 MB ✅</td><td>3.10 MB ✅</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>16.71 MB</strong></td><td style="background-color: #ffcccc"><strong>4.74</strong></td><td><strong>-</strong></td><td><strong>16.71 MB</strong></td><td><strong>4.90 MB</strong></td><td><strong>4.80 MB</strong></td></tr>
  </tbody>
</table>

</details>

---

#### Performance Overview

<table>
  <thead>
    <tr>
      <th>Codec</th>
      <th>Ratio</th>
      <th>Savings</th>
      <th>Cospeed</th>
      <th>Despeed</th>
      <th>BPB</th>
      <th>Factor</th>
      <th>Mutilization</th>
      <th>Putilization</th>
      <th>Integrity</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Light</td>
      <td style="background-color: #ffe6e6">1.000</td>
      <td>0.0%</td>
      <td>171.3 MB/s</td>
      <td>172.1 MB/s</td>
      <td>8.00</td>
      <td>1.00×</td>
      <td>65 MB</td>
      <td>81%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>GZIP</td>
      <td style="background-color: #ffe6e6">0.851</td>
      <td>14.9%</td>
      <td>5.0 MB/s</td>
      <td>34.8 MB/s</td>
      <td>6.81</td>
      <td>1.18×</td>
      <td>65 MB</td>
      <td>50%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td style="background-color: #e6ffe6">0.604</td>
      <td>39.6%</td>
      <td>28.1 MB/s</td>
      <td>50.2 MB/s</td>
      <td>4.83</td>
      <td>1.66×</td>
      <td>65 MB</td>
      <td>66%</td>
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
      <th>Ratio</th>
      <th>Savings</th>
      <th>Cospeed</th>
      <th>Despeed</th>
      <th>BPB</th>
      <th>Factor</th>
      <th>Mutilization</th>
      <th>Putilization</th>
      <th>Integrity</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #ffe6e6"><strong>1.000</strong></td>
      <td><strong>0.0%</strong></td>
      <td><strong>171.3 MB/s</strong></td>
      <td><strong>172.1 MB/s</strong></td>
      <td><strong>8.00</strong></td>
      <td><strong>1.00×</strong></td>
      <td><strong>65 MB</strong></td>
      <td><strong>81%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">1.000</td>
      <td>0.0%</td>
      <td>287.9 MB/s</td>
      <td>441.0 MB/s</td>
      <td>8.00</td>
      <td>1.00×</td>
      <td>65 MB</td>
      <td>68%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #ffe6e6">1.000</td>
      <td>0.0%</td>
      <td>122.1 MB/s</td>
      <td>136.4 MB/s</td>
      <td>8.00</td>
      <td>1.00×</td>
      <td>65 MB</td>
      <td>85%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #ffe6e6">1.000</td>
      <td>0.0%</td>
      <td>497.4 MB/s</td>
      <td>224.9 MB/s</td>
      <td>8.00</td>
      <td>1.00×</td>
      <td>65 MB</td>
      <td>55%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

</details>

<details>
<summary><strong>GZIP</strong></summary>

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th>Ratio</th>
      <th>Savings</th>
      <th>Cospeed</th>
      <th>Despeed</th>
      <th>BPB</th>
      <th>Factor</th>
      <th>Mutilization</th>
      <th>Putilization</th>
      <th>Integrity</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #ffe6e6"><strong>0.851</strong></td>
      <td><strong>14.9%</strong></td>
      <td><strong>5.0 MB/s</strong></td>
      <td><strong>34.8 MB/s</strong></td>
      <td><strong>6.81</strong></td>
      <td><strong>1.18×</strong></td>
      <td><strong>65 MB</strong></td>
      <td><strong>50%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">0.963</td>
      <td>3.7%</td>
      <td>2.1 MB/s</td>
      <td>76.5 MB/s</td>
      <td>7.70</td>
      <td>1.04×</td>
      <td>64 MB</td>
      <td>27%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #ffe6e6">0.901</td>
      <td>9.9%</td>
      <td>5.1 MB/s</td>
      <td>29.9 MB/s</td>
      <td>7.21</td>
      <td>1.11×</td>
      <td>65 MB</td>
      <td>54%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.284</td>
      <td>71.6%</td>
      <td>7.2 MB/s</td>
      <td>36.9 MB/s</td>
      <td>2.27</td>
      <td>3.52×</td>
      <td>65 MB</td>
      <td>45%</td>
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
      <th>Ratio</th>
      <th>Savings</th>
      <th>Cospeed</th>
      <th>Despeed</th>
      <th>BPB</th>
      <th>Factor</th>
      <th>Mutilization</th>
      <th>Putilization</th>
      <th>Integrity</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>average</strong></td>
      <td style="background-color: #e6ffe6"><strong>0.604</strong></td>
      <td><strong>39.6%</strong></td>
      <td><strong>28.1 MB/s</strong></td>
      <td><strong>50.2 MB/s</strong></td>
      <td><strong>4.83</strong></td>
      <td><strong>1.66×</strong></td>
      <td><strong>65 MB</strong></td>
      <td><strong>66%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>archives</td>
      <td style="background-color: #ffe6e6">0.968</td>
      <td>3.2%</td>
      <td>86.7 MB/s</td>
      <td>84.2 MB/s</td>
      <td>7.74</td>
      <td>1.03×</td>
      <td>65 MB</td>
      <td>42%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>contents</td>
      <td style="background-color: #e6ffe6">0.600</td>
      <td>40.0%</td>
      <td>21.6 MB/s</td>
      <td>45.9 MB/s</td>
      <td>4.80</td>
      <td>1.67×</td>
      <td>65 MB</td>
      <td>73%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>blobs</td>
      <td style="background-color: #e6ffe6">0.275</td>
      <td>72.5%</td>
      <td>27.7 MB/s</td>
      <td>55.0 MB/s</td>
      <td>2.20</td>
      <td>3.64×</td>
      <td>65 MB</td>
      <td>24%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

</details>


### 2. Silesia Corpus
*Diverse real-world file types.*

<details>
<summary><strong>archives/</strong></summary>

No data

</details>

---

<details>
<summary><strong>contents/</strong></summary>

No data

</details>

---

<details>
<summary><strong>blobs/</strong></summary>

No data

</details>

---

#### Performance Overview

No data




### 3. Wikipedia Corpus
*Large English text data.*

<details>
<summary><strong>archives/</strong></summary>

No data

</details>

---

<details>
<summary><strong>contents/</strong></summary>

No data

</details>

---

<details>
<summary><strong>blobs/</strong></summary>

No data

</details>

---

#### Performance Overview

No data



### 4. Random Data Corpus
*Testing overhead and worst-case scenarios.*

<details>
<summary><strong>B/</strong></summary>

No data

</details>

---

<details>
<summary><strong>KB/</strong></summary>

No data

</details>

---

<details>
<summary><strong>MB/</strong></summary>

No data

</details>

---

#### Performance Overview

No data



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
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
    </tr>
    <tr>
      <td>GZIP</td>
      <td>0.851</td>
      <td>4.76</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td>0.604</td>
      <td>4.76</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
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
