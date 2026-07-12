# Target-Benchmark Report

> **Report Generated:** July 12, 2026 14:47:24  
> **Benchmark Suite:** V 1.0  
> **Target File:** `Party.rtf` (66.68 KB)  
> **Total Codecs Tested:** 2 (in 0hrs 0min 1secs)  
> **Platform:** Mac OS X, Java 25.0.3
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
3. [Target File](#target-file)
4. [Metrics Overview](#metrics-overview)
5. [Benchmark Results](#benchmark-results)
6. [Conclusion](#conclusion)

---

## Executive Summary

This report presents benchmarking results for Harmattan against standard compression algorithms, evaluated on a single user-selected file rather than a full corpus. Key metrics include compression ratio, space savings, compression speed, decompression speed, and theoretical entropy analysis.

### Session

> **Started:** 12-07-2026:14:47:22  
> **Ended:** 12-07-2026:14:47:25  
> **Duration:** 0 days, 0 hours, 0 minutes, 3 seconds, 378 ms

Codecs are benchmarked simultaneously across spindle threads, so the per-codec breakdown below reports each codec's own start, end and duration independent of the overall session time:

<table>
  <thead>
    <tr>
      <th>Codec</th>
      <th>Thread</th>
      <th>Started</th>
      <th>Ended</th>
      <th>Duration</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>GZIP</td>
      <td>Spindle[0]</td>
      <td>12-07-2026:14:47:22</td>
      <td>12-07-2026:14:47:22</td>
      <td>0 days, 0 hours, 0 minutes, 0 seconds, 442 ms</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td>Spindle[1]</td>
      <td>12-07-2026:14:47:22</td>
      <td>12-07-2026:14:47:24</td>
      <td>0 days, 0 hours, 0 minutes, 1 seconds, 776 ms</td>
    </tr>
  </tbody>
</table>

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
      <td>GZIP</td>
      <td>0.307</td>
    </tr>
    <tr>
      <td><strong>Best Space Savings</strong></td>
      <td>GZIP</td>
      <td>69.3%</td>
    </tr>
    <tr>
      <td><strong>Fastest Compression</strong></td>
      <td>GZIP</td>
      <td>6.2 MB/s</td>
    </tr>
    <tr>
      <td><strong>Fastest Decompression</strong></td>
      <td>Zstandard</td>
      <td>30.7 MB/s</td>
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
      <td>GZIP</td>
      <td>Apache Commons Compress</td>
      <td>1.26.0</td>
      <td>Yes</td>
      <td>[header (10B)] + [payload (20924B)] + [trailer (8B)]</td>
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

## Target File

Unlike the corpora suite, this run benchmarks a single file selected on the device. Each participating codec is run against this file to measure its compression and decompression characteristics.

*   **File:** `Party.rtf` (66.68 KB)

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

### Selected File
*The single file chosen for this benchmark run.*

<details>
<summary><strong>results/</strong></summary>

<table>
  <thead>
    <tr>
      <th rowspan="2">File</th>
      <th rowspan="2">Size</th>
      <th rowspan="2"><code>Entropy</code></th>
      <th rowspan="2">Potential</th>
      <th colspan="2">Compression</th>
    </tr>
    <tr>
      <th>GZIP</th>
      <th>Zstandard</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Party.rtf</td><td>66.68 KB</td><td style="background-color: #ffcccc">5.03</td><td>Moderate</td><td>20.45 KB ✅</td><td>21.32 KB ✅</td></tr>
    <tr><td><strong>Total/Average</strong></td><td><strong>66.68 KB</strong></td><td style="background-color: #ffcccc"><strong>5.03</strong></td><td><strong>-</strong></td><td><strong>20.45 KB</strong></td><td><strong>21.32 KB</strong></td></tr>
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
      <td>GZIP</td>
      <td style="background-color: #e6ffe6">0.307</td>
      <td>69.3%</td>
      <td>6.2 MB/s</td>
      <td>2.7 MB/s</td>
      <td>2.45</td>
      <td>3.26×</td>
      <td>38 MB</td>
      <td>88%</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>Zstandard</td>
      <td style="background-color: #e6ffe6">0.320</td>
      <td>68.0%</td>
      <td>0.0 MB/s</td>
      <td>30.7 MB/s</td>
      <td>2.56</td>
      <td>3.13×</td>
      <td>41 MB</td>
      <td>4%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>



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
      <td style="background-color: #e6ffe6"><strong>0.307</strong></td>
      <td><strong>69.3%</strong></td>
      <td><strong>6.2 MB/s</strong></td>
      <td><strong>2.7 MB/s</strong></td>
      <td><strong>2.45</strong></td>
      <td><strong>3.26×</strong></td>
      <td><strong>38 MB</strong></td>
      <td><strong>88%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>file</td>
      <td style="background-color: #e6ffe6">0.307</td>
      <td>69.3%</td>
      <td>6.2 MB/s</td>
      <td>2.7 MB/s</td>
      <td>2.45</td>
      <td>3.26×</td>
      <td>38 MB</td>
      <td>88%</td>
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
      <td style="background-color: #e6ffe6"><strong>0.320</strong></td>
      <td><strong>68.0%</strong></td>
      <td><strong>0.0 MB/s</strong></td>
      <td><strong>30.7 MB/s</strong></td>
      <td><strong>2.56</strong></td>
      <td><strong>3.13×</strong></td>
      <td><strong>41 MB</strong></td>
      <td><strong>4%</strong></td>
      <td><strong>✅</strong></td>
    </tr>
    <tr>
      <td>file</td>
      <td style="background-color: #e6ffe6">0.320</td>
      <td>68.0%</td>
      <td>0.0 MB/s</td>
      <td>30.7 MB/s</td>
      <td>2.56</td>
      <td>3.13×</td>
      <td>41 MB</td>
      <td>4%</td>
      <td>✅</td>
    </tr>
  </tbody>
</table>

</details>


---

## Conclusion

GZIP demonstrates strong compression performance across all tested corpora. Benchmark results indicate overall codec behavior is consistent with expected compression characteristics.

For standard corpora with lower entropy, GZIP achieves competitive compression ratios while maintaining reasonable speeds.

Further investigation into individual codec performance characteristics is recommended for production deployment decisions.

---
Copyright © 2026 Linkersoft
