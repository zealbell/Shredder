package com.linkersoft.reductions;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.stream.Collectors;

/**
 * Benchmarking facilitates comprehensive performance testing of compression codecs.
 * <p>
 * This class wraps {@link Transcoder} to provide:
 * <ul>
 *   <li>Codec verification and performance analysis</li>
 *   <li>Detailed Markdown report generation</li>
 *   <li>Corpus management (downloading and preparation)</li>
 *   <li>Custom "Light" codec for baseline comparison</li>
 * </ul>
 * </p>
 * <p>
 * The benchmark uses a structured corpus and generates reports based on the
 * template at <a href="../../../../../resources/benchmarking/sandbox/results/benchmarking-blueprint.md">{@code src/main/resources/benchmarking/sandbox/results/benchmarking-blueprint.md}</a>.
 * <br>
 * Reports are generated with the naming convention
 * {@code Benchmarking(N)-dd-MM-yyyy.md}, where {@code N} is the number of participating codecs.
 * </p>
 * <p>
 * <b>Directory Organization (Standard Corpora: Canterbury, Silesia, Wikipedia):</b>
 * <ul>
 *   <li><b>archives/</b>: Original compressed archives.</li>
 *   <li><b>contents/</b>: Extracted files used as raw input. Files are individually compressed to stress codecs.</li>
 *   <li><b>blobs/</b>: Aggregated content files.</li>
 * </ul>
 * </p>
 */
public class Benchmarking {

    private final Transcoder transcoder;
    private final List<Integer> codecIds;
    private static final String BASE_PATH = "src/main/resources/benchmarking/corpora";
    private static final String SANDBOX_TESTING = "src/main/resources/benchmarking/sandbox/testing";
    private String resultsPath;
    private String title;
    private Map<Integer, String> codecNotes = new HashMap<>();
    private boolean showLogs = false;
    private long startTime;
    private int totalTasks = 0;
    private int completedTasks = 0;

    private String getConciseTime(long nanos) {
        long ms = nanos / 1_000_000;
        long s = ms / 1000;
        long m = s / 60;
        long h = m / 60;
        
        if (h > 0) return h + "h";
        if (m > 0) return m + "m";
        if (s > 0) return s + "s";
        if (ms > 0) return ms + "ms";
        return (nanos / 1000) + "ms";
    }

    // Benchmark result storage
    private static class BenchmarkResult {
        String fileName;
        String subType; // archives, contents, blobs, B, KB, MB
        String codecName;
        int codecId;
        long originalSize;
        long compressedSize;
        double compressionRatio;
        double spaceSavings;
        double compressionTimeMs;
        double decompressionTimeMs;
        double compressionSpeedMBps;
        double decompressionSpeedMBps;
        double entropy;
        double bitsPerByte;
        boolean integrityPassed;
        String errorMessage;
        long memoryUsedBytes; // peak memory during operation
        double cpuUtilization; // CPU utilization as percentage (cpuTime/wallTime)
    }

    private java.lang.management.MemoryMXBean memoryBean;
    private List<java.lang.management.MemoryPoolMXBean> memoryPools;

    /**
     * Creates a new Benchmarking instance with default settings.
     * @param title The title of the benchmarking suite (e.g., "Reductions").
     */
    public Benchmarking(String title) {
        this.title = title;
        this.transcoder = new Transcoder();
        this.codecIds = new ArrayList<>();
        this.resultsPath = "src/main/resources/benchmarking/sandbox/results";
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.memoryPools = ManagementFactory.getMemoryPoolMXBeans();
    }

    public Benchmarking() {
        this("Reductions");
    }

    public Benchmarking(String title, String resultsPath) {
        this(title);
        this.resultsPath = resultsPath;
    }

    public Transcoder getTranscoder() {
        return transcoder;
    }

    /**
     * Adds a codec to the benchmarking suite by its ID.
     * @param codecId The codec ID to add.
     * @return This Benchmarking instance.
     */
    public Benchmarking addCodec(int codecId) {
        if (!codecIds.contains(codecId)) {
            codecIds.add(codecId);
            transcoder.addCodec(codecId);
        }
        return this;
    }

    /**
     * Adds a custom codec instance to the benchmarking suite.
     * @param codecId The ID for the custom codec.
     * @param codec The custom codec instance.
     * @return This Benchmarking instance.
     */
    public Benchmarking addCodec(int codecId, Transcoder.Codec codec) {
        if (!codecIds.contains(codecId)) {
            codecIds.add(codecId);
            transcoder.addCodec(codecId, codec);
        }
        return this;
    }

    public Benchmarking setNotes(int codecId, String note) {
        codecNotes.put(codecId, note);
        return this;
    }

    public void showLogs(boolean show) {
        this.showLogs = show;
    }

    private void log(String message) {
        if (showLogs) {
            long elapsed = System.nanoTime() - startTime;
            String timeStr = formatDuration(elapsed);
            // Pad with spaces to clear previous longer lines
            System.out.print("\r[" + timeStr + "] " + message + "                                                  ");
        }
    }

    private String formatDuration(long nanos) {
        long ms = nanos / 1_000_000;
        long ns = nanos % 1_000_000;
        long s = ms / 1000;
        long m = s / 60;
        long h = m / 60;
        
        ms = ms % 1000;
        s = s % 60;
        m = m % 60;
        
        return String.format("%02d:%02d:%02d:%03d:%03d-ms", h, m, s, ms, ns / 1000);
    }

    private long getPeakMemoryUsage() {
        long peak = 0;
        for (java.lang.management.MemoryPoolMXBean pool : memoryPools) {
            peak += pool.getPeakUsage().getUsed();
        }
        // Note: This sums peak usage of all JVM memory pools (Heap + Non-Heap).
        // It may not capture:
        // 1. Native memory allocated directly by JNI codecs (outside JVM pools).
        // 2. Short-lived allocations if GC collects them before peak is registered (though peak should track max).
        // 3. For very small files, the overhead might be negligible (< 1MB).
        return peak;
    }

    private void resetPeakMemoryUsage() {
        for (java.lang.management.MemoryPoolMXBean pool : memoryPools) {
            pool.resetPeakUsage();
        }
    }

    private BenchmarkResult benchmarkCodec(File testFile, int codecId, String subType, File rootDir, String testSection) {
        BenchmarkResult result = new BenchmarkResult();
        // Calculate relative path for contents, or just name for others
        if (rootDir != null && testFile.getAbsolutePath().startsWith(rootDir.getAbsolutePath())) {
            String relative = rootDir.toURI().relativize(testFile.toURI()).getPath();
            result.fileName = relative;
        } else {
            result.fileName = testFile.getName();
        }
        
        // If it's a directory (shouldn't happen with listRecursive but safe safeguard), skip or handle
        if (testFile.isDirectory()) return null;

        result.subType = subType;
        result.codecId = codecId;
        result.codecName = transcoder.getCodecName(codecId);
        result.originalSize = testFile.length();

        File sandboxDir = new File(SANDBOX_TESTING);
        sandboxDir.mkdirs();

        String conciseTime = getConciseTime(System.nanoTime() - startTime);
        int percent = totalTasks > 0 ? (int) ((completedTasks * 100L) / totalTasks) : 0;
        log("Benchmarking " + result.codecName + " on " + result.fileName + " (" + result.subType + ") - " + testSection + " | " + conciseTime + " | " + percent + "% Done");

        try {
            transcoder.setInput(testFile);
            result.entropy = transcoder.getEntropy();
            result.bitsPerByte = result.entropy; // Fallback if no compression

            // Track memory and CPU
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            
            // Reset peak memory usage before compression
            System.gc();
            resetPeakMemoryUsage();
            
            long cpuBefore = threadBean.getCurrentThreadCpuTime(); // nanoseconds
            long compStartTime = System.nanoTime();
            File compressed = transcoder.getOutput(codecId);
            long compEndTime = System.nanoTime();
            long peakMemoryComp = getPeakMemoryUsage();

            if (compressed == null || !compressed.exists()) {
                result.errorMessage = "Compression failed";
                result.integrityPassed = false;
                return result;
            }

            // Move compressed file to sandbox/testing if it's not already there
            File sandboxCompressed = new File(sandboxDir, compressed.getName());
            if (!compressed.getAbsolutePath().startsWith(sandboxDir.getAbsolutePath())) {
                Files.move(compressed.toPath(), sandboxCompressed.toPath(), StandardCopyOption.REPLACE_EXISTING);
                compressed = sandboxCompressed;
            }

            result.compressedSize = compressed.length();
            result.compressionTimeMs = (compEndTime - compStartTime) / 1_000_000.0;
            result.compressionRatio = result.originalSize > 0 ?
                (double) result.compressedSize / result.originalSize : 0;
            result.spaceSavings = (1 - result.compressionRatio) * 100;
            result.compressionSpeedMBps = result.compressionTimeMs > 0 ?
                (result.originalSize / 1_048_576.0) / (result.compressionTimeMs / 1000.0) : 0;
            result.bitsPerByte = result.originalSize > 0 ? (result.compressedSize * 8.0) / result.originalSize : 0;

            // Reset peak memory for decompression
            System.gc();
            resetPeakMemoryUsage();

            long decompStartTime = System.nanoTime();
            File decompressed = transcoder.getInput(codecId, compressed);
            long decompEndTime = System.nanoTime();
            long peakMemoryDecomp = getPeakMemoryUsage();
            
            long cpuAfter = threadBean.getCurrentThreadCpuTime(); // nanoseconds

            if (decompressed == null || !decompressed.exists()) {
                result.errorMessage = "Decompression failed";
                result.integrityPassed = false;
                compressed.delete();
                return result;
            }

            // Move decompressed file to sandbox/testing if needed
            File sandboxDecompressed = new File(sandboxDir, decompressed.getName());
            if (!decompressed.getAbsolutePath().startsWith(sandboxDir.getAbsolutePath())) {
                Files.move(decompressed.toPath(), sandboxDecompressed.toPath(), StandardCopyOption.REPLACE_EXISTING);
                decompressed = sandboxDecompressed;
            }

            // Use the maximum peak memory observed
            result.memoryUsedBytes = Math.max(peakMemoryComp, peakMemoryDecomp);

            // CPU utilization = (CPU time consumed) / (wall clock time elapsed) × 100%
            long totalWallTimeNs = (compEndTime - compStartTime) + (decompEndTime - decompStartTime);
            long totalCpuTimeNs = cpuAfter - cpuBefore;
            result.cpuUtilization = totalWallTimeNs > 0 ?
                (double) totalCpuTimeNs / totalWallTimeNs * 100.0 : 0;

            result.decompressionTimeMs = (decompEndTime - decompStartTime) / 1_000_000.0;
            result.decompressionSpeedMBps = result.decompressionTimeMs > 0 ?
                (result.originalSize / 1_048_576.0) / (result.decompressionTimeMs / 1000.0) : 0;

            long originalCRC = Generator.getChecksum(testFile.getAbsolutePath());
            long decompressedCRC = Generator.getChecksum(decompressed.getAbsolutePath());
            result.integrityPassed = (originalCRC == decompressedCRC);

            compressed.delete();
            decompressed.delete();

        } catch (Exception e) {
            result.errorMessage = e.getMessage();
            result.integrityPassed = false;
            e.printStackTrace();
        }

        return result;
    }

    private List<File> listFiles(File dir) {
        try {
            return Files.list(dir.toPath())
                .filter(Files::isRegularFile)
                .filter(p -> !p.getFileName().toString().startsWith("."))
                .map(Path::toFile)
                .sorted()
                .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private List<File> listRecursive(File dir) {
        try {
            return Files.walk(dir.toPath())
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .sorted()
                .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private String generateReport(Map<String, Map<String, List<BenchmarkResult>>> allResults) throws IOException {
        String templatePath = "src/main/resources/benchmarking/sandbox/results/benchmarking-blueprint.md";
        String template = Files.readString(Paths.get(templatePath));

        java.time.Duration d = java.time.Duration.ofNanos(System.nanoTime() - startTime);
        String durationStr = String.format("%dhrs %dmin %dsecs", d.toHours(), d.toMinutesPart(), d.toSecondsPart());

        LocalDateTime now = LocalDateTime.now();
        template = template.replace("{{TITLE}}", title)
                           .replace("{{TIMESTAMP}}", now.format(DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm:ss")))
                           .replace("{{CODEC_COUNT}}", codecIds.size() + " (in " + durationStr + ")")
                           .replace("{{PLATFORM_INFO}}", System.getProperty("os.name") + ", Java " + System.getProperty("java.version"))
                           .replace("{{DEVICE_INFO}}", Hardware.getDeviceInfo());

        // Generate tables
        template = template.replace("{{KEY_FINDINGS_TABLE}}", generateKeyFindings(allResults));
        template = template.replace("{{CODEC_LIST_TABLE}}", generateCodecList());

        String[] standard = {"CANTERBURY", "SILESIA", "WIKIPEDIA"};
        String[] subLabels = {"archives", "contents", "blobs"};
        for (String corpus : standard) {
            Map<String, List<BenchmarkResult>> corpusData = allResults.get(corpus.toLowerCase());
            template = template.replace("{{" + corpus + "_ARCHIVES_TABLE}}", generateComparisonTable(corpusData, "archives"));
            template = template.replace("{{" + corpus + "_CONTENTS_TABLE}}", generateComparisonTable(corpusData, "contents"));
            template = template.replace("{{" + corpus + "_BLOBS_TABLE}}", generateComparisonTable(corpusData, "blobs"));
            template = template.replace("{{" + corpus + "_PERFORMANCE_TABLE}}", generatePerformanceTable(corpusData));
            template = template.replace("{{" + corpus + "_DETAILS}}", generateDetailTables(corpusData, subLabels));
        }

        // Random
        Map<String, List<BenchmarkResult>> randomData = allResults.get("random");
        String[] randomLabels = {"B", "KB", "MB"};
        template = template.replace("{{RANDOM_B_TABLE}}", generateComparisonTable(randomData, "B"));
        template = template.replace("{{RANDOM_KB_TABLE}}", generateComparisonTable(randomData, "KB"));
        template = template.replace("{{RANDOM_MB_TABLE}}", generateComparisonTable(randomData, "MB"));
        template = template.replace("{{RANDOM_PERFORMANCE_TABLE}}", generatePerformanceTable(randomData));
        template = template.replace("{{RANDOM_DETAILS}}", generateDetailTables(randomData, randomLabels));

        template = template.replace("{{COMPARATIVE_ANALYSIS_TABLE}}", generateComparativeAnalysis(allResults));

        // Conclusion logic
        template = template.replace("{{CONCLUSION_TEXT}}", generateConclusion(allResults));

        return template;
    }

    private String generateCodecList() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n");
        sb.append("  <thead>\n    <tr>\n      <th>Codec</th>\n      <th>Library</th>\n      <th>Version</th>\n      <th>Streaming</th>\n      <th>Structure</th>\n      <th>Notes</th>\n    </tr>\n  </thead>\n");
        sb.append("  <tbody>\n");
        for (int id : codecIds) {
            Transcoder.Codec codec = transcoder.getCodec(id);
            Map<String, String> footer = codec.getFooter();
            
            String lib = footer.getOrDefault("library", "Unknown");
            String version = footer.getOrDefault("version", "-");
            String streaming = footer.getOrDefault("streaming", "No");
            String structure = footer.getOrDefault("structure", "-");
            String note = footer.getOrDefault("notes", "-");
            
            sb.append("    <tr>\n");
            sb.append(String.format("      <td>%s</td>\n      <td>%s</td>\n      <td>%s</td>\n      <td>%s</td>\n      <td>%s</td>\n      <td>%s</td>\n",
                codec.getName(), lib, version, streaming, structure, note));
            sb.append("    </tr>\n");
        }
        sb.append("  </tbody>\n");
        sb.append("</table>\n");
        return sb.toString();
    }

    private String getCodecLibrary(int id) {
        if (id >= 1000) return "Custom";
        switch (id) {
            case Transcoder.Codec.ZSTANDARD: return "zstd-jni";
            case Transcoder.Codec.BROTLI: return "Brotli4j";
            case Transcoder.Codec.GZIP:
            case Transcoder.Codec.DEFLATE:
            case Transcoder.Codec.BZIP2:
            case Transcoder.Codec.LZ4_BLOCK:
            case Transcoder.Codec.LZ4_FRAMED:
            case Transcoder.Codec.SNAPPY_RAW:
            case Transcoder.Codec.SNAPPY_FRAMED:
            case Transcoder.Codec.XZ:
            case Transcoder.Codec.LZMA:
            case Transcoder.Codec.LZW:
            case Transcoder.Codec.DEFLATE64:
                return "Apache Commons";
            case Transcoder.Codec.BASE64:
            case Transcoder.Codec.BASE32:
                return "Apache Commons Codec";
            case Transcoder.Codec.RUN_LENGTH:
            case Transcoder.Codec.HUFFMAN:
            case Transcoder.Codec.BWT:
            case Transcoder.Codec.LZ78:
            case Transcoder.Codec.PLAIN:
                return "Custom";
            default:
                return "Unknown";
        }
    }

    private String generateKeyFindings(Map<String, Map<String, List<BenchmarkResult>>> allResults) {
        // Flat list of all results
        List<BenchmarkResult> flat = new ArrayList<>();
        allResults.values().forEach(m -> m.values().forEach(flat::addAll));

        if (flat.isEmpty()) return "<table><tbody><tr><td>No data available</td></tr></tbody></table>";

        // Best compression ratio
        BenchmarkResult bestRatio = flat.stream()
                .filter(r -> r.compressionRatio > 0)
                .min(Comparator.comparingDouble(r -> r.compressionRatio))
                .orElse(null);

        // Best space savings
        BenchmarkResult bestSavings = flat.stream()
                .max(Comparator.comparingDouble(r -> r.spaceSavings))
                .orElse(null);

        // Fastest compression
        BenchmarkResult fastestComp = flat.stream()
                .filter(r -> r.compressionSpeedMBps > 0)
                .max(Comparator.comparingDouble(r -> r.compressionSpeedMBps))
                .orElse(null);

        // Fastest decompression
        BenchmarkResult fastestDecomp = flat.stream()
                .filter(r -> r.decompressionSpeedMBps > 0)
                .max(Comparator.comparingDouble(r -> r.decompressionSpeedMBps))
                .orElse(null);

        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n");
        sb.append("  <thead>\n    <tr>\n      <th>Category</th>\n      <th>Best Performer</th>\n      <th>Value</th>\n    </tr>\n  </thead>\n");
        sb.append("  <tbody>\n");

        if (bestRatio != null) {
            sb.append(String.format("    <tr>\n      <td><strong>Best Compression Ratio</strong></td>\n      <td>%s</td>\n      <td>%.3f</td>\n    </tr>\n",
                    bestRatio.codecName, bestRatio.compressionRatio));
        }
        if (bestSavings != null) {
            sb.append(String.format("    <tr>\n      <td><strong>Best Space Savings</strong></td>\n      <td>%s</td>\n      <td>%.1f%%</td>\n    </tr>\n",
                    bestSavings.codecName, bestSavings.spaceSavings));
        }
        if (fastestComp != null) {
            sb.append(String.format("    <tr>\n      <td><strong>Fastest Compression</strong></td>\n      <td>%s</td>\n      <td>%s MB/s</td>\n    </tr>\n",
                    fastestComp.codecName, formatDecimal(fastestComp.compressionSpeedMBps)));
        }
        if (fastestDecomp != null) {
            sb.append(String.format("    <tr>\n      <td><strong>Fastest Decompression</strong></td>\n      <td>%s</td>\n      <td>%s MB/s</td>\n    </tr>\n",
                    fastestDecomp.codecName, formatDecimal(fastestDecomp.decompressionSpeedMBps)));
        }

        sb.append("  </tbody>\n</table>");
        return sb.toString();
    }

    private String generateComparisonTable(Map<String, List<BenchmarkResult>> data, String key) {
        if (data == null || !data.containsKey(key)) return "No data";

        List<BenchmarkResult> results = data.get(key);
        // Group by file, preserving insertion order
        Map<String, List<BenchmarkResult>> byFile = new LinkedHashMap<>();
        for (BenchmarkResult r : results) {
            byFile.computeIfAbsent(r.fileName, k -> new ArrayList<>()).add(r);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n");
        sb.append("  <thead>\n");
        sb.append("    <tr>\n");
        sb.append("      <th rowspan=\"2\">File</th>\n");
        sb.append("      <th rowspan=\"2\">Size</th>\n");
        sb.append("      <th rowspan=\"2\"><code>Entropy</code></th>\n");
        sb.append("      <th rowspan=\"2\">Potential</th>\n");
        sb.append(String.format("      <th colspan=\"%d\">Compression</th>\n", codecIds.size()));
        sb.append("    </tr>\n");
        sb.append("    <tr>\n");
        for (int id : codecIds) {
            sb.append("      <th>").append(transcoder.getCodecName(id)).append("</th>\n");
        }
        sb.append("    </tr>\n");
        sb.append("  </thead>\n");
        sb.append("  <tbody>\n");

        long totalOriginal = 0;
        double totalEntropy = 0;
        int fileCount = 0;
        Map<Integer, Long> totalCompressed = new LinkedHashMap<>();
        for (int id : codecIds) totalCompressed.put(id, 0L);

        for (String fileName : byFile.keySet()) {
            List<BenchmarkResult> fileResults = byFile.get(fileName);
            BenchmarkResult first = fileResults.get(0);

            totalOriginal += first.originalSize;
            totalEntropy += first.entropy;
            fileCount++;

            sb.append("    <tr>");
            sb.append(String.format("<td>%s</td><td>%s</td><td style=\"background-color: %s\">%.2f</td><td>%s</td>",
                fileName, formatBytes(first.originalSize), getEntropyColor(first.entropy), first.entropy, getPotential(first.entropy)));

            for (int id : codecIds) {
                Optional<BenchmarkResult> r = fileResults.stream().filter(res -> res.codecId == id).findFirst();
                if (r.isPresent()) {
                    BenchmarkResult res = r.get();
                    String mark = res.compressedSize < res.originalSize ? " ✅" : " ❌";
                    sb.append(String.format("<td>%s%s</td>", formatBytes(res.compressedSize), mark));
                    totalCompressed.merge(id, res.compressedSize, Long::sum);
                } else {
                    sb.append("<td>-</td>");
                }
            }
            sb.append("</tr>\n");
        }

        // Total/Average row
        double avgEntropy = fileCount > 0 ? totalEntropy / fileCount : 0;
        sb.append("    <tr>");
        sb.append(String.format("<td><strong>Total/Average</strong></td><td><strong>%s</strong></td><td style=\"background-color: %s\"><strong>%.2f</strong></td><td><strong>-</strong></td>",
            formatBytes(totalOriginal), getEntropyColor(avgEntropy), avgEntropy));
        for (int id : codecIds) {
            sb.append(String.format("<td><strong>%s</strong></td>", formatBytes(totalCompressed.get(id))));
        }
        sb.append("</tr>\n");

        sb.append("  </tbody>\n");
        sb.append("</table>");
        return sb.toString();
    }

    private String generatePerformanceTable(Map<String, List<BenchmarkResult>> data) {
         if (data == null) return "No data";
         List<BenchmarkResult> all = new ArrayList<>();
         data.values().forEach(all::addAll);

         boolean hasNegatives = false;

         StringBuilder sb = new StringBuilder();
         sb.append("<table>\n");
         sb.append("  <thead>\n    <tr>\n      <th>Codec</th>\n      <th>Ratio</th>\n      <th>Savings</th>\n      <th>Cospeed</th>\n      <th>Despeed</th>\n      <th>BPB</th>\n      <th>Factor</th>\n      <th>Mutilization</th>\n      <th>Putilization</th>\n      <th>Integrity</th>\n    </tr>\n  </thead>\n");
         sb.append("  <tbody>\n");

         for (int id : codecIds) {
             List<BenchmarkResult> codecResults = all.stream().filter(r -> r.codecId == id).collect(Collectors.toList());
             if (codecResults.isEmpty()) continue;

             double avgRatio = codecResults.stream().mapToDouble(r -> r.compressionRatio).average().orElse(0);
             double avgSavings = codecResults.stream().mapToDouble(r -> r.spaceSavings).average().orElse(0);
             double avgCoSpeed = codecResults.stream().mapToDouble(r -> r.compressionSpeedMBps).average().orElse(0);
             double avgDeSpeed = codecResults.stream().mapToDouble(r -> r.decompressionSpeedMBps).average().orElse(0);
             double avgBPB = codecResults.stream().mapToDouble(r -> r.bitsPerByte).average().orElse(0);
             double factor = avgRatio > 0 ? 1.0 / avgRatio : 0;
             boolean allPass = codecResults.stream().allMatch(r -> r.integrityPassed);
             long avgMemory = (long) codecResults.stream().mapToLong(r -> r.memoryUsedBytes).average().orElse(0);
             String memStr = formatMemory(avgMemory);

             double avgCpu = codecResults.stream().mapToDouble(r -> r.cpuUtilization).average().orElse(0);
             String cpuUtil = String.format("%.0f%%", avgCpu);

             if (avgSavings < 0) hasNegatives = true;

             sb.append("    <tr>\n");
             sb.append(String.format("      <td>%s</td>\n", transcoder.getCodecName(id)));
             sb.append(String.format("      <td style=\"background-color: %s\">%.3f</td>\n", getRatioColor(avgRatio), avgRatio));
             sb.append(String.format("      <td>%.1f%%</td>\n", avgSavings));
             sb.append(String.format("      <td>%s MB/s</td>\n", formatDecimal(avgCoSpeed)));
             sb.append(String.format("      <td>%s MB/s</td>\n", formatDecimal(avgDeSpeed)));
             sb.append(String.format("      <td>%.2f</td>\n", avgBPB));
             sb.append(String.format("      <td>%.2f×</td>\n", factor));
             sb.append(String.format("      <td>%s</td>\n", memStr));
             sb.append(String.format("      <td>%s</td>\n", cpuUtil));
             sb.append(String.format("      <td>%s</td>\n", allPass ? "✅" : "❌"));
             sb.append("    </tr>\n");
         }
         sb.append("  </tbody>\n</table>\n");

         if (hasNegatives) {
             sb.append("\n> ⚠️ **Caution:** Negative values in performance metrics indicate data expansion, where the compressed size exceeds the original size. This typically occurs when compressing high-entropy data (random) or very small files where header overhead outweighs compression gains.\n");
         }

         return sb.toString();
    }

    /**
     * Generates per-codec detail tables for a given corpus, breaking down performance by sub-type.
     * Matches the demo format with collapsible sections per codec.
     */
    private String generateDetailTables(Map<String, List<BenchmarkResult>> data, String[] subLabels) {
        if (data == null) return "";

        List<BenchmarkResult> all = new ArrayList<>();
        data.values().forEach(all::addAll);
        if (all.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        // Determine the first column header based on sub-type labels
        String colHeader = isRandomSubType(subLabels) ? "Size" : "Directory";

        for (int id : codecIds) {
            List<BenchmarkResult> codecResults = all.stream().filter(r -> r.codecId == id).collect(Collectors.toList());
            if (codecResults.isEmpty()) continue;

            String codecName = transcoder.getCodecName(id);

            sb.append("\n<details>\n<summary><strong>").append(codecName).append("</strong></summary>\n\n");
            
            sb.append("<table>\n");
            sb.append(String.format("  <thead>\n    <tr>\n      <th>%s</th>\n      <th>Ratio</th>\n      <th>Savings</th>\n      <th>Cospeed</th>\n      <th>Despeed</th>\n      <th>BPB</th>\n      <th>Factor</th>\n      <th>Mutilization</th>\n      <th>Putilization</th>\n      <th>Integrity</th>\n    </tr>\n  </thead>\n", colHeader));
            sb.append("  <tbody>\n");

            // Overall average row
            double avgRatio = codecResults.stream().mapToDouble(r -> r.compressionRatio).average().orElse(0);
            double avgSavings = codecResults.stream().mapToDouble(r -> r.spaceSavings).average().orElse(0);
            double avgCoSpeed = codecResults.stream().mapToDouble(r -> r.compressionSpeedMBps).average().orElse(0);
            double avgDeSpeed = codecResults.stream().mapToDouble(r -> r.decompressionSpeedMBps).average().orElse(0);
            double avgBPB = codecResults.stream().mapToDouble(r -> r.bitsPerByte).average().orElse(0);
            double avgFactor = avgRatio > 0 ? 1.0 / avgRatio : 0;
            boolean allPass = codecResults.stream().allMatch(r -> r.integrityPassed);
            long avgMemory = (long) codecResults.stream().mapToLong(r -> r.memoryUsedBytes).average().orElse(0);
            double avgCpu = codecResults.stream().mapToDouble(r -> r.cpuUtilization).average().orElse(0);
            String cpuUtil = String.format("%.0f%%", avgCpu);

            sb.append("    <tr>\n");
            sb.append("      <td><strong>average</strong></td>\n");
            sb.append(String.format("      <td style=\"background-color: %s\"><strong>%.3f</strong></td>\n", getRatioColor(avgRatio), avgRatio));
            sb.append(String.format("      <td><strong>%.1f%%</strong></td>\n", avgSavings));
            sb.append(String.format("      <td><strong>%s MB/s</strong></td>\n", formatDecimal(avgCoSpeed)));
            sb.append(String.format("      <td><strong>%s MB/s</strong></td>\n", formatDecimal(avgDeSpeed)));
            sb.append(String.format("      <td><strong>%.2f</strong></td>\n", avgBPB));
            sb.append(String.format("      <td><strong>%.2f×</strong></td>\n", avgFactor));
            sb.append(String.format("      <td><strong>%s</strong></td>\n", formatMemory(avgMemory)));
            sb.append(String.format("      <td><strong>%s</strong></td>\n", cpuUtil));
            sb.append(String.format("      <td><strong>%s</strong></td>\n", allPass ? "✅" : "❌"));
            sb.append("    </tr>\n");

            // Per sub-type rows
            for (String label : subLabels) {
                List<BenchmarkResult> subResults = codecResults.stream().filter(r -> r.subType.equals(label)).collect(Collectors.toList());
                if (subResults.isEmpty()) continue;

                double subRatio = subResults.stream().mapToDouble(r -> r.compressionRatio).average().orElse(0);
                double subSavings = subResults.stream().mapToDouble(r -> r.spaceSavings).average().orElse(0);
                double subCoSpeed = subResults.stream().mapToDouble(r -> r.compressionSpeedMBps).average().orElse(0);
                double subDeSpeed = subResults.stream().mapToDouble(r -> r.decompressionSpeedMBps).average().orElse(0);
                double subBPB = subResults.stream().mapToDouble(r -> r.bitsPerByte).average().orElse(0);
                double subFactor = subRatio > 0 ? 1.0 / subRatio : 0;
                boolean subPass = subResults.stream().allMatch(r -> r.integrityPassed);
                long subMemory = (long) subResults.stream().mapToLong(r -> r.memoryUsedBytes).average().orElse(0);

                double subCpu = subResults.stream().mapToDouble(r -> r.cpuUtilization).average().orElse(0);
                String subCpuUtil = String.format("%.0f%%", subCpu);

                sb.append("    <tr>\n");
                sb.append(String.format("      <td>%s</td>\n", label));
                sb.append(String.format("      <td style=\"background-color: %s\">%.3f</td>\n", getRatioColor(subRatio), subRatio));
                sb.append(String.format("      <td>%.1f%%</td>\n", subSavings));
                sb.append(String.format("      <td>%s MB/s</td>\n", formatDecimal(subCoSpeed)));
                sb.append(String.format("      <td>%s MB/s</td>\n", formatDecimal(subDeSpeed)));
                sb.append(String.format("      <td>%.2f</td>\n", subBPB));
                sb.append(String.format("      <td>%.2f×</td>\n", subFactor));
                sb.append(String.format("      <td>%s</td>\n", formatMemory(subMemory)));
                sb.append(String.format("      <td>%s</td>\n", subCpuUtil));
                sb.append(String.format("      <td>%s</td>\n", subPass ? "✅" : "❌"));
                sb.append("    </tr>\n");
            }

            sb.append("  </tbody>\n</table>\n");
            sb.append("\n</details>\n");
        }

        return sb.toString();
    }

    private boolean isRandomSubType(String[] labels) {
        return labels.length > 0 && (labels[0].equals("B") || labels[0].equals("KB") || labels[0].equals("MB"));
    }

    private String generateComparativeAnalysis(Map<String, Map<String, List<BenchmarkResult>>> allResults) {
        StringBuilder sb = new StringBuilder();
        for (int id : codecIds) {
            sb.append("    <tr>\n");
            String codecName = transcoder.getCodecName(id);
            // Bold the best performer(s)
            sb.append(String.format("      <td>%s</td>\n", codecName));
            // Add columns for each corpus
            String[] corpora = {"canterbury", "silesia", "wikipedia", "random"};
            for (String corpus : corpora) {
                Map<String, List<BenchmarkResult>> corpusData = allResults.get(corpus);
                if (corpusData != null) {
                    List<BenchmarkResult> all = new ArrayList<>();
                    corpusData.values().forEach(all::addAll);
                    double avgRatio = all.stream().filter(r -> r.codecId == id).mapToDouble(r -> r.compressionRatio).average().orElse(0);
                    double avgEntropy = all.stream().filter(r -> r.codecId == id).mapToDouble(r -> r.entropy).average().orElse(0);
                    sb.append(String.format("      <td>%.3f</td>\n      <td>%.2f</td>\n", avgRatio, avgEntropy));
                } else {
                    sb.append("      <td>-</td>\n      <td>-</td>\n");
                }
            }
            sb.append("    </tr>\n");
        }
        return sb.toString();
    }

    private String generateConclusion(Map<String, Map<String, List<BenchmarkResult>>> allResults) {
        List<BenchmarkResult> flat = new ArrayList<>();
        allResults.values().forEach(m -> m.values().forEach(flat::addAll));

        if (flat.isEmpty()) {
            return "No benchmark data was collected.";
        }

        // Find best overall codec by average ratio
        Map<Integer, Double> avgRatios = new LinkedHashMap<>();
        for (int id : codecIds) {
            double avg = flat.stream().filter(r -> r.codecId == id).mapToDouble(r -> r.compressionRatio).average().orElse(1.0);
            avgRatios.put(id, avg);
        }
        int bestCodecId = avgRatios.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey).orElse(codecIds.get(0));
        String bestName = transcoder.getCodecName(bestCodecId);

        StringBuilder sb = new StringBuilder();
        sb.append(bestName).append(" demonstrates strong compression performance across all tested corpora. ");
        sb.append("Benchmark results indicate overall codec behavior is consistent with expected compression characteristics.\n\n");
        sb.append("For standard corpora with lower entropy, ").append(bestName);
        sb.append(" achieves competitive compression ratios while maintaining reasonable speeds.\n\n");
        sb.append("Further investigation into individual codec performance characteristics is recommended for production deployment decisions.");
        return sb.toString();
    }

    private String getPotential(double entropy) {
        if (entropy < 2.0) return "Excellent";
        if (entropy < 4.0) return "Very High";
        if (entropy < 5.0) return "High";
        if (entropy < 6.0) return "Moderate";
        if (entropy < 7.5) return "Low";
        return "None";
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private String formatDecimal(double value) {
        if (value >= 1000) {
            return String.format("%,.1f", value);
        }
        return String.format("%.1f", value);
    }

    private String formatMemory(long bytes) {
        if (bytes <= 0) return "< 1 MB";
        long mb = bytes / (1024 * 1024);
        if (mb < 1) return "< 1 MB";
        return mb + " MB";
    }



    private static void unzip(File zipFile, File destDir) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
             ZipEntry entry;
             while ((entry = zis.getNextEntry()) != null) {
                 if (entry.getName().startsWith("__MACOSX") || entry.getName().contains("/__MACOSX")) {
                     continue;
                 }
                 File newFile = new File(destDir, entry.getName());
                 if (entry.isDirectory()) {
                     newFile.mkdirs();
                 } else {
                     new File(newFile.getParent()).mkdirs();
                     try (FileOutputStream fos = new FileOutputStream(newFile)) {
                         byte[] buffer = new byte[1024];
                         int len;
                         while ((len = zis.read(buffer)) > 0) {
                             fos.write(buffer, 0, len);
                         }
                     }
                 }
             }
        } catch (IOException e) {
        }
    }

    private String getEntropyColor(double entropy) {
        if (entropy < 2.0) return "#fff0f0";
        if (entropy < 4.0) return "#ffe0e0";
        if (entropy < 6.0) return "#ffcccc";
        if (entropy < 7.5) return "#ff9999";
        return "#ff6666";
    }

    private String getRatioColor(double ratio) {
        return ratio < 0.80 ? "#e6ffe6" : "#ffe6e6"; // Light green for good, light red otherwise
    }


    private static boolean isValidCorpus(File corpusDir) {
        if (!corpusDir.exists() || !corpusDir.isDirectory()) return false;
        File archivesDir = new File(corpusDir, "archives");
        return archivesDir.exists() && archivesDir.isDirectory() && Objects.requireNonNull(archivesDir.listFiles()).length > 0;
    }

    public static void sourceCorpora(String url) {
        System.out.println("Sourcing up corpora");
        File corpusRoot = new File(BASE_PATH);
        File standard1 = new File(corpusRoot, "canterbury");
        File standard2 = new File(corpusRoot, "silesia");
        File standard3 = new File(corpusRoot, "wikipedia");

        if (!isValidCorpus(standard1) || !isValidCorpus(standard2) || !isValidCorpus(standard3)) {

            System.out.println("Standard corpora incomplete. \nClearing download directory...");
            sinkCorpora("random", "canterbury", "silesia", "wikipedia");
            System.out.println("Preparing to download from Google Drive: " +url);
            try {
                 corpusRoot.mkdirs();
                 File zipFile = new File(corpusRoot, "corpora.zip");


                downloadCorpora(url, zipFile);

                System.out.println(); // New line after download

                 System.out.println("Download complete. Unpacking...");
                 unzip(zipFile, corpusRoot.getParentFile());
                 System.out.println("Unpacking complete. Cleaning up...");
                 zipFile.delete();

                 // Verify existence
                 if (!isValidCorpus(standard1)) throw new FileNotFoundException("Failed to unpack corpus: canterbury archives missing or empty");

             } catch (IOException exception) {
                 throw new RuntimeException("Corpus setup failed", exception);
             }
        }

        System.out.println("Using previously downloaded corpora");
    }

    private static void printProgress(long current, long total) {
        if (total <= 0) {
            System.out.print(String.format("\rDownloaded: %.2f MB", current / (1024.0 * 1024.0)));
            return;
        }

        int width = 50; // Width of progress bar
        int percent = (int) ((current * 100) / total);
        int progress = (int) ((current * width) / total);

        StringBuilder bar = new StringBuilder("Downloading: [");
        for (int i = 0; i < width; i++) {
            if (i < progress) bar.append("#");
            else bar.append("-");
        }
        bar.append("]");

        String sizeInfo = String.format("(%.2fMB/%.2fMB)", current / (1024.0 * 1024.0), total / (1024.0 * 1024.0));
        System.out.print(String.format("\r%s %d%% %s", bar.toString(), percent, sizeInfo));
    }

    public static void sinkCorpora(String... corpus) {
        for(int i = 0; i < corpus.length; i++) {
            sinkCorpus(corpus[i]);
        }
    }

    public static void sinkCorpus(String corpusName) {
        File corpusDir = new File(BASE_PATH, corpusName);
        if (!corpusDir.exists()) return;

        File[] files = corpusDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().equalsIgnoreCase("README.md")) {
                    deleteRecursively(file);
                }
            }
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                deleteRecursively(c);
            }
        }
        file.delete();
    }

    public static void initializeCorpora(Object[] random, String... corpus) {
        int count = (int) random[0];
        int[] B = (int[]) random[1];
        int[] KB = (int[]) random[2];
        int[] MB = (int[]) random[3];

        for(int i = 0; i < corpus.length; i++) {
            createCorpusContents(corpus[i]);
            createCorpusBlobs(corpus[i]);
        }
        createCorpusRandoms(count, B, KB, MB);
    }

    public static void createCorpusContents(String corpusName) {
        System.out.println("creating " +corpusName + " corpus contents...");
        File corpusDir = new File(BASE_PATH, corpusName);
        File archivesDir = new File(corpusDir, "archives");
        File contentsDir = new File(corpusDir, "contents");

        if (archivesDir.exists() && (!contentsDir.exists() || Objects.requireNonNull(contentsDir.listFiles()).length == 0)) {
            if (!contentsDir.exists()) contentsDir.mkdirs();
            File[] archives = archivesDir.listFiles((dir, name) -> name.endsWith(".zip"));
            if (archives != null) {
                for (File archive : archives) {
                    String folderName = archive.getName().substring(0, archive.getName().length() - 4);
                    File targetDir = new File(contentsDir, folderName);
                    if (!targetDir.exists()) targetDir.mkdirs();
                    unzip(archive, targetDir);

                    // Check for redundant nesting (e.g. silesia/silesia)
                    File nestedDir = new File(targetDir, folderName);
                    if (nestedDir.exists() && nestedDir.isDirectory()) {
                        File[] children = targetDir.listFiles();
                        if (children != null && children.length == 1 && children[0].equals(nestedDir)) {
                            // Move valid contents up
                            File[] nestedFiles = nestedDir.listFiles();
                            if (nestedFiles != null) {
                                for (File f : nestedFiles) {
                                    f.renameTo(new File(targetDir, f.getName()));
                                }
                            }
                            nestedDir.delete();
                        }
                    }
                }
            }
        }
    }

    public static void createCorpusBlobs(String corpusName) {
        System.out.println("creating " +corpusName + " corpus blobs...");
        File corpusDir = new File(BASE_PATH, corpusName);
        File contentsDir = new File(corpusDir, "contents");
        File blobsDir = new File(corpusDir, "blobs");

        if (contentsDir.exists()) {
             if (!blobsDir.exists()) blobsDir.mkdirs();

             File[] children = contentsDir.listFiles();
             if (children == null) return;

             // 1. Handle folders -> text.blob, image.blob etc
             for (File child : children) {
                 if (child.isDirectory()) {
                     File blobFile = new File(blobsDir, child.getName() + ".blob");
                     Generator.toBlob(child.getAbsolutePath(), blobFile.getAbsolutePath());
                 }
             }

             // 2. Handle loose files -> extra.blob
             List<File> looseFiles = Arrays.stream(children)
                 .filter(File::isFile)
                 .filter(f -> !f.getName().startsWith(".")) // Ignore hidden files like .DS_Store
                 .collect(Collectors.toList());

             if (!looseFiles.isEmpty()) {
                 File extraDir = new File(contentsDir, ".extra_files_temp");
                 extraDir.mkdirs();
                 try {
                     for (File f : looseFiles) {
                         Files.copy(f.toPath(), new File(extraDir, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                     }
                     File blobFile = new File(blobsDir, "extra.blob");
                     Generator.toBlob(extraDir.getAbsolutePath(), blobFile.getAbsolutePath());
                 } catch (IOException e) {
                     e.printStackTrace();
                 } finally {
                     deleteRecursively(extraDir);
                 }
             }
        }
    }

    public static void createCorpusRandoms(int count, int[] B, int[] KB, int[] MB) {
         System.out.println("creating random corpus files...");
         generateRandoms("B", B, count);
         generateRandoms("KB", KB, count);
         generateRandoms("MB", MB, count);
    }

    private static void generateRandoms(String unit, int[] sizes, int count) {
        if (sizes == null) return;
        File randomDir = new File(BASE_PATH, "random/" + unit);
        for (int size : sizes) {
             File dir = new File(randomDir, size + unit);
             if (!dir.exists()) dir.mkdirs();
             for (int i=1; i<=count; i++) {
                 File f = new File(dir, i + ".random");
                 if (!f.exists()) {
                     int sunit = size;
                     if (unit.equals("B")) sunit |= Generator.B;
                     if (unit.equals("KB")) sunit |= Generator.KB;
                     if (unit.equals("MB")) sunit |= Generator.MB;
                     Generator.setRandomFile(sunit, i);
                 }
             }
        }
    }

    /**
     * Cleans the sandbox testing directory of any leftover temp files.
     */
    private void cleanSandbox() {
        File sandbox = new File(SANDBOX_TESTING);
        if (sandbox.exists()) {
            File[] files = sandbox.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    /**
     * Executes benchmarks and generates a report.
     * @return Path to the generated report file.
     */
    public String run() throws IOException {
        startTime = System.nanoTime();
        log("Starting Benchmark Suite: " + title);
        if (codecIds.isEmpty()) {
            throw new IllegalStateException("No codecs added for benchmarking.");
        }

        // Ensure sandbox testing directory exists and is clean
        cleanSandbox();
        new File(SANDBOX_TESTING).mkdirs();
        log("Sandbox cleaned and initialized at " + SANDBOX_TESTING);

        this.totalTasks = 0;
        this.completedTasks = 0;

        String[] standardCorpora = {"canterbury", "silesia", "wikipedia"};
        String[] subTypes = {"archives", "contents", "blobs"};

        for (String corpusName : standardCorpora) {
            File corpusDir = new File(BASE_PATH, corpusName);
            if (!corpusDir.exists()) continue;
            for (String type : subTypes) {
                File typeDir = new File(corpusDir, type);
                if (typeDir.exists()) {
                    long count = type.equals("contents") ? listRecursive(typeDir).size() : listFiles(typeDir).size();
                    this.totalTasks += count * codecIds.size();
                }
            }
        }

        File rDir = new File(BASE_PATH, "random");
        if (rDir.exists()) {
            String[] rUnits = {"B", "KB", "MB"};
            for (String unit : rUnits) {
                File unitDir = new File(rDir, unit);
                if (unitDir.exists()) {
                    long count = listRecursive(unitDir).stream().filter(f -> f.getName().endsWith(".random")).count();
                    this.totalTasks += count * codecIds.size();
                }
            }
        }

        // Corpus -> SubType -> List<Result>
        Map<String, Map<String, List<BenchmarkResult>>> corpusResults = new LinkedHashMap<>();

        for (String corpusName : standardCorpora) {
            File corpusDir = new File(BASE_PATH, corpusName);
            if (!corpusDir.exists()) continue;

            System.out.println("Processing " + corpusName + "...");
            log("Processing standard corpus: " + corpusName);
            Map<String, List<BenchmarkResult>> subResults = new LinkedHashMap<>();

            for (String type : subTypes) {
                File typeDir = new File(corpusDir, type);
                if (typeDir.exists()) {
                    List<BenchmarkResult> results = new ArrayList<>();
                    if (type.equals("contents")) {
                        // "contents" should be recursive and use relative paths
                        List<File> files = listRecursive(typeDir);
                        log("  Processing sub-type: " + type + " (" + files.size() + " files)");
                        for (File f : files) {
                            for (int id : codecIds) {
                                BenchmarkResult r = benchmarkCodec(f, id, type, typeDir, corpusName);
                                if (r != null) results.add(r);
                                completedTasks++;
                            }
                        }
                    } else {
                        // "archives" and "blobs" are flat
                        List<File> files = listFiles(typeDir);
                        log("  Processing sub-type: " + type + " (" + files.size() + " files)");
                        for (File f : files) {
                            for (int id : codecIds) {
                                BenchmarkResult r = benchmarkCodec(f, id, type, null, corpusName);
                                if (r != null) results.add(r);
                                completedTasks++;
                            }
                        }
                    }
                    subResults.put(type, results);
                }
            }
            System.out.println();
            corpusResults.put(corpusName, subResults);
        }

        // Random Corpus
        File randomDir = new File(BASE_PATH, "random");
        if (randomDir.exists()) {
            System.out.println("Processing random...");
            log("Processing Random corpus");
            Map<String, List<BenchmarkResult>> subResults = new LinkedHashMap<>();
            String[] units = {"B", "KB", "MB"};

            for (String unit : units) {
                File unitDir = new File(randomDir, unit);
                if (unitDir.exists()) {
                    List<File> files = listRecursive(unitDir); // random files are nested in size folders
                    List<BenchmarkResult> results = new ArrayList<>();
                    log("  Processing sub-type: " + unit + " (" + files.size() + " files)");
                    for (File f : files) {
                        if (!f.getName().endsWith(".random")) continue;
                        for (int id : codecIds) {
                            results.add(benchmarkCodec(f, id, unit, null, "random")); // Random files don't need relative path logic usually as name is unique enough x.random
                            completedTasks++;
                        }
                    }
                    subResults.put(unit, results);
                }

            }
            System.out.println();
            corpusResults.put("random", subResults);
        }

        String reportContent = generateReport(corpusResults);
        log("Report content generated (" + reportContent.length() + " bytes)");

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String reportName = String.format("Benchmarking(%d)-%s.md", codecIds.size(), timestamp);

        Path resultsDir = Paths.get(resultsPath);
        Files.createDirectories(resultsDir);
        Path reportPath = resultsDir.resolve(reportName);
        Files.writeString(reportPath, reportContent);

        // Clean up sandbox testing files
        cleanSandbox();

        System.out.println("\n✅ Benchmark complete! Report saved to: " + reportPath);
        return reportPath.toString();
    }


    private static void downloadCorpora(String urlString, File destination) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String cookies = "";

        // Follow initial redirects manually to capture cookies
        connection.setInstanceFollowRedirects(false);
        int status = connection.getResponseCode();

        // Limited redirect loop
        for (int i=0; i<10; i++) {
            // Capture cookies
            List<String> setCookies = connection.getHeaderFields().get("Set-Cookie");
            if (setCookies != null) {
                for (String c : setCookies) {
                    if (!cookies.isEmpty()) cookies += "; ";
                    cookies += c.split(";", 2)[0];
                }
            }

            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == 303 || status == 307 || status == 302) {
                String loc = connection.getHeaderField("Location");
                url = new URL(loc);
                connection = (HttpURLConnection) url.openConnection();
                if (!cookies.isEmpty()) connection.setRequestProperty("Cookie", cookies);
                connection.setInstanceFollowRedirects(false);
                status = connection.getResponseCode();
                continue;
            }
            break;
        }

        // Check for warning page
        String contentType = connection.getContentType();
        if (contentType != null && (contentType.startsWith("text/html") || contentType.startsWith("application/xhtml+xml"))) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (InputStream in = connection.getInputStream()) {
                byte[] data = new byte[1024];
                int n;
                while ((n = in.read(data)) != -1) buffer.write(data, 0, n);
            }
            String html = buffer.toString("UTF-8");

            if (html.contains("Virus scan warning") || html.contains("Download anyway")) {
                String confirm = extractValue(html, "name=\"confirm\" value=\"([^\"]+)\"");
                String uuid = extractValue(html, "name=\"uuid\" value=\"([^\"]+)\"");
                String action = extractValue(html, "action=\"([^\"]+)\"");
                String id = extractValue(html, "name=\"id\" value=\"([^\"]+)\"");

                // Fallback if action not found
                if (action == null) action = "https://drive.usercontent.google.com/download";

                // Fix relative action
                if (action.startsWith("/")) {
                    action = url.getProtocol() + "://" + url.getHost() + action;
                }

                String nextUrl = action + "?id=" + (id != null ? id : "1v-sSUOwrQC4q17irRSgrDxTVXmj8zplc") + "&export=download&confirm=" + (confirm != null ? confirm : "t");
                if (uuid != null) nextUrl += "&uuid=" + uuid;

                url = new URL(nextUrl);
                connection = (HttpURLConnection) url.openConnection();
                if (!cookies.isEmpty()) connection.setRequestProperty("Cookie", cookies);
                status = connection.getResponseCode();
            }
        }

        if (status != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned HTTP " + status + " " + connection.getResponseMessage());
        }

        long contentLength = connection.getContentLengthLong();
        try (InputStream in = connection.getInputStream();
             FileOutputStream fos = new FileOutputStream(destination)) {
            byte[] buf = new byte[8192];
            long totalRead = 0;
            int n;
            while ((n = in.read(buf)) != -1) {
                fos.write(buf, 0, n);
                totalRead += n;
                printProgress(totalRead, contentLength);
            }
        }
    }

    private static String extractValue(String html, String regex) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(html);
        return m.find() ? m.group(1) : null;
    }

    public static void main(String[] args) {
        try {
            sourceCorpora("https://drive.google.com/uc?export=download&id=1v-sSUOwrQC4q17irRSgrDxTVXmj8zplc");

            Object[] random = new Object[4];
            random[0] = 5; // count
            random[1] = new int[]{1, 2, 4, 8, 16, 32, 64, 128, 256, 512}; // B
            random[2] = new int[]{1, 2, 4, 8, 16, 32, 64, 128, 256, 512}; // KB
            random[3] = new int[]{1, 2, 4, 8, 16, 32, 64};                // MB

            initializeCorpora(random, "canterbury", "silesia", "wikipedia");

            Benchmarking benchmarking = new Benchmarking("Reductions");
            benchmarking.showLogs(true); // Enable detailed logs

            // Create Light Codec on the fly (no-op baseline)
            Transcoder.Codec lightCodec = new Transcoder.Codec("Light") {
                @Override
                protected void onFooter(String name) {
                   footer.put("name", name);
                   footer.put("library", "Custom");
                   footer.put("version", "1.0");
                   footer.put("streaming", "Yes");
                   footer.put("notes", "Entropy-defying custom codec");
                }
                @Override
                public void onCompress(File input, File output) throws Exception {
                    Files.copy(input.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                @Override
                public void onDecompress(File input, File output) throws Exception {
                    Files.copy(input.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            };

            int lightCodecId = benchmarking.getTranscoder().getUniqueIdentifier();
            benchmarking.addCodec(lightCodecId, lightCodec);

            // Add other codecs for comparison
            benchmarking.addCodec(Transcoder.Codec.GZIP);
            benchmarking.addCodec(Transcoder.Codec.ZSTANDARD);

            benchmarking.run();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
