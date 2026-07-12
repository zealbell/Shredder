# Target-Benchmark Report

> **Report Generated:** {{TIMESTAMP}}  
> **Benchmark Suite:** V 1.0  
> **Target File:** {{TARGET_FILE}}  
> **Total Codecs Tested:** {{CODEC_COUNT}}  
> **Platform:** {{PLATFORM_INFO}}
{{DEVICE_INFO}}

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

This report presents benchmarking results for {{TITLE}} against standard compression algorithms, evaluated on a single user-selected file rather than a full corpus. Key metrics include compression ratio, space savings, compression speed, decompression speed, and theoretical entropy analysis.

### Session

> **Started:** {{START_TIME}}  
> **Ended:** {{END_TIME}}  
> **Duration:** {{DURATION}}

Codecs are benchmarked simultaneously across worker threads, so the per-codec breakdown below reports each codec's own start, end and duration independent of the overall session time:

{{CODEC_SCHEDULE_TABLE}}

### Key Findings

{{KEY_FINDINGS_TABLE}}

---

## Participating Codecs

The following codecs were included in this benchmark run:

{{CODEC_LIST_TABLE}}

---

## Target File

Unlike the corpora suite, this run benchmarks a single file selected on the device. Each participating codec is run against this file to measure its compression and decompression characteristics.

*   **File:** {{TARGET_FILE}}

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

{{TARGET_TABLE}}

</details>

---

#### Performance Overview

{{TARGET_PERFORMANCE_TABLE}}

{{TARGET_DETAILS}}

---

## Conclusion

{{CONCLUSION_TEXT}}

---
Copyright © 2026 Linkersoft
