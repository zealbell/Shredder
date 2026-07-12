package com.linkersoft.reductions;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.ThreadMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;

/**
 * Benchmarking facilitates comprehensive performance testing of compression codecs by wrapping
 * {@link Transcoder}, driving each codec through a full compress/decompress round-trip and turning
 * the collected measurements into a detailed Markdown report.
 *
 * <h2>Class layout</h2>
 * This type is an <b>abstract-in-spirit base</b> that owns only the state and helpers shared by
 * every benchmark run; it has no {@code run()} of its own. The two concrete flavours are provided
 * as nested {@code static} subclasses so callers reference them as {@code Benchmarking.Corpora}
 * and {@code Benchmarking.Target}:
 * <ul>
 *   <li>{@link Corpora} &mdash; the whole-suite benchmark. It sources, downloads and prepares the
 *       standard corpora, then benchmarks <em>every</em> file found under
 *       {@code src/main/resources/benchmarking/corpora}.</li>
 *   <li>{@link Target} &mdash; the single-file benchmark. It opens a Swing file picker and benchmarks
 *       only the one file the user selects, with no download or corpus preparation.</li>
 * </ul>
 * Both flavours run the identical per-file measurement pipeline and reuse the identical report
 * generators; they differ only in <em>what</em> files they feed in and <em>which</em> report
 * template they fill.
 *
 * <h2>Shared responsibilities (this class)</h2>
 * <ul>
 *   <li><b>Codec registry</b> &mdash; {@link #addCodec(int)} / {@link #addCodec(int, Transcoder.Codec)}
 *       register built-in or custom codecs against the wrapped {@link Transcoder}.</li>
 *   <li><b>Measurement</b> &mdash; {@link #benchmarkCodec} compresses then decompresses a file,
 *       capturing size, compression ratio, space savings, throughput (MB/s), Shannon entropy,
 *       bits-per-byte, peak memory, CPU utilization and a CRC32 integrity check into a
 *       {@link BenchmarkResult}.</li>
 *   <li><b>Sandboxing</b> &mdash; intermediate compressed/decompressed artefacts are written to
 *       {@code src/main/resources/benchmarking/sandbox/testing} and cleaned before and after each run.</li>
 *   <li><b>Report rendering</b> &mdash; the {@code generate*} helpers (key findings, participating
 *       codecs, comparison tables, performance tables, per-codec detail tables, conclusion) emit the
 *       HTML/Markdown fragments substituted into the report templates. {@link #writeReport} persists
 *       the finished report under {@link #resultsPath}.</li>
 *   <li><b>Progress logging</b> &mdash; {@link #showLogs(boolean)} enables a single-line elapsed-time
 *       progress indicator.</li>
 * </ul>
 *
 * <h2>Filesystem layout</h2>
 * Every path is a top-level constant derived from a single {@link #BASE} root, so relocating the
 * whole suite means changing one constant. {@link #CORPORA} (formerly {@code Corpora.BASE_PATH}) is
 * the corpus directory; the transient scratch and report directories hang off {@link #SANDBOX}
 * ({@link #SANDBOX_TESTING} for intermediate artefacts, {@link #SANDBOX_RESULTS} for generated
 * reports). Both blueprint templates live under {@link #TEMPLATES} ({@code results/templates}) and
 * are referenced via {@link #CORPORA_BLUEPRINT} and {@link #TARGET_BLUEPRINT}.
 *
 * <h2>Reports</h2>
 * Each flavour fills its own template via {@code {{PLACEHOLDER}}} substitution:
 * <ul>
 *   <li>{@link Corpora} uses
 *       <a href="../../../../../resources/benchmarking/sandbox/results/templates/corpora-blueprint.md">{@code corpora-blueprint.md}</a>
 *       and writes {@code Corpora-Benchmark(N)-dd-MM-yyyy~HH~mm~ss.md}, where {@code N} is the number of participating codecs.</li>
 *   <li>{@link Target} uses
 *       <a href="../../../../../resources/benchmarking/sandbox/results/templates/target-blueprint.md">{@code target-blueprint.md}</a>
 *       (a single-file mirror of the corpora blueprint &mdash; same section titles and Metrics Overview,
 *       minus the multi-corpus structure/comparison sections) and writes {@code Target-Benchmark(fileName)-dd-MM-yyyy~HH~mm~ss.md}.</li>
 * </ul>
 *
 * <h2>Corpus directory organization (Canterbury, Silesia, Wikipedia)</h2>
 * <ul>
 *   <li><b>archives/</b>: Original compressed archives (e.g. {@code .zip}) of the corpus data.</li>
 *   <li><b>contents/</b>: Files extracted from the archives, compressed individually to stress codecs
 *       on smaller inputs.</li>
 *   <li><b>blobs/</b>: All content files aggregated into a single binary via {@link Generator#toBlob}.</li>
 * </ul>
 * The {@code random/} corpus holds cryptographically generated files bucketed by size ({@code B}, {@code KB}, {@code MB}).
 *
 * @see Corpora
 * @see Target
 * @see Transcoder
 */
public class Benchmarking {

    // Filesystem layout. Every benchmarking path is derived from BASE, so relocating the whole
    // suite only requires changing BASE (or SANDBOX for the transient/output directories).
    
    /** Root of the benchmarking resources folder; the single source of truth for every path below. */
    protected static final String BASE = "src/main/resources/benchmarking";
    /** Corpora directory holding the standard and random corpora (renamed from Corpora's BASE_PATH). */
    protected static final String CORPORA = BASE + "/corpora";
    /** Sandbox root for transient benchmark artefacts and generated reports. */
    protected static final String SANDBOX = BASE + "/sandbox";
    /** Scratch directory for intermediate compressed/decompressed files produced during a run. */
    protected static final String SANDBOX_TESTING = SANDBOX + "/testing";
    /** Default output directory for generated Markdown reports. */
    protected static final String SANDBOX_RESULTS = SANDBOX + "/results";
    /** Directory holding the report blueprint templates. */
    protected static final String TEMPLATES = SANDBOX_RESULTS + "/templates";
    /** Report template for the whole-suite corpora benchmark. */
    protected static final String CORPORA_BLUEPRINT = TEMPLATES + "/corpora-blueprint.md";
    /** Report template for the single-file target benchmark. */
    protected static final String TARGET_BLUEPRINT = TEMPLATES + "/target-blueprint.md";
    /** Name prefix given to the parallel benchmark worker threads (e.g. {@code Thread[0]}). */
    protected static final String THREAD_NAME_PREFIX = "Thread";


    protected final Transcoder transcoder;
    protected final List<Integer> codecIds;
    protected String resultsPath;
    protected String title;
    protected Map<Integer, String> codecNotes = new HashMap<>();
    protected boolean showLogs = false;
    protected long startTime;
    protected LocalDateTime startDateTime; // wall-clock time the current run began
    protected int totalTasks = 0;
    protected final AtomicInteger completedTasks = new AtomicInteger(0);
    // Per-codec timing captured across parallel worker threads, keyed by codec id, ordered by registration.
    protected final Map<Integer, CodecTiming> codecTimings = new ConcurrentHashMap<>();

    protected MemoryMXBean memoryBean;
    protected List<MemoryPoolMXBean> memoryPools;

    protected String getConciseTime(long nanos) {
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
    protected static class BenchmarkResult {
        String fileName;
        String subType; // archives, contents, blobs, B, KB, MB, file
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

    // Timing for one codec's whole benchmarking task, recorded on the worker thread (or main thread) that ran it.
    protected static class CodecTiming {
        String codecName;
        String thread; // "main" for the non-parallel codec, else the worker thread name e.g. "Thread[2]"
        LocalDateTime started;
        LocalDateTime ended;
        long durationNanos;
    }

    // One unit of benchmarking work: a single file to run, tagged with its corpus and sub-type so
    // results can be aggregated back into the corpus -> sub-type -> file structure the report expects.
    protected static class Cell {
        final File file;
        final String subType;
        final File rootDir;   // for relative naming (contents), null otherwise
        final String corpus;  // e.g. "canterbury", "random", "target"
        Cell(File file, String subType, File rootDir, String corpus) {
            this.file = file;
            this.subType = subType;
            this.rootDir = rootDir;
            this.corpus = corpus;
        }
    }

    /**
     * Creates a new Benchmarking instance with default settings.
     * @param title The title of the benchmarking suite (e.g., "Harmattan").
     */
    public Benchmarking(String title) {
        this.title = title;
        this.transcoder = new Transcoder();
        this.codecIds = new ArrayList<>();
        this.resultsPath = SANDBOX_RESULTS;
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.memoryPools = ManagementFactory.getMemoryPoolMXBeans();
    }

    public Benchmarking() {
        this("Harmattan");
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

    protected void log(String message) {
        if (showLogs) {
            long elapsed = System.nanoTime() - startTime;
            String timeStr = formatDuration(elapsed);
            // Pad with spaces to clear previous longer lines
            System.out.print("\r[" + timeStr + "] " + message + "                                                  ");
        }
    }

    protected String formatDuration(long nanos) {
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

    protected long getPeakMemoryUsage() {
        long peak = 0;
        for (MemoryPoolMXBean pool : memoryPools) {
            peak += pool.getPeakUsage().getUsed();
        }
        // Note: This sums peak usage of all JVM memory pools (Heap + Non-Heap).
        // It may not capture:
        // 1. Native memory allocated directly by JNI codecs (outside JVM pools).
        // 2. Short-lived allocations if GC collects them before peak is registered (though peak should track max).
        // 3. For very small files, the overhead might be negligible (< 1MB).
        return peak;
    }

    protected void resetPeakMemoryUsage() {
        for (MemoryPoolMXBean pool : memoryPools) {
            pool.resetPeakUsage();
        }
    }

    /**
     * Benchmarks a single codec over a single file: compresses, decompresses and measures the round-trip.
     * <p>
     * To make parallel per-codec execution safe, this operates on a private {@code transcoder} and a
     * private {@code workDir}: the input is first copied into {@code workDir} so the transcoder's
     * derived {@code <name>.compressed} / {@code <name>.decompressed} artefacts land in that isolated
     * directory and never collide with another codec running the same file on a different worker thread.
     * </p>
     * <p>
     * <b>Note:</b> peak-memory attribution ({@link #getPeakMemoryUsage()}) reads JVM-global pools and
     * is therefore only reliable when codecs run one-at-a-time; under parallel worker threads the memory
     * figure is approximate. CPU time is per-thread and remains accurate per codec.
     * </p>
     * @param transcoder A transcoder that has this codec registered (one per codec/worker).
     * @param workDir A directory private to this codec for transient compress/decompress artefacts.
     */
    protected BenchmarkResult benchmarkCodec(Transcoder transcoder, File workDir, File testFile, int codecId, String subType, File rootDir, String testSection) {
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

        workDir.mkdirs();

        String conciseTime = getConciseTime(System.nanoTime() - startTime);
        int percent = totalTasks > 0 ? (int) ((completedTasks.get() * 100L) / totalTasks) : 0;
        log("Benchmarking " + result.codecName + " on " + result.fileName + " (" + result.subType + ") - " + testSection + " | " + conciseTime + " | " + percent + "% Done");

        File localInput = null;
        try {
            // Isolated copy of the input so the transcoder's output paths stay inside workDir.
            localInput = new File(workDir, testFile.getName());
            Files.copy(testFile.toPath(), localInput.toPath(), StandardCopyOption.REPLACE_EXISTING);

            transcoder.setInput(localInput);
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
        } finally {
            if (localInput != null) localInput.delete();
        }

        return result;
    }

    protected List<File> listFiles(File dir) {
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

    protected List<File> listRecursive(File dir) {
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

    protected String generateKeyFindings(Map<String, Map<String, List<BenchmarkResult>>> allResults) {
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

    protected String generateCodecList() {
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

    protected String generateComparisonTable(Map<String, List<BenchmarkResult>> data, String key) {
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

    protected String generatePerformanceTable(Map<String, List<BenchmarkResult>> data) {
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
    protected String generateDetailTables(Map<String, List<BenchmarkResult>> data, String[] subLabels) {
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

    protected boolean isRandomSubType(String[] labels) {
        return labels.length > 0 && (labels[0].equals("B") || labels[0].equals("KB") || labels[0].equals("MB"));
    }

    protected String generateComparativeAnalysis(Map<String, Map<String, List<BenchmarkResult>>> allResults) {
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

    protected String generateConclusion(Map<String, Map<String, List<BenchmarkResult>>> allResults) {
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

    protected String getPotential(double entropy) {
        if (entropy < 2.0) return "Excellent";
        if (entropy < 4.0) return "Very High";
        if (entropy < 5.0) return "High";
        if (entropy < 6.0) return "Moderate";
        if (entropy < 7.5) return "Low";
        return "None";
    }

    protected String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    protected String formatDecimal(double value) {
        if (value >= 1000) {
            return String.format("%,.1f", value);
        }
        return String.format("%.1f", value);
    }

    protected String formatMemory(long bytes) {
        if (bytes <= 0) return "< 1 MB";
        long mb = bytes / (1024 * 1024);
        if (mb < 1) return "< 1 MB";
        return mb + " MB";
    }

    /**
     * Formats a byte count as a compact size for the folder tree, choosing the closest 1024-based unit
     * so the number stays readable (e.g. {@code 4GB}, {@code 3.5MB}, {@code 512B}). Units ascend
     * B &rarr; KB &rarr; MB &rarr; GB &rarr; TB &rarr; PB &rarr; EB where 1KB = 1024B, 1MB = 1024KB, etc.
     * (1B = 8 bits); values below one byte fall back to bits. At most one decimal is shown and a
     * trailing {@code .0} is dropped.
     */
    protected String formatTreeSize(long bytes) {
        if (bytes <= 0) return "0B";
        if (bytes < 1) return (bytes * 8) + "bit"; // sub-byte fallback (1B = 8bits)

        String[] units = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        double size = bytes;
        int unit = 0;
        while (size >= 1024 && unit < units.length - 1) {
            size /= 1024;
            unit++;
        }

        // Compact number: whole values render without a decimal, otherwise one decimal place.
        String number = (size == Math.floor(size))
            ? String.format("%.0f", size)
            : String.format("%.1f", size);
        return number + units[unit];
    }

    /**
     * Sums the size of every file contained in {@code file}, recursively. A single file returns its own
     * length; a directory returns the combined length of all files beneath it.
     */
    protected long folderSize(File file) {
        if (file == null || !file.exists()) return 0;
        if (file.isFile()) return file.length();
        long total = 0;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                total += folderSize(child);
            }
        }
        return total;
    }

    protected String getEntropyColor(double entropy) {
        if (entropy < 2.0) return "#fff0f0";
        if (entropy < 4.0) return "#ffe0e0";
        if (entropy < 6.0) return "#ffcccc";
        if (entropy < 7.5) return "#ff9999";
        return "#ff6666";
    }

    protected String getRatioColor(double ratio) {
        return ratio < 0.80 ? "#e6ffe6" : "#ffe6e6"; // Light green for good, light red otherwise
    }

    /**
     * Cleans the sandbox testing directory of any leftover temp files.
     */
    protected void cleanSandbox() {
        File sandbox = new File(SANDBOX_TESTING);
        if (sandbox.exists()) {
            File[] files = sandbox.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteRecursively(f); // recursive: also clears any leftover per-codec work dirs
                }
            }
        }
    }

    /**
     * Writes the given report content to the results directory under the supplied name.
     * @param reportName File name for the report (e.g. {@code Corpora-Benchmark(3)-16-02-2026:14:30:05.md}).
     * @param reportContent The Markdown content to write.
     * @return The path of the written report.
     */
    protected String writeReport(String reportName, String reportContent) throws IOException {
        log("Report content generated (" + reportContent.length() + " bytes)");

        Path resultsDir = Paths.get(resultsPath);
        Files.createDirectories(resultsDir);
        Path reportPath = resultsDir.resolve(reportName);
        Files.writeString(reportPath, reportContent);
        return reportPath.toString();
    }

    /**
     * Substitutes the session-timing placeholders shared by both report templates, capturing the
     * timing of the entire benchmarking session:
     * <ul>
     *   <li>{@code {{START_TIME}}} &mdash; when the run began ({@code dd-MM-yyyy:HH:mm:ss}).</li>
     *   <li>{@code {{END_TIME}}} &mdash; the moment this method is called, i.e. the run's end
     *       ({@code dd-MM-yyyy:HH:mm:ss}).</li>
     *   <li>{@code {{DURATION}}} &mdash; elapsed wall-clock time as days/hours/minutes/seconds/ms.</li>
     *   <li>{@code {{CODEC_SCHEDULE_TABLE}}} &mdash; the per-codec start/end/duration breakdown so the
     *       time each codec actually took can be read despite the amortization of parallel worker threads.</li>
     * </ul>
     * Call this after the run has completed so the end time and duration cover the whole session.
     * @param template The report template with the placeholders above.
     * @return The template with the session-timing placeholders substituted.
     */
    protected String applySessionTiming(String template) {
        DateTimeFormatter stamp = DateTimeFormatter.ofPattern("dd-MM-yyyy:HH:mm:ss");
        LocalDateTime end = LocalDateTime.now();
        String duration = formatSessionDuration(System.nanoTime() - startTime);
        return template
            .replace("{{START_TIME}}", startDateTime != null ? startDateTime.format(stamp) : "-")
            .replace("{{END_TIME}}", end.format(stamp))
            .replace("{{DURATION}}", duration)
            .replace("{{CODEC_SCHEDULE_TABLE}}", generateCodecSchedule());
    }

    /**
     * Formats a nanosecond duration as {@code d days, h hours, m minutes, s seconds, ms ms}. The
     * milliseconds component is always included so short runs (where whole seconds are zero) still
     * report a meaningful figure.
     */
    protected String formatSessionDuration(long nanos) {
        Duration elapsed = Duration.ofNanos(nanos);
        return String.format("%d days, %d hours, %d minutes, %d seconds, %d ms",
            elapsed.toDays(), elapsed.toHoursPart(), elapsed.toMinutesPart(),
            elapsed.toSecondsPart(), elapsed.toMillisPart());
    }

    /**
     * Renders the per-codec session schedule: a table of Codec vs Thread/worker, Started, Ended and
     * Duration, in codec-registration order. Because codecs are benchmarked simultaneously on separate
     * worker threads, this exposes each codec's own wall-clock cost independent of the overall session time.
     */
    protected String generateCodecSchedule() {
        DateTimeFormatter stamp = DateTimeFormatter.ofPattern("dd-MM-yyyy:HH:mm:ss");
        StringBuilder builder = new StringBuilder();
        builder.append("<table>\n");
        builder.append("  <thead>\n    <tr>\n      <th>Codec</th>\n      <th>Thread</th>\n      <th>Started</th>\n      <th>Ended</th>\n      <th>Duration</th>\n    </tr>\n  </thead>\n");
        builder.append("  <tbody>\n");
        for (int id : codecIds) {
            CodecTiming timing = codecTimings.get(id);
            if (timing == null) continue;
            builder.append("    <tr>\n");
            builder.append(String.format("      <td>%s</td>\n", transcoder.getCodecName(id)));
            builder.append(String.format("      <td>%s</td>\n", timing.thread));
            builder.append(String.format("      <td>%s</td>\n", timing.started != null ? timing.started.format(stamp) : "-"));
            builder.append(String.format("      <td>%s</td>\n", timing.ended != null ? timing.ended.format(stamp) : "-"));
            builder.append(String.format("      <td>%s</td>\n", formatSessionDuration(timing.durationNanos)));
            builder.append("    </tr>\n");
        }
        builder.append("  </tbody>\n</table>");
        return builder.toString();
    }

    /**
     * Builds a fresh {@link Transcoder} carrying only the given codec, so each parallel task owns its
     * own transcoder (the shared one holds mutable input/analysis state that is not thread-safe). The
     * already-registered {@link Transcoder.Codec} instance is reused since each id is handled by exactly
     * one task on one thread.
     */
    protected Transcoder buildTranscoder(int codecId) {
        Transcoder isolated = new Transcoder();
        isolated.addCodec(codecId, transcoder.getCodec(codecId));
        return isolated;
    }

    /**
     * Runs one codec over every {@code cell} sequentially on the calling thread (a benchmark worker, or main),
     * timing the whole codec task and returning results aligned one-to-one with {@code cells}
     * (entries may be {@code null} where a cell was skipped). Uses an isolated transcoder and a
     * per-codec working directory so it is safe to invoke concurrently for different codecs.
     */
    protected List<BenchmarkResult> benchmarkCells(int codecId, List<Cell> cells) {
        Transcoder isolated = buildTranscoder(codecId);
        File workDir = new File(SANDBOX_TESTING, "codec-" + codecId);

        CodecTiming timing = new CodecTiming();
        timing.codecName = isolated.getCodecName(codecId);
        // Label the timing with the worker thread that ran it, or "main" for the non-parallel baseline codec.
        Thread current = Thread.currentThread();
        timing.thread = current.getName().startsWith(THREAD_NAME_PREFIX) ? current.getName() : "main";
        timing.started = LocalDateTime.now();
        long begin = System.nanoTime();

        List<BenchmarkResult> results = new ArrayList<>(cells.size());
        for (Cell cell : cells) {
            results.add(benchmarkCodec(isolated, workDir, cell.file, codecId, cell.subType, cell.rootDir, cell.corpus));
            completedTasks.incrementAndGet();
        }

        timing.durationNanos = System.nanoTime() - begin;
        timing.ended = LocalDateTime.now();
        codecTimings.put(codecId, timing);

        deleteRecursively(workDir); // tidy this codec's private scratch space
        return results;
    }

    /**
     * Benchmarks all registered codecs over {@code cells}, running them across a fixed pool of plain
     * {@link Thread} workers so multiple codecs run simultaneously (one codec per worker at a time).
     * When {@code firstOnMain} is set, the first-registered codec is run to completion on the calling
     * (main) thread <em>before</em> the rest are handed to the workers &mdash; this is how
     * {@link Corpora} keeps the "Light" baseline off the worker pool.
     * @return codec id &rarr; results aligned to {@code cells}.
     */
    protected Map<Integer, List<BenchmarkResult>> runCodecsParallel(List<Cell> cells, boolean firstOnMain) {
        Map<Integer, List<BenchmarkResult>> resultsByCodec = new ConcurrentHashMap<>();
        List<Integer> parallelCodecs = new ArrayList<>(codecIds);

        if (firstOnMain && !parallelCodecs.isEmpty()) {
            int mainCodec = parallelCodecs.remove(0);
            log("Benchmarking " + transcoder.getCodecName(mainCodec) + " on the main thread...");
            resultsByCodec.put(mainCodec, benchmarkCells(mainCodec, cells));
        }

        if (!parallelCodecs.isEmpty()) {
            // Spread the remaining codecs across a fixed pool of worker threads, capped at the available
            // parallelism (never more workers than codecs). Each worker pulls codec ids off a shared,
            // thread-safe queue and benchmarks one at a time until the queue is drained.
            int workerCount = Math.min(parallelCodecs.size(), getMaximumThreads());
            ConcurrentLinkedQueue<Integer> pendingCodecs = new ConcurrentLinkedQueue<>(parallelCodecs);
            log("Benchmarking " + parallelCodecs.size() + " codec(s) across up to " + workerCount + " worker thread(s)...");

            List<Thread> workers = new ArrayList<>(workerCount);
            for (int index = 0; index < workerCount; index++) {
                Thread worker = new Thread(() -> {
                    Integer codecId;
                    while ((codecId = pendingCodecs.poll()) != null) {
                        resultsByCodec.put(codecId, benchmarkCells(codecId, cells));
                    }
                }, THREAD_NAME_PREFIX + "[" + index + "]");
                workers.add(worker);
                worker.start();
            }

            // Block the calling (main) thread until every worker has finished (all codecs done).
            for (Thread worker : workers) {
                try {
                    worker.join();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt(); // preserve the interrupt and stop waiting on the rest
                    break;
                }
            }
        }

        return resultsByCodec;
    }

    /**
     * Maximum number of worker threads used to benchmark codecs in parallel: one per available
     * processor minus two, so some headroom is left for the OS and other native processes, clamped to
     * at least one so a small (&le; 2 core) machine still gets a single worker.
     */
    protected static int getMaximumThreads() {
        return Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
    }

    /**
     * Aggregates per-codec results back into the {@code corpus -> sub-type -> results} structure the
     * report generators expect. Iterating {@code cells} (grouped by corpus/sub-type, files in order)
     * on the outside and codec-registration order on the inside keeps the output deterministic
     * regardless of the order worker threads actually finished in.
     */
    protected Map<String, Map<String, List<BenchmarkResult>>> aggregateResults(List<Cell> cells, Map<Integer, List<BenchmarkResult>> resultsByCodec) {
        Map<String, Map<String, List<BenchmarkResult>>> corpusResults = new LinkedHashMap<>();
        for (int index = 0; index < cells.size(); index++) {
            Cell cell = cells.get(index);
            Map<String, List<BenchmarkResult>> subResults = corpusResults.computeIfAbsent(cell.corpus, key -> new LinkedHashMap<>());
            List<BenchmarkResult> list = subResults.computeIfAbsent(cell.subType, key -> new ArrayList<>());
            for (int codecId : codecIds) {
                List<BenchmarkResult> codecResults = resultsByCodec.get(codecId);
                if (codecResults == null) continue;
                BenchmarkResult result = codecResults.get(index);
                if (result != null) list.add(result);
            }
        }
        return corpusResults;
    }

    /**
     * Recursively deletes a file or directory tree. Shared by the parallel workers (to clear per-codec
     * scratch dirs) and by the corpus sink helpers.
     */
    protected static void deleteRecursively(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    /**
     * Corpora is the whole-suite benchmark: it benchmarks every file discovered under the corpora
     * directory ({@code src/main/resources/benchmarking/corpora}), preparing the corpora first when
     * necessary.
     *
     * <h3>Corpus preparation</h3>
     * The following static helpers build the corpus on disk before a run and are idempotent (they
     * skip work that is already present):
     * <ul>
     *   <li>{@link #sourceCorpora(String)} &mdash; validates the standard corpora and, if incomplete,
     *       downloads the corpora archive from the given URL (following redirects and handling Google
     *       Drive's virus-scan confirmation page) and unpacks it.</li>
     *   <li>{@link #initializeCorpora(Object[], String...)} &mdash; for each named corpus, extracts
     *       {@code archives/} into {@code contents/} ({@link #createCorpusContents}) and aggregates
     *       {@code contents/} into {@code blobs/} ({@link #createCorpusBlobs}); it also generates the
     *       {@code random/} corpus ({@link #createCorpusRandoms}).</li>
     *   <li>{@link #sinkCorpora(String...)} / {@link #sinkCorpus(String)} &mdash; clear generated
     *       corpus data (preserving {@code README.md}).</li>
     * </ul>
     *
     * <h3>Execution</h3>
     * {@link #run()} discovers every file to benchmark once (grouped by corpus and sub-type &mdash;
     * standard corpora by {@code archives}/{@code contents}/{@code blobs}, then {@code random} by
     * {@code B}/{@code KB}/{@code MB}) and then benchmarks the codecs over that set in parallel via
     * {@link #runCodecsParallel}: the first-registered codec (the "Light" baseline) runs to completion
     * on the main thread, after which the remaining codecs are benchmarked across the maximum available
     * worker threads. Results are aggregated in codec-registration order, fill {@link #CORPORA_BLUEPRINT}
     * ({@code results/templates/corpora-blueprint.md}) and are written as
     * {@code Corpora-Benchmark(N)-dd-MM-yyyy~HH~mm~ss.md}.
     *
     * <h3>Entry point</h3>
     * {@link #main(String[])} demonstrates a full run: it sources and initializes the corpora, then
     * registers a no-op "Light" baseline codec (Corpora-only) alongside GZIP and ZSTANDARD.
     */
    public static class Corpora extends Benchmarking {

        public Corpora(String title) {
            super(title);
        }

        public Corpora() {
            super();
        }

        public Corpora(String title, String resultsPath) {
            super(title, resultsPath);
        }

        private String generateReport(Map<String, Map<String, List<BenchmarkResult>>> allResults) throws IOException {
            String templatePath = CORPORA_BLUEPRINT;
            String template = Files.readString(Paths.get(templatePath));

            Duration d = Duration.ofNanos(System.nanoTime() - startTime);
            String durationStr = String.format("%dhrs %dmin %dsecs", d.toHours(), d.toMinutesPart(), d.toSecondsPart());

            LocalDateTime now = LocalDateTime.now();
            template = template.replace("{{TITLE}}", title)
                               .replace("{{TIMESTAMP}}", now.format(DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm:ss")))
                               .replace("{{CODEC_COUNT}}", codecIds.size() + " (in " + durationStr + ")")
                               .replace("{{PLATFORM_INFO}}", System.getProperty("os.name") + ", Java " + System.getProperty("java.version"))
                               .replace("{{DEVICE_INFO}}", Hardware.getDeviceInfo());

            // Session timing (start/end/duration) for the whole benchmarking session
            template = applySessionTiming(template);

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

            // Live folder tree with per-folder sizes
            template = template.replace("{{FOLDER_TREE}}", generateFolderTree());

            // Conclusion logic
            template = template.replace("{{CONCLUSION_TEXT}}", generateConclusion(allResults));

            return template;
        }

        /**
         * Renders the live corpora directory as an ASCII folder tree (directories only) with each
         * folder's total recursive size prepended to its branch, e.g. {@code ├─4GB── canterbury/}. The
         * root folder carries its size before the name too ({@code 8.8MB── corpora/}) and its children
         * are indented to align beneath it.
         */
        private String generateFolderTree() {
            File root = new File(CORPORA);
            if (!root.exists()) {
                return "(corpora not present)";
            }
            StringBuilder builder = new StringBuilder();
            // Root carries its size before the name too; children are indented to align under it.
            String rootPrefix = formatTreeSize(folderSize(root)) + "── ";
            builder.append(rootPrefix).append("corpora/\n");
            appendFolderTree(builder, root, " ".repeat(rootPrefix.length()));
            return builder.toString();
        }

        /**
         * Recursively appends the sub-directories of {@code dir} to {@code builder}, sorted by name and
         * skipping hidden folders. Each branch carries the folder's total size in its connector.
         */
        private void appendFolderTree(StringBuilder builder, File dir, String prefix) {
            File[] entries = dir.listFiles(File::isDirectory);
            if (entries == null) return;

            List<File> subDirectories = new ArrayList<>();
            for (File entry : entries) {
                if (!entry.getName().startsWith(".")) subDirectories.add(entry);
            }
            subDirectories.sort(Comparator.comparing(File::getName));

            for (int index = 0; index < subDirectories.size(); index++) {
                File child = subDirectories.get(index);
                boolean last = (index == subDirectories.size() - 1);
                String connector = last ? "└─" : "├─";
                builder.append(prefix)
                       .append(connector).append(formatTreeSize(folderSize(child))).append("── ")
                       .append(child.getName()).append("/\n");
                appendFolderTree(builder, child, prefix + (last ? "    " : "│   "));
            }
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

        private static boolean isValidCorpus(File corpusDir) {
            if (!corpusDir.exists() || !corpusDir.isDirectory()) return false;
            File archivesDir = new File(corpusDir, "archives");
            return archivesDir.exists() && archivesDir.isDirectory() && Objects.requireNonNull(archivesDir.listFiles()).length > 0;
        }

        public static void sourceCorpora(String url) {
            System.out.println("Sourcing up corpora");
            File corpusRoot = new File(CORPORA);
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
            File corpusDir = new File(CORPORA, corpusName);
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
            File corpusDir = new File(CORPORA, corpusName);
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
            File corpusDir = new File(CORPORA, corpusName);
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
            File randomDir = new File(CORPORA, "random/" + unit);
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
            Matcher m = Pattern.compile(regex).matcher(html);
            return m.find() ? m.group(1) : null;
        }

        /**
         * Executes benchmarks over every corpus and generates a report.
         * @return Path to the generated report file.
         */
        public String run() throws IOException {
            startTime = System.nanoTime();
            startDateTime = LocalDateTime.now();
            log("Starting Benchmark Suite: " + title);
            if (codecIds.isEmpty()) {
                throw new IllegalStateException("No codecs added for benchmarking.");
            }

            // Ensure sandbox testing directory exists and is clean
            cleanSandbox();
            new File(SANDBOX_TESTING).mkdirs();
            log("Sandbox cleaned and initialized at " + SANDBOX_TESTING);

            this.completedTasks.set(0);
            codecTimings.clear();

            String[] standardCorpora = {"canterbury", "silesia", "wikipedia"};
            String[] subTypes = {"archives", "contents", "blobs"};

            // Discover every file to benchmark once, as an ordered list of cells (grouped by corpus,
            // then sub-type, then sorted files). Each codec is later run over this identical list.
            List<Cell> cells = new ArrayList<>();

            for (String corpusName : standardCorpora) {
                File corpusDir = new File(CORPORA, corpusName);
                if (!corpusDir.exists()) continue;
                for (String type : subTypes) {
                    File typeDir = new File(corpusDir, type);
                    if (!typeDir.exists()) continue;
                    if (type.equals("contents")) {
                        // "contents" is recursive and uses relative paths (rootDir = typeDir)
                        for (File file : listRecursive(typeDir)) {
                            cells.add(new Cell(file, type, typeDir, corpusName));
                        }
                    } else {
                        // "archives" and "blobs" are flat
                        for (File file : listFiles(typeDir)) {
                            cells.add(new Cell(file, type, null, corpusName));
                        }
                    }
                }
            }

            File randomDir = new File(CORPORA, "random");
            if (randomDir.exists()) {
                String[] units = {"B", "KB", "MB"};
                for (String unit : units) {
                    File unitDir = new File(randomDir, unit);
                    if (!unitDir.exists()) continue;
                    for (File file : listRecursive(unitDir)) { // random files are nested in size folders
                        if (!file.getName().endsWith(".random")) continue;
                        cells.add(new Cell(file, unit, null, "random"));
                    }
                }
            }

            this.totalTasks = cells.size() * codecIds.size();

            // Light (first-registered) codec runs on the main thread; the rest run in parallel across worker threads.
            System.out.println("Benchmarking " + codecIds.size() + " codec(s) over " + cells.size() + " file(s)...");
            Map<Integer, List<BenchmarkResult>> resultsByCodec = runCodecsParallel(cells, true);
            Map<String, Map<String, List<BenchmarkResult>>> corpusResults = aggregateResults(cells, resultsByCodec);
            System.out.println();

            String reportContent = generateReport(corpusResults);

            LocalDateTime now = LocalDateTime.now();
            // '~' rather than ':' in the time part so the file name stays valid on Windows/other OSes.
            String timestamp = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy~HH~mm~ss"));
            String reportName = String.format("Corpora-Benchmark(%d)-%s.md", codecIds.size(), timestamp);

            String reportPath = writeReport(reportName, reportContent);

            // Clean up sandbox testing files
            cleanSandbox();

            System.out.println("\n✅ Benchmark complete! Report saved to: " + reportPath);
            return reportPath;
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

                Corpora benchmarking = new Corpora("Harmattan");
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

    /**
     * Target is the single-file benchmark: it benchmarks one file chosen by the user rather than a
     * whole corpus. Unlike {@link Corpora} it performs no download, extraction or corpus generation
     * &mdash; it operates directly on a file already present on the device.
     *
     * <h3>Execution</h3>
     * {@link #run()} opens a Swing {@link javax.swing.JFileChooser} via {@link #chooseFile()} and,
     * once a file is selected, benchmarks every participating codec over it in parallel via
     * {@link #runCodecsParallel} (all codecs benchmarked across worker threads; the single file is grouped
     * under the {@code "file"} sub-type). If the user cancels the picker, the run is aborted and
     * {@code null} is returned. Note the picker requires a graphical display and will not work headless.
     *
     * <h3>Report</h3>
     * {@link #generateReport} fills {@link #TARGET_BLUEPRINT} ({@code results/templates/target-blueprint.md}),
     * a single-file mirror of the corpora blueprint that reuses the same shared table generators. It
     * keeps the corpora report's section
     * titles and full Metrics Overview, replacing the multi-corpus "Test Corpus Structure" with a
     * "Target File" section and omitting the cross-corpus "Comparative Analysis". Output is written
     * as {@code Target-Benchmark(fileName)-dd-MM-yyyy~HH~mm~ss.md}.
     *
     * <h3>Entry point</h3>
     * {@link #main(String[])} demonstrates a run against GZIP and ZSTANDARD. The "Light" baseline
     * codec is intentionally excluded here &mdash; it is featured only in {@link Corpora}.
     */
    public static class Target extends Benchmarking {

        // Sub-type label used for the single chosen file in tables and grouping.
        private static final String FILE_SUBTYPE = "file";

        public Target(String title) {
            super(title);
        }

        public Target() {
            super();
        }

        public Target(String title, String resultsPath) {
            super(title, resultsPath);
        }

        /**
         * Opens a file picker so the user can choose the single file to benchmark.
         * @return The chosen file, or {@code null} if the selection was cancelled.
         */
        private File chooseFile() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select a file to benchmark");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int selection = fileChooser.showOpenDialog(null);
            if (selection == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile();
            }
            return null;
        }

        /**
         * Fills the target report template, mirroring {@link Corpora#generateReport} but for the
         * single benchmarked file. Reuses the shared table generators so the layout, section titles
         * and metrics match the corpora report.
         */
        private String generateReport(List<BenchmarkResult> results, File targetFile) throws IOException {
            String templatePath = TARGET_BLUEPRINT;
            String template = Files.readString(Paths.get(templatePath));

            Map<String, List<BenchmarkResult>> subResults = new LinkedHashMap<>();
            subResults.put(FILE_SUBTYPE, results);
            Map<String, Map<String, List<BenchmarkResult>>> allResults = new LinkedHashMap<>();
            allResults.put("target", subResults);

            Duration d = Duration.ofNanos(System.nanoTime() - startTime);
            String durationStr = String.format("%dhrs %dmin %dsecs", d.toHours(), d.toMinutesPart(), d.toSecondsPart());
            String targetFileInfo = "`" + targetFile.getName() + "` (" + formatBytes(targetFile.length()) + ")";

            LocalDateTime now = LocalDateTime.now();
            template = template.replace("{{TITLE}}", title)
                               .replace("{{TIMESTAMP}}", now.format(DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm:ss")))
                               .replace("{{TARGET_FILE}}", targetFileInfo)
                               .replace("{{CODEC_COUNT}}", codecIds.size() + " (in " + durationStr + ")")
                               .replace("{{PLATFORM_INFO}}", System.getProperty("os.name") + ", Java " + System.getProperty("java.version"))
                               .replace("{{DEVICE_INFO}}", Hardware.getDeviceInfo());

            // Session timing (start/end/duration) for the whole benchmarking session
            template = applySessionTiming(template);

            // Generate tables
            template = template.replace("{{KEY_FINDINGS_TABLE}}", generateKeyFindings(allResults));
            template = template.replace("{{CODEC_LIST_TABLE}}", generateCodecList());
            template = template.replace("{{TARGET_TABLE}}", generateComparisonTable(subResults, FILE_SUBTYPE));
            template = template.replace("{{TARGET_PERFORMANCE_TABLE}}", generatePerformanceTable(subResults));
            template = template.replace("{{TARGET_DETAILS}}", generateDetailTables(subResults, new String[]{FILE_SUBTYPE}));

            // Conclusion logic
            template = template.replace("{{CONCLUSION_TEXT}}", generateConclusion(allResults));

            return template;
        }

        /**
         * Prompts for a single file and benchmarks every participating codec against it.
         * @return Path to the generated report file, or {@code null} if no file was selected.
         */
        public String run() throws IOException {
            if (codecIds.isEmpty()) {
                throw new IllegalStateException("No codecs added for benchmarking.");
            }

            File targetFile = chooseFile();
            if (targetFile == null) {
                System.out.println("No file selected. Benchmark cancelled.");
                return null;
            }

            startTime = System.nanoTime();
            startDateTime = LocalDateTime.now();
            log("Starting Target Benchmark: " + targetFile.getName());

            // Ensure sandbox testing directory exists and is clean
            cleanSandbox();
            new File(SANDBOX_TESTING).mkdirs();
            log("Sandbox cleaned and initialized at " + SANDBOX_TESTING);

            this.totalTasks = codecIds.size();
            this.completedTasks.set(0);
            codecTimings.clear();

            System.out.println("Benchmarking " + codecIds.size() + " codec(s) on " + targetFile.getName() + "...");

            // A single cell (the chosen file); all codecs run simultaneously across worker threads.
            List<Cell> cells = Collections.singletonList(new Cell(targetFile, FILE_SUBTYPE, null, "target"));
            Map<Integer, List<BenchmarkResult>> resultsByCodec = runCodecsParallel(cells, false);

            // Flatten to a codec-ordered result list for the single cell.
            List<BenchmarkResult> results = new ArrayList<>();
            for (int id : codecIds) {
                List<BenchmarkResult> codecResults = resultsByCodec.get(id);
                if (codecResults != null && codecResults.get(0) != null) results.add(codecResults.get(0));
            }
            System.out.println();

            String reportContent = generateReport(results, targetFile);

            LocalDateTime now = LocalDateTime.now();
            // '~' rather than ':' in the time part so the file name stays valid on Windows/other OSes.
            String timestamp = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy~HH~mm~ss"));
            String reportName = String.format("Target-Benchmark(%s)-%s.md", targetFile.getName(), timestamp);

            String reportPath = writeReport(reportName, reportContent);

            // Clean up sandbox testing files
            cleanSandbox();

            System.out.println("\n✅ Target benchmark complete! Report saved to: " + reportPath);
            return reportPath;
        }

        public static void main(String[] args) {
            try {
                Target benchmarking = new Target("Harmattan");
                benchmarking.showLogs(true); // Enable detailed logs

                // Add codecs for comparison (Light baseline is Corpora-only)
                benchmarking.addCodec(Transcoder.Codec.GZIP);
                benchmarking.addCodec(Transcoder.Codec.ZSTANDARD);

                benchmarking.run();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
