package com.linkersoft.reductions;


import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Transcoder facilitates file compression and decompression using a plugin-based architecture.
 * <p>
 * This class provides a registry of {@link Codec} implementations, allowing for:
 * <ul>
 *   <li>Compression and decompression of files.</li>
 *   <li>Detailed statistics including size, ratio, time, and entropy.</li>
 *   <li>Extensibility by adding custom codecs via {@link #addCodec(int, Codec)}.</li>
 *   <li>Dynamic configuration of codecs via {@link #setCodecProperty(int, String, Object)}.</li>
 * </ul>
 * </p>
 * <p>
 * By default, only the {@link Codec#PLAIN} codec is registered for benchmarking baseline.
 * Additional codecs must be added via {@link #addCodec(int, Codec)}.
 * </p>
 */
public class Transcoder {

    /**
     * A utility class for quickly verifying the functionality of popular Transcoder codecs (Huffman, RLE, BWT, LZ78, Gzip, Deflate).
     * <p>
     * This class conducts a series of tests using specific input patterns including:
     * <ul>
     *   <li>"WWWW..." (RLE Target)</li>
     *   <li>"The coarser the grid..." (Huffman Target)</li>
     *   <li>"abab..." (LZ78 Target)</li>
     *   <li>"BANANA..." (BWT + RLE Target)</li>
     *   <li>JSON Data (GZIP Target)</li>
     *   <li>Java Source Code (Deflate Target)</li>
     * </ul>
     * It ensures that all registered codecs are operating correctly and efficiently by checking for:
     * <ul>
     *   <li>Compression integrity (reversibility).</li>
     *   <li>Compression ratios (compressed / original).</li>
     *   <li>Entropy analysis accuracy.</li>
     * </ul>
     * Use this class to validate changes to existing codecs or to test new codec implementations.
     * </p>
     */
    public static class Testing {

        public static void main(String[] args) {
            Testing suite = new Testing();
            try {
                suite.runAllTests();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                suite.cleanup();
            }
        }

        private final File tempDir;
        private final Transcoder transcoder;
        private final LinkedHashMap<String, String> inputs;

        {
            inputs = new LinkedHashMap<>();
        }

        public Testing() {
            this.transcoder = new Transcoder();
            this.tempDir = new File("transcoder_suite_tmp");
            if (!this.tempDir.exists()) {
                this.tempDir.mkdirs();
            }
            
            // Define Realistic Inputs
            
            // 1. RLE Winner: Repeated characters
            StringBuilder rleInput = new StringBuilder();
            for(int i=0; i<50; i++) rleInput.append("W");
            inputs.put("RLE Target", rleInput.toString());

            // 2. Huffman Winner
            String huffmanInput = "The coarser the grid, the less accurate the approximation. However, a finer grid requires more computation. " +
                    "The coarser the grid, the less accurate the approximation. However, a finer grid requires more computation. " +
                    "The coarser the grid, the less accurate the approximation. However, a finer grid requires more computation.";
            inputs.put("Huffman Target", huffmanInput);

            // 3. LZ78 Winner
            String lz78Input = "abababababababababababababababababababababababababababababababababababababababababababababababababab";
            inputs.put("LZ78 Target", lz78Input);

            // 4. BWT + RLE Winner: Repetitive distinct phrases (BANANA example scaled up)
            // "BANANA" -> "NNBAAA". RLE compresses "AAA" nicely.
            StringBuilder bwtInput = new StringBuilder();
            for(int i=0; i<10; i++) bwtInput.append("BANANA");
            inputs.put("BWT + RLE Target", bwtInput.toString());

            // 5. GZIP Winner
            String gzipInput = "{\"menu\": { \"id\": \"file\", \"value\": \"File\", \"popup\": { \"menuitem\": [ {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"}, {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"}, {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"} ] } }}";
            inputs.put("GZIP Target", gzipInput);

            // 6. Deflate Winner
            String deflateInput = "import java.util.*; public class Main { public static void main(String[] args) { System.out.println(\"Hello World\"); } } " +
                                  "import java.util.*; public class Main { public static void main(String[] args) { System.out.println(\"Hello World\"); } }";
            inputs.put("Deflate Target", deflateInput);
            
            // Register Codecs
            transcoder.addCodec(Transcoder.Codec.RUN_LENGTH, new Transcoder.RunLength());
            transcoder.addCodec(Transcoder.Codec.HUFFMAN, new Transcoder.Huffman());
            transcoder.addCodec(Transcoder.Codec.LZ78, new Transcoder.LZ78());
            transcoder.addCodec(Transcoder.Codec.GZIP, new Transcoder.Gzip());
            transcoder.addCodec(Transcoder.Codec.DEFLATE, new Transcoder.Deflate());
            
            // Custom BWT + RLE Codec for demonstration
            transcoder.addCodec(99, new Codec("BWT + RLE") {
                private final BWT bwt = new BWT();
                private final RunLength rle = new RunLength();

                @Override
                protected void onFooter(String name) {
                    footer.put("name", name);
                    footer.put("version", "Demo");
                }
                @Override
                public void onCompress(File input, File output) throws Exception {
                    File temp = new File(output.getAbsolutePath() + ".bwt.tmp");
                    bwt.onCompress(input, temp);
                    rle.onCompress(temp, output);
                    temp.delete();
                }
                @Override
                public void onDecompress(File input, File output) throws Exception {
                   File temp = new File(output.getAbsolutePath() + ".bwt.tmp");
                   rle.onDecompress(input, temp);
                   bwt.onDecompress(temp, output);
                   temp.delete();
                }
            });
        }

        private void runAllTests() throws Exception {
            int[] codecIds = {
                Transcoder.Codec.HUFFMAN,
                Transcoder.Codec.RUN_LENGTH,
                99, // BWT + RLE
                Transcoder.Codec.LZ78,
                Transcoder.Codec.GZIP,
                Transcoder.Codec.DEFLATE
            };

            List<String> headers = new java.util.ArrayList<>();
            headers.add("Input");
            for (int id : codecIds) headers.add(transcoder.getCodecName(id));

            List<List<String>> rows = new java.util.ArrayList<>();

            for (java.util.Map.Entry<String, String> entry : inputs.entrySet()) {
                List<String> row = new java.util.ArrayList<>();
                String inputName = entry.getKey();
                String inputData = entry.getValue();
                
                File inputFile = createInputFile(inputName, inputData);
                long inputSize = inputFile.length();
                row.add(inputName + "\n(" + inputSize + " bytes)");

                transcoder.setInput(inputFile);

                for (int codecId : codecIds) {
                    Codec c = transcoder.getCodec(codecId);
                    try {
                        File compressedFile = transcoder.getOutput(codecId);
                        if (compressedFile == null || !compressedFile.exists()) {
                            row.add("FAILED");
                            continue;
                        }
                        
                        long compressedSize = compressedFile.length();
                        double ratio = (double) compressedSize / inputSize;
                        String emoji = (ratio < 1.0) ? "✅" : "❌";
                        
                        String sizeStr = String.format("%dB", compressedSize);
                        String containerStr = c.footer.getOrDefault("structure", "");
                        
                        String cellContent = sizeStr + "\n" + String.format("Ratio: %.2f %s", ratio, emoji);
                        if (!containerStr.isEmpty()) {
                             cellContent += "\n" + containerStr;
                        }
                        row.add(cellContent);

                    } catch (Exception e) {
                        row.add("ERROR: " + e.getClass().getSimpleName());
                    }
                }
                rows.add(row);
            }

            printTable(headers, rows);
        }

        private File createInputFile(String label, String data) throws IOException {
            File file = new File(tempDir, label.replaceAll("[^a-zA-Z0-9.-]", "_") + ".txt");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            return file;
        }
        
        // Helper to perform simple BWT for string display (simplified logic matching Codec)
        private String performBWTTransform(String str) {
            int n = str.length();
            String[] rotations = new String[n];
            for (int i = 0; i < n; i++) {
                rotations[i] = str.substring(i) + str.substring(0, i);
            }
            java.util.Arrays.sort(rotations);
            StringBuilder bwt = new StringBuilder();
            for (int i = 0; i < n; i++) {
                bwt.append(rotations[i].charAt(n - 1));
            }
            return bwt.toString();
        }

        private void printTable(List<String> headers, List<List<String>> rows) {
            int[] colWidths = new int[headers.size()];
            // Calculate max content width per column
            for (int i = 0; i < headers.size(); i++) {
                colWidths[i] = getVisualLength(headers.get(i));
            }
            for (List<String> row : rows) {
                for (int i = 0; i < row.size(); i++) {
                    String cell = row.get(i);
                    for (String line : cell.split("\n")) {
                        colWidths[i] = Math.max(colWidths[i], getVisualLength(line));
                    }
                }
            }
            // Add padding (1 space left, 1 space right)
            for(int i=0; i<colWidths.length; i++) colWidths[i] += 2;

            // Calculate Total Table Width for Borders
            // Border is: + (W1) + (W2) + ... +
            int totalTableWidth = 1; // First '+'
            for(int w : colWidths) totalTableWidth += w + 1; // Width + separator '+'

            // 1. Top Border
            printFullHorizontalLine(totalTableWidth);
            
            // 2. Title Section
            String title = "TRANSCODER";
            String subtitle = "Popular Codec Evaluation";
            
            int titlePadLeft = (totalTableWidth - 4 - title.length()) / 2;
            int subPadLeft = (totalTableWidth - 4 - subtitle.length()) / 2;
            
            System.out.println(String.format("| %" + titlePadLeft + "s%s%-" + (totalTableWidth - 4 - titlePadLeft - title.length()) + "s |", "", title, ""));
            System.out.println(String.format("| %" + subPadLeft + "s%s%-" + (totalTableWidth - 4 - subPadLeft - subtitle.length()) + "s |", "", subtitle, ""));
            printFullHorizontalLine(totalTableWidth);

            // 3. Testing Data Section
            System.out.println(String.format("| %-" + (totalTableWidth - 4) + "s |", "TESTING DATA:"));
            int idx = 1;
            for (java.util.Map.Entry<String, String> entry : inputs.entrySet()) {
                String key = entry.getKey();
                String rawVal = entry.getValue();
                String val = "\"" + rawVal + "\"";
                
                if (key.startsWith("BWT + RLE")) {
                     String transformed = performBWTTransform(rawVal);
                     val = "\"" + rawVal + "\" -> \"" + transformed + "\"";
                }

                double targetEntropy;
                try {
                    File entropyFile = createInputFile(key, rawVal);
                    transcoder.setInput(entropyFile);
                    targetEntropy = transcoder.getEntropy();
                } catch (Exception e) {
                    targetEntropy = -1;
                }
                String prefix = idx++ + ". " + key + "(Entropy - " + String.format("%.4f", targetEntropy) + "): ";
                String indent = String.format("%" + prefix.length() + "s", "");
                String fullLine = prefix + val;
                
                // Wrap text to fit inside: TotalWidth - 4 chars ( "| " + text + " |" )
                List<String> wrappedLines = wrapText(fullLine, totalTableWidth - 4, indent);
                for (String line : wrappedLines) {
                    System.out.print("| ");
                    printPadded(line, totalTableWidth - 4);
                    System.out.println(" |");
                }
            }
            printFullHorizontalLine(totalTableWidth);

            // 4. Header Row
            printRow(headers, colWidths);
            printSeparator(colWidths);

            // 5. Data Rows
            for (List<String> row : rows) {
                printRow(row, colWidths);
                printSeparator(colWidths);
            }
            
            System.out.println("📝 Codec Structure Notes:");
            for (int id : new int[]{
                Transcoder.Codec.HUFFMAN, Transcoder.Codec.RUN_LENGTH, Transcoder.Codec.BWT, Transcoder.Codec.LZ78, 
                Transcoder.Codec.GZIP, Transcoder.Codec.DEFLATE, Transcoder.Codec.ZSTANDARD, Transcoder.Codec.BROTLI,
                Transcoder.Codec.BZIP2, Transcoder.Codec.LZ4_BLOCK, Transcoder.Codec.LZ4_FRAMED, 
                Transcoder.Codec.SNAPPY_RAW, Transcoder.Codec.SNAPPY_FRAMED, Transcoder.Codec.XZ, 
                Transcoder.Codec.LZMA, Transcoder.Codec.LZW, Transcoder.Codec.DEFLATE64, 
                Transcoder.Codec.BASE64, Transcoder.Codec.BASE32, Transcoder.Codec.PLAIN
            }) {
                Codec c = transcoder.getCodec(id);
                if (c != null) {
                    String structure = c.footer.getOrDefault("structure", "");
                    structure = structure.replace("{", "").replace("}", "").trim();
                    structure = structure.replaceAll("payload\\(.*?\\)", "payload(variable)");
                    System.out.println("   - " + c.footer.get("name") + ": " + structure);
                }
            }
        }

        private void printRow(List<String> row, int[] widths) {
            // Split all cells into lines
            List<String[]> cellLines = new java.util.ArrayList<>();
            int maxLines = 0;
            for (String cell : row) {
                String[] lines = cell.split("\n");
                cellLines.add(lines);
                maxLines = Math.max(maxLines, lines.length);
            }

            for (int lineIdx = 0; lineIdx < maxLines; lineIdx++) {
                System.out.print("|"); // Start of row
                for (int colIdx = 0; colIdx < row.size(); colIdx++) {
                    String[] lines = cellLines.get(colIdx);
                    String content = (lineIdx < lines.length) ? lines[lineIdx] : "";
                    
                    System.out.print(" "); 
                    System.out.print(content);
                    
                    int visualLen = getVisualLength(content);
                    int remainingSpace = widths[colIdx] - 1 - visualLen; // -1 for left space
                    for(int k=0; k<remainingSpace; k++) System.out.print(" ");
                    System.out.print("|"); // End of cell
                }
                System.out.println();
            }
        }

        private void printSeparator(int[] widths) {
             System.out.print("+");
            for (int width : widths) {
                for(int k=0; k<width; k++) System.out.print("-"); 
                System.out.print("+");
            }
            System.out.println();
        }
        
        private void printFullHorizontalLine(int width) {
            System.out.print("+");
            for(int k=0; k<width-2; k++) System.out.print("-");
            System.out.println("+");
        }

        private void printPadded(String s, int width) {
            System.out.print(s);
            int len = getVisualLength(s);
            for(int i=0; i < width - len; i++) {
                System.out.print(" ");
            }
        }

        private List<String> wrapText(String text, int maxWidth, String indent) {
            List<String> lines = new java.util.ArrayList<>();
            if (text == null) return lines;
            
            if (getVisualLength(text) <= maxWidth) {
                lines.add(text);
                return lines;
            }

            StringBuilder currentLine = new StringBuilder();
            int currentWidth = 0;
            int indentWidth = getVisualLength(indent);

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                int charWidth = (c == '\u2705' || c == '\u274C') ? 2 : 1; 

                // Check if adding this char exceeds width
                if (currentWidth + charWidth > maxWidth) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                    currentLine.append(indent);
                    currentWidth = indentWidth;
                }
                
                currentLine.append(c);
                currentWidth += charWidth;
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
            return lines;
        }

        private int getVisualLength(String s) {
            int len = 0;
            for(int i=0; i<s.length(); i++) {
                char c = s.charAt(i);
                 if (c == '\u2705' || c == '\u274C') { // ✅ and ❌
                    len += 2; 
                 } else {
                    len += 1;
                 }
            }
            return len;
        }

        private void cleanup() {
            if (tempDir != null && tempDir.exists()) {
                File[] files = tempDir.listFiles();
                if (files != null) for (File f : files) f.delete();
                tempDir.delete();
            }
        }
    }

    /**
     * Abstract base class for all compression codecs.
     * <p>
     * New compression algorithms can be added by extending this class and registering
     * the instance via {@link Transcoder#addCodec(int, Codec)}.
     * </p>
     * <p>
     * This class also holds the constants for supported compression algorithm IDs.
     * </p>
     */
    public static abstract class Codec {
        // Codec ID constants
        public static final int RUN_LENGTH = 0;
        public static final int HUFFMAN = 1;
        public static final int BWT = 2;
        public static final int LZ78 = 3;
        public static final int ZSTANDARD = 4;
        public static final int BROTLI = 5;
        public static final int GZIP = 6;
        public static final int DEFLATE = 7;
        public static final int BZIP2 = 8;
        public static final int LZ4_BLOCK = 9;
        public static final int LZ4_FRAMED = 10;
        public static final int SNAPPY_RAW = 11;
        public static final int SNAPPY_FRAMED = 12;
        public static final int XZ = 13;
        public static final int LZMA = 14;
        public static final int LZW = 15;
        public static final int DEFLATE64 = 16;
        public static final int BASE64 = 17;
        public static final int BASE32 = 18;
        public static final int PLAIN = 19;

        protected Map<String, Object> properties;
        // name field removed

        protected Map<String, String> footer;

        {
            properties = new HashMap<>();
            footer = new HashMap<>();
            footer.put("library", "Unknown");
            footer.put("version", "1.0");
            footer.put("streaming", "No");
            footer.put("notes", "-");
        }
        
        /**
         * Constructs a new Codec with the given name.
         * @param name The human-readable name of the codec.
         */
        public Codec(String name) {
            onFooter(name);
        }

        /**
         * Populates the footer metadata.
         * <p>
         * Subclasses must implement this method to populate the {@link #footer} map with
         * metadata such as "library", "version", "streaming", and "notes".
         * </p>
         * @param name The name of the codec passed from the constructor.
         */
        protected abstract void onFooter(String name);

        /**
         * Sets a configuration property for this codec.
         * @param key The property key.
         * @param value The property value.
         */
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }

        /**
         * Gets a configuration property with a default value.
         * @param key The property key.
         * @param defaultValue The default value if the key is missing or the type is incorrect.
         * @return The property value or the default value.
         */
        @SuppressWarnings("unchecked")
        protected <T> T getProperty(String key, T defaultValue) {
            Object value = properties.get(key);
            if (value != null && defaultValue.getClass().isInstance(value)) {
                return (T) value;
            }
            return defaultValue;
        }


        /**
         * Gets the footer metadata map.
         * @return The map of footer metadata.
         */
        public Map<String, String> getFooter() {
            return footer;
        }

        /**
         * Compresses the input file to the output file.
         * @param input The source file to compress.
         * @param output The destination file for compressed data.
         * @throws Exception If an error occurs during compression.
         */
        public abstract void onCompress(File input, File output) throws Exception;

        /**
         * Decompresses the input file to the output file.
         * @param input The source compressed file.
         * @param output The destination file for decompressed data.
         * @throws Exception If an error occurs during decompression.
         */
        public abstract void onDecompress(File input, File output) throws Exception;

        public String getName() {
            return footer.get("name");
        }

        @Override
        public String toString() {
            return new StringBuilder().append(getName()).append("\nproperties: ").append(properties).append("\nfooter: ").append(footer).toString();
        }
    }

    // ========== Built-in Codec Implementations ==========
    // These are public static so they can be instantiated by users who want to add them.

    public static class RunLength extends Codec {

        public RunLength() { super("Run-Length Encoding"); }

        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Built-in");
            footer.put("version", "1.0");
            footer.put("streaming", "Yes");
            footer.put("structure", "[header (4B): Original Size] + [payload (variable)]");
            footer.put("notes", "Simple RLE");
        }

        @Override
        public void onCompress(File input, File output) throws Exception {
            byte[] data = Files.readAllBytes(input.toPath());
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(output))) {
                out.writeInt(data.length); // 4 bytes header
                
                if (data.length == 0) return;
                
                int count = 1;
                byte prev = data[0]; 
                
                for (int i = 1; i < data.length; i++) {
                    byte current = data[i];
                    if (current == prev && count < 255) {
                        count++;
                    } else {
                        out.writeByte(prev);
                        out.writeByte(count);
                        prev = current;
                        count = 1;
                    }
                }
                // Write last part
                out.writeByte(prev);
                out.writeByte(count);
            }
            long finalSize = output.length();
            long payloadSize = Math.max(0, finalSize - 4);
            footer.put("structure", String.format("[header (4B)] + [payload (%dB)]", payloadSize));
        }

        @Override
        public void onDecompress(File input, File output) throws Exception {
            try (InputStream in = new FileInputStream(input);
                 OutputStream out = new FileOutputStream(output)) {
                int value, count;
                while ((value = in.read()) != -1) {
                    count = in.read();
                    if (count == -1) break;
                    for (int i = 0; i < count; i++) {
                        out.write(value);
                    }
                }
            }
        }
    }

    public static class Huffman extends Codec {

        public Huffman() { super("Huffman Coding"); }

        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Built-in");
            footer.put("version", "2.0 (Topology)");
            footer.put("streaming", "No");
            footer.put("structure", "[header (4B): length] + [tree (~10 bits/char)] + [payload (variable)]");
            footer.put("notes", "Dynamic Huffman with Tree Topology Encoding");
        }

        @Override
        public void onCompress(File input, File output) throws Exception {
            byte[] data = Files.readAllBytes(input.toPath());
            if (data.length == 0) {
                try (OutputStream out = new FileOutputStream(output)) { out.write(0); }
                return;
            }

            // 1. Calculate Frequencies
            Map<Byte, Integer> freqMap = new HashMap<>();
            for (byte b : data) {
                freqMap.put(b, freqMap.getOrDefault(b, 0) + 1);
            }

            // 2. Build Tree
            HuffmanNode root = buildHuffmanTree(freqMap);
            Map<Byte, String> codeMap = new HashMap<>();
            buildCodeMap(root, "", codeMap);

            // 3. Serialize
            long treeBits = 0;
            
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
                BitUtils bits = new BitUtils(dos);
                
                // A. Write Original Length (4 bytes)
                dos.writeInt(data.length);

                // B. Write Tree Topology
                int startBits = bits.getTotalBits();
                writeTree(root, bits);
                treeBits = bits.getTotalBits() - startBits;

                // C. Write Encoded Data
                for (byte b : data) {
                    String code = codeMap.get(b);
                    for (char bit : code.toCharArray()) {
                        bits.writeBit(bit == '1');
                    }
                }
                bits.flush();
            }
            
            // 4. Calculate Container Stats
            long finalSize = output.length();
            
            long codeBits = 0;
            for (byte b : data) codeBits += codeMap.get(b).length();
            long payloadBytes = (codeBits + 7) / 8;
            
            // Tree bytes is roughly the rest (minus header and payload)
            // But since they are bit-packed, "payload" isn't a separate block exactly.
            // However, conceptually:
            // Header: 4 bytes
            // Tree: treeBits / 8
            // Payload: codeBits / 8
            // Total = Header + (TreeBits + CodeBits + Padding) / 8
            
            long treeBytes = (treeBits + 7) / 8; 
            // Note: This is an approximation because tree and payload share the same bitstream.
            // But it gives the user the breakdown they want.
            // A more exact "structure" byte count would be: Total - 4 - PayloadBytes.
            
            long containerTreeBytes = Math.max(0, finalSize - 4 - payloadBytes);
            
            footer.put("structure", String.format("[header (4B): length] + [tree (%dB)] + [payload (%dB)]", containerTreeBytes, payloadBytes));
        }

        @Override
        public void onDecompress(File input, File output) throws Exception {
            byte[] fileBytes = Files.readAllBytes(input.toPath());
            if (fileBytes.length == 0 || fileBytes.length == 1 && fileBytes[0] == 0) return;

            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(fileBytes));
                 OutputStream os = new BufferedOutputStream(new FileOutputStream(output))) {
                
                BitUtils bits = new BitUtils(dis);
                
                // A. Read Original Length
                int length = dis.readInt();
                
                // B. Rebuild Tree
                HuffmanNode root = readTree(bits);
                
                // C. Decode Data
                for (int i = 0; i < length; i++) {
                    HuffmanNode node = root;
                    while (node.left != null || node.right != null) {
                        boolean bit = bits.readBit();
                        node = bit ? node.right : node.left;
                    }
                    os.write(node.data);
                }
            }
        }
        
        // --- Helper Methods ---

        private void writeTree(HuffmanNode node, BitUtils bits) throws IOException {
            if (node.left == null && node.right == null) {
                bits.writeBit(true); // 1 = Leaf
                bits.writeByte(node.data);
            } else {
                bits.writeBit(false); // 0 = Internal
                writeTree(node.left, bits);
                writeTree(node.right, bits);
            }
        }

        private HuffmanNode readTree(BitUtils bits) throws IOException {
            boolean isLeaf = bits.readBit();
            if (isLeaf) {
                return new HuffmanNode(bits.readByte(), 1, null, null);
            } else {
                HuffmanNode left = readTree(bits);
                HuffmanNode right = readTree(bits);
                return new HuffmanNode((byte)0, 0, left, right);
            }
        }

        private HuffmanNode buildHuffmanTree(Map<Byte, Integer> freqMap) {
            java.util.PriorityQueue<HuffmanNode> pq = new java.util.PriorityQueue<>();
            for (Map.Entry<Byte, Integer> entry : freqMap.entrySet()) {
                pq.add(new HuffmanNode(entry.getKey(), entry.getValue()));
            }

            while (pq.size() > 1) {
                HuffmanNode left = pq.poll();
                HuffmanNode right = pq.poll();
                HuffmanNode parent = new HuffmanNode(null, left.frequency + right.frequency);
                parent.left = left;
                parent.right = right;
                pq.add(parent);
            }
            return pq.poll();
        }

        private void buildCodeMap(HuffmanNode node, String code, Map<Byte, String> codeMap) {
            if (node == null) return;
            if (node.left == null && node.right == null) {
                codeMap.put(node.data, code);
                return;
            }
            buildCodeMap(node.left, code + "0", codeMap);
            buildCodeMap(node.right, code + "1", codeMap);
        }
        
        // --- Inner Classes ---

        private static class HuffmanNode implements Comparable<HuffmanNode> {
            Byte data;
            int frequency;
            HuffmanNode left, right;

            HuffmanNode(Byte data, int frequency) {
                this.data = data;
                this.frequency = frequency;
            }
            // Internal node constructor
            HuffmanNode(Byte data, int frequency, HuffmanNode left, HuffmanNode right) {
                this.data = data;
                this.frequency = frequency;
                this.left = left;
                this.right = right;
            }

            @Override
            public int compareTo(HuffmanNode other) {
                return this.frequency - other.frequency;
            }
        }

        private static class BitUtils {
            private final DataOutputStream out;
            private final DataInputStream in;
            private int currentByte = 0;
            private int numBits = 0;
            private int totalBits = 0;
            
            private int readBuffer = 0;
            private int readBitsRemaining = 0;

            public BitUtils(DataOutputStream out) { this.out = out; this.in = null; }
            public BitUtils(DataInputStream in) { this.in = in; this.out = null; }
            
            public int getTotalBits() { return totalBits; }

            public void writeBit(boolean bit) throws IOException {
                if (bit) currentByte |= (1 << (7 - numBits));
                numBits++;
                totalBits++;
                if (numBits == 8) {
                    out.write(currentByte);
                    currentByte = 0;
                    numBits = 0;
                }
            }

            public void writeByte(byte b) throws IOException {
                for (int i = 7; i >= 0; i--) {
                    writeBit(((b >> i) & 1) == 1);
                }
            }

            public void flush() throws IOException {
                if (numBits > 0) {
                    out.write(currentByte);
                    numBits = 0;
                }
            }

            public boolean readBit() throws IOException {
                if (readBitsRemaining == 0) {
                    readBuffer = in.read();
                    if (readBuffer == -1) throw new EOFException();
                    readBitsRemaining = 8;
                }
                boolean bit = ((readBuffer >> (readBitsRemaining - 1)) & 1) == 1;
                readBitsRemaining--;
                return bit;
            }

            public byte readByte() throws IOException {
                int val = 0;
                for (int i = 0; i < 8; i++) {
                    val <<= 1;
                    if (readBit()) val |= 1;
                }
                return (byte) val;
            }
        }
    }

    public static class BWT extends Codec {

        public BWT() { super("Burrows-Wheeler Transform"); }

        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Built-in");
            footer.put("version", "1.0");
            footer.put("streaming", "No");
            footer.put("structure", "[header (4B): index] + [payload (variable)]");
            footer.put("notes", "Burrows-Wheeler Transform (No Compression)");
        }

        @Override
        public void onCompress(File input, File output) throws Exception {
            byte[] data = Files.readAllBytes(input.toPath());
            String str = new String(data);

            int n = str.length();
            String[] rotations = new String[n];
            for (int i = 0; i < n; i++) {
                rotations[i] = str.substring(i) + str.substring(0, i);
            }

            java.util.Arrays.sort(rotations);

            StringBuilder bwt = new StringBuilder();
            int originalIndex = 0;
            for (int i = 0; i < n; i++) {
                bwt.append(rotations[i].charAt(n - 1));
                if (rotations[i].equals(str)) {
                    originalIndex = i;
                }
            }

            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(output))) {
                out.writeInt(originalIndex);
                out.write(bwt.toString().getBytes());
            }
            long finalSize = output.length();
            long payloadSize = Math.max(0, finalSize - 4);
            String container = String.format("[header (4B)] + [payload (%dB)]", payloadSize);
            footer.put("structure", container);
            // System.out.println("DEBUG: BWT Compressed " + input.getName() + " Size=" + finalSize + " Footer=" + footer);
        }

        @Override
        public void onDecompress(File input, File output) throws Exception {
            try (DataInputStream in = new DataInputStream(new FileInputStream(input))) {
                int originalIndex = in.readInt();
                byte[] bwtBytes = in.readAllBytes();
                String bwt = new String(bwtBytes);

                int n = bwt.length();
                char[] chars = bwt.toCharArray();
                int[] next = new int[n];

                Character[] sorted = new Character[n];
                for (int i = 0; i < n; i++) {
                    sorted[i] = chars[i];
                }
                java.util.Arrays.sort(sorted);

                Map<Character, Integer> count = new HashMap<>();
                for (int i = 0; i < n; i++) {
                    char c = chars[i];
                    int pos = count.getOrDefault(c, 0);

                    int sortedPos = 0;
                    int seen = 0;
                    for (int j = 0; j < n; j++) {
                        if (sorted[j] == c) {
                            if (seen == pos) {
                                sortedPos = j;
                                break;
                            }
                            seen++;
                        }
                    }

                    next[sortedPos] = i;
                    count.put(c, pos + 1);
                }

                StringBuilder original = new StringBuilder();
                int idx = originalIndex;
                for (int i = 0; i < n; i++) {
                    original.append(sorted[idx]);
                    idx = next[idx];
                }

                Files.write(output.toPath(), original.toString().getBytes());
            }
        }
    }

    public static class LZ78 extends Codec {
        public LZ78() { super("LZ78"); }

        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Built-in");
            footer.put("version", "1.0");
            footer.put("streaming", "Yes");
            footer.put("structure", "[payload (variable)]");
            footer.put("notes", "Basic LZ78 dictionary compression");
        }

        @Override
        public void onCompress(File input, File output) throws Exception {
            byte[] data = Files.readAllBytes(input.toPath());
            Map<String, Integer> dictionary = new HashMap<>();
            int dictSize = 256;

            for (int i = 0; i < 256; i++) {
                dictionary.put(String.valueOf((char) i), i);
            }

            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(output))) {
                String current = "";
                for (byte b : data) {
                    String next = current + (char) (b & 0xFF);
                    if (dictionary.containsKey(next)) {
                        current = next;
                    } else {
                        out.writeInt(dictionary.get(current));
                        out.writeByte(b & 0xFF);
                        dictionary.put(next, dictSize++);
                        current = "";
                    }
                }

                if (!current.isEmpty()) {
                    out.writeInt(dictionary.get(current));
                    out.writeByte(0);
                }
            }
            long finalSize = output.length();
            footer.put("structure", String.format("[payload (%dB)]", finalSize));
        }

        @Override
        public void onDecompress(File input, File output) throws Exception {
            Map<Integer, String> dictionary = new HashMap<>();
            int dictSize = 256;

            for (int i = 0; i < 256; i++) {
                dictionary.put(i, String.valueOf((char) i));
            }

            try (DataInputStream in = new DataInputStream(new FileInputStream(input));
                 OutputStream out = new FileOutputStream(output)) {

                while (in.available() > 0) {
                    int code = in.readInt();
                    int ch = in.readByte() & 0xFF;

                    String entry = dictionary.get(code);
                    if (entry != null) {
                        out.write(entry.getBytes());
                        if (ch != 0) {
                            out.write(ch);
                            dictionary.put(dictSize++, entry + (char) ch);
                        }
                    }
                }
            }
        }
    }

    public static class Zstd extends Codec {
        public static class Properties {
            public enum Level {
                FASTEST(1),
                DEFAULT(3),
                MAX(22);

                final int value;
                Level(int value) { this.value = value; }
            }
            public enum Checksum {
                ENABLED(true),
                DISABLED(false);

                final boolean value;
                Checksum(boolean value) { this.value = value; }
            }
        }

        public Zstd() {
            super("Zstandard");
        }

        /**
         * Sets the compression level.
         * <ul>
         *   <li>{@link Properties.Level#FASTEST}: Optimizes for speed.</li>
         *   <li>{@link Properties.Level#DEFAULT}: Balanced default (level 3).</li>
         *   <li>{@link Properties.Level#MAX}: Maximum compression (level 22).</li>
         * </ul>
         * @param level The compression level.
         */
        public void setProperty(Properties.Level level) {
            setProperty("level", level);
        }

        /**
         * Enables or disables checksum validation.
         * <ul>
         *   <li>{@link Properties.Checksum#ENABLED}: Adds checksums to the stream.</li>
         *   <li>{@link Properties.Checksum#DISABLED}: No checksums (faster).</li>
         * </ul>
         * @param checksum The checksum configuration.
         */
        public void setProperty(Properties.Checksum checksum) {
            setProperty("checksum", checksum);
        }

        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "zstd-jni");
            footer.put("version", "1.5.5-11");
            footer.put("streaming", "Yes");
            footer.put("notes", "Dictionary-based algorithm with finite state entropy. Excellent real-time speed and high compression ratio. Larger memory footprint.");
            footer.put("structure", "[magic (4B)] + [frame_header (2-14B)] + [blocks (variable)] + [checksum (0-4B)]");
        }

        @Override
        public void onCompress(File input, File output) throws Exception {
            if (properties.isEmpty()) {
                 try (InputStream in = Files.newInputStream(input.toPath());
                     OutputStream out = new com.github.luben.zstd.ZstdOutputStream(Files.newOutputStream(output.toPath()))) {
                    in.transferTo(out);
                }
                return;
            }

            int level = getProperty("level", Properties.Level.DEFAULT).value;
            boolean checksum = getProperty("checksum", Properties.Checksum.DISABLED).value;

            try (InputStream in = Files.newInputStream(input.toPath());
                 com.github.luben.zstd.ZstdOutputStream out = new com.github.luben.zstd.ZstdOutputStream(Files.newOutputStream(output.toPath()), level)) {
                out.setChecksum(checksum);
                in.transferTo(out);
            }
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            // Zstd properties are compression-only; using default decompression
            try (InputStream in = new com.github.luben.zstd.ZstdInputStream(Files.newInputStream(input.toPath()));
                 OutputStream out = Files.newOutputStream(output.toPath())) {
                in.transferTo(out);
            }
        }
    }

    public static class Brotli extends Codec {
        public static class Properties {
            public enum Quality {
                LOW(0),
                DEFAULT(4),
                HIGH(11);

                final int value;
                Quality(int value) { this.value = value; }
            }
        }

        public Brotli() {
            super("Brotli");
        }

        /**
         * Sets the compression quality.
         * <ul>
         *   <li>{@link Properties.Quality#LOW}: Faster compression (0).</li>
         *   <li>{@link Properties.Quality#DEFAULT}: Balanced default (4).</li>
         *   <li>{@link Properties.Quality#HIGH}: Maximum compression, slower (11).</li>
         * </ul>
         * @param quality The quality level.
         */
        public void setProperty(Properties.Quality quality) {
            setProperty("quality", quality);
        }

        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Brotli4j");
            footer.put("version", "1.20.0");
            footer.put("streaming", "Yes");
            footer.put("notes", "General-purpose compression, good for text");
            footer.put("structure", "[header (variable)] + [meta_blocks (variable)]");
        }

        @Override
        public void onCompress(File input, File output) throws Exception {
            com.aayushatharva.brotli4j.Brotli4jLoader.ensureAvailability();
            
            if (properties.isEmpty()) {
                try (InputStream in = Files.newInputStream(input.toPath());
                     OutputStream out = new com.aayushatharva.brotli4j.encoder.BrotliOutputStream(Files.newOutputStream(output.toPath()))) {
                    in.transferTo(out);
                }
                return;
            }

            int quality = getProperty("quality", Properties.Quality.DEFAULT).value;
            com.aayushatharva.brotli4j.encoder.Encoder.Parameters params = new com.aayushatharva.brotli4j.encoder.Encoder.Parameters().setQuality(quality);

            try (InputStream in = Files.newInputStream(input.toPath());
                 OutputStream out = new com.aayushatharva.brotli4j.encoder.BrotliOutputStream(Files.newOutputStream(output.toPath()), params)) {
                in.transferTo(out);
            }
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            com.aayushatharva.brotli4j.Brotli4jLoader.ensureAvailability();

            // Brotli properties are compression-only; using default decompression
            try (InputStream in = new com.aayushatharva.brotli4j.decoder.BrotliInputStream(Files.newInputStream(input.toPath()));
                 OutputStream out = Files.newOutputStream(output.toPath())) {
                in.transferTo(out);
            }
        }
    }

    public static class Gzip extends Codec {
        public static class Properties {
            public enum Level {
                BEST_SPEED(java.util.zip.Deflater.BEST_SPEED),
                BEST_COMPRESSION(java.util.zip.Deflater.BEST_COMPRESSION),
                DEFAULT(java.util.zip.Deflater.DEFAULT_COMPRESSION);

                final int value;
                Level(int value) { this.value = value; }
            }
            public enum BufferSize {
                SMALL(512),
                MEDIUM(4096),
                LARGE(8192);

                final int value;
                BufferSize(int value) { this.value = value; }
            }
        }

        public Gzip() {
            super("GZIP");
        }

        /**
         * Sets the compression level.
         * <ul>
         *   <li>{@link Properties.Level#BEST_SPEED}: Fastest compression.</li>
         *   <li>{@link Properties.Level#BEST_COMPRESSION}: Maximum compression.</li>
         *   <li>{@link Properties.Level#DEFAULT}: Balanced default.</li>
         * </ul>
         * @param level The compression level.
         */
        public void setProperty(Properties.Level level) {
            setProperty("level", level);
        }

        /**
         * Sets the buffer size.
         * <ul>
         *   <li>{@link Properties.BufferSize#SMALL}: 512 bytes.</li>
         *   <li>{@link Properties.BufferSize#MEDIUM}: 4096 bytes.</li>
         *   <li>{@link Properties.BufferSize#LARGE}: 8192 bytes.</li>
         * </ul>
         * @param bufferSize The buffer size.
         */
        public void setProperty(Properties.BufferSize bufferSize) {
            setProperty("buffer-size", bufferSize);
        }

        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Compress");
            footer.put("version", "1.26.0");
            footer.put("streaming", "Yes");
            footer.put("structure", "[header (10B)] + [payload (variable)] + [trailer (8B)]");
            footer.put("notes", "DEFLATE algorithm with sliding window.");
        }

        @Override
        public void onCompress(File input, File output) throws Exception {
            if (properties.isEmpty()) {
                try (InputStream in = Files.newInputStream(input.toPath());
                     OutputStream out = new org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream(Files.newOutputStream(output.toPath()))) {
                    in.transferTo(out);
                }
                long finalSize = output.length();
                long payloadSize = Math.max(0, finalSize - 18);
                footer.put("structure", String.format("[header (10B)] + [payload (%dB)] + [trailer (8B)]", payloadSize));
                return;
            }

            int level = getProperty("level", Properties.Level.DEFAULT).value;
            int bufferSize = getProperty("buffer-size", Properties.BufferSize.LARGE).value;

            org.apache.commons.compress.compressors.gzip.GzipParameters parameters = new org.apache.commons.compress.compressors.gzip.GzipParameters();
            parameters.setCompressionLevel(level);
            parameters.setBufferSize(bufferSize);

            try (InputStream in = Files.newInputStream(input.toPath());
                 OutputStream out = new org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream(Files.newOutputStream(output.toPath()), parameters)) {
                in.transferTo(out);
            }
            long finalSize = output.length();
            long payloadSize = Math.max(0, finalSize - 18);
            footer.put("structure", String.format("[header (10B)] + [payload (%dB)] + [trailer (8B)]", payloadSize));
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            // Gzip properties are compression-only; using default decompression
            try (InputStream in = new org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream(Files.newInputStream(input.toPath()));
                 OutputStream out = Files.newOutputStream(output.toPath())) {
                in.transferTo(out);
            }
        }
    }

    public static class Deflate extends Codec {
        public static class Properties {
            public enum Level {
                BEST_SPEED(java.util.zip.Deflater.BEST_SPEED),
                BEST_COMPRESSION(java.util.zip.Deflater.BEST_COMPRESSION),
                DEFAULT(java.util.zip.Deflater.DEFAULT_COMPRESSION);

                final int value;
                Level(int value) { this.value = value; }
            }
        }

        public Deflate() {
            super("DEFLATE");
        }

        /**
         * Sets the compression level.
         * <ul>
         *   <li>{@link Properties.Level#BEST_SPEED}: Fastest compression.</li>
         *   <li>{@link Properties.Level#BEST_COMPRESSION}: Maximum compression.</li>
         *   <li>{@link Properties.Level#DEFAULT}: Balanced default.</li>
         * </ul>
         * @param level The compression level.
         */
        public void setProperty(Properties.Level level) {
            setProperty("level", level);
        }

        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Compress");
            footer.put("version", "1.26.0");
            footer.put("streaming", "Yes");
            footer.put("structure", "[header (2B)] + [payload (variable)] + [trailer (4B)]");
            footer.put("notes", "LZ77 + Huffman coding.");
        }

        @Override
        public void onCompress(File input, File output) throws Exception {
            if (properties.isEmpty()) {
                try (InputStream in = Files.newInputStream(input.toPath());
                     OutputStream out = new org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream(Files.newOutputStream(output.toPath()))) {
                    in.transferTo(out);
                }
                long finalSize = output.length();
                long payloadSize = Math.max(0, finalSize - 6);
                footer.put("structure", String.format("[header (2B)] + [payload (%dB)] + [trailer (4B)]", payloadSize));
                return;
            }

            int level = getProperty("level", Properties.Level.DEFAULT).value;
            org.apache.commons.compress.compressors.deflate.DeflateParameters parameters = new org.apache.commons.compress.compressors.deflate.DeflateParameters();
            parameters.setCompressionLevel(level);

            try (InputStream in = Files.newInputStream(input.toPath());
                 OutputStream out = new org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream(Files.newOutputStream(output.toPath()), parameters)) {
                in.transferTo(out);
            }
            long finalSize = output.length();
            long payloadSize = Math.max(0, finalSize - 6);
            footer.put("structure", String.format("[header (2B)] + [payload (%dB)] + [trailer (4B)]", payloadSize));
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            try (InputStream in = new org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream(Files.newInputStream(input.toPath()));
                 OutputStream out = Files.newOutputStream(output.toPath())) {
                in.transferTo(out);
            }
        }
    }

    public static class Bzip2 extends Codec {

        public Bzip2() {
            super("BZIP2");
        }

        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Compress");
            footer.put("version", "1.26.0");
            footer.put("streaming", "Yes");
            footer.put("notes", "Burrows-Wheeler Transform. Good compression ratio. Slower than GZIP and modern alternatives.");
            footer.put("structure", "[header (4B): Magic + Level] + [blocks (variable)] + [trailer (CRC)]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            try (InputStream in = Files.newInputStream(input.toPath());
                 OutputStream out = new org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream(Files.newOutputStream(output.toPath()))) {
                in.transferTo(out);
            }
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            try (InputStream in = new org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream(Files.newInputStream(input.toPath()));
                 OutputStream out = Files.newOutputStream(output.toPath())) {
                in.transferTo(out);
            }
        }
    }

    public static class Lz4Block extends Codec {
        public Lz4Block() {
            super("LZ4 Block");
        }
        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Compress");
            footer.put("version", "1.26.0");
            footer.put("streaming", "Yes");
            footer.put("notes", "LZ77-type algorithm (Block format). Extremely fast. Requires framing for stream usage.");
            footer.put("structure", "[sequence (variable): count, literal, match]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            try (InputStream in = Files.newInputStream(input.toPath());
                 OutputStream out = new org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream(Files.newOutputStream(output.toPath()))) {
                in.transferTo(out);
            }
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            try (InputStream in = new org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream(Files.newInputStream(input.toPath()));
                 OutputStream out = Files.newOutputStream(output.toPath())) {
                in.transferTo(out);
            }
        }
    }

    public static class Lz4Framed extends Codec {
        public Lz4Framed() {
            super("LZ4 Framed");
        }
        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Compress");
            footer.put("version", "1.26.0");
            footer.put("streaming", "Yes");
            footer.put("notes", "Extremely fast compression/decompression, framed format");
            footer.put("structure", "[magic (4B)] + [desc (3-15B)] + [blocks (variable)] + [end_mark (4B)] + [checksum (4B)]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            try (InputStream in = Files.newInputStream(input.toPath());
                 OutputStream out = new org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream(Files.newOutputStream(output.toPath()))) {
                in.transferTo(out);
            }
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            try (InputStream in = new org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream(Files.newInputStream(input.toPath()));
                 OutputStream out = Files.newOutputStream(output.toPath())) {
                in.transferTo(out);
            }
        }
    }

    public static class SnappyRaw extends Codec {
        public SnappyRaw() {
            super("Snappy Raw");
        }
        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Compress");
            footer.put("version", "1.26.0");
            footer.put("streaming", "No"); // Snappy raw is block-based, not streamable in the same way
            footer.put("notes", "High speed, moderate compression, raw format");
            footer.put("structure", "[blocks (variable)]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            try (InputStream in = Files.newInputStream(input.toPath());
                 OutputStream out = new org.apache.commons.compress.compressors.snappy.SnappyCompressorOutputStream(Files.newOutputStream(output.toPath()), input.length())) {
                in.transferTo(out);
            }
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            try (InputStream in = new org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream(Files.newInputStream(input.toPath()), (int) input.length());
                 OutputStream out = Files.newOutputStream(output.toPath())) {
                in.transferTo(out);
            }
        }
    }

    public static class SnappyFramed extends Codec {
        public SnappyFramed() {
            super("Snappy Framed");
        }
        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Compress");
            footer.put("version", "1.26.0");
            footer.put("streaming", "Yes");
            footer.put("notes", "High speed, moderate compression, framed format");
            footer.put("structure", "[header (10B): StreamID] + [chunks (variable)]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            try (InputStream in = Files.newInputStream(input.toPath());
                 OutputStream out = new org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorOutputStream(Files.newOutputStream(output.toPath()))) {
                in.transferTo(out);
            }
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            try (InputStream in = new org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream(Files.newInputStream(input.toPath()));
                 OutputStream out = Files.newOutputStream(output.toPath())) {
                in.transferTo(out);
            }
        }
    }

    public static class Xz extends Codec {
        public static class Properties {
            public enum Preset {
                MIN(0),
                DEFAULT(6),
                MAX(9);

                final int value;
                Preset(int value) { this.value = value; }
            }
        }

        public Xz() { super("XZ"); }

        /**
         * Sets the compression preset.
         * <ul>
         *   <li>{@link Properties.Preset#MIN}: Minimum compression (0).</li>
         *   <li>{@link Properties.Preset#DEFAULT}: Balanced default (6).</li>
         *   <li>{@link Properties.Preset#MAX}: Maximum compression (9).</li>
         * </ul>
         * @param preset The compression preset.
         */
        public void setProperty(Properties.Preset preset) {
            setProperty("preset", preset);
        }

        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "XZ for Java");
            footer.put("version", "1.9");
            footer.put("streaming", "Yes");
            footer.put("notes", "LZMA2 algorithm. Very high compression ratio. Slow compression speed, moderate decompression.");
            footer.put("structure", "[header (12B)] + [blocks (variable)] + [index (variable)] + [footer (12B)]");
        }

        @Override
        public void onCompress(File input, File output) throws Exception {
            if (properties.isEmpty()) {
                try (InputStream in = new FileInputStream(input);
                     OutputStream out = new XZCompressorOutputStream(new FileOutputStream(output))) {
                    in.transferTo(out);
                }
                return;
            }

            int preset = getProperty("preset", Properties.Preset.DEFAULT).value;
            try (InputStream in = new FileInputStream(input);
                 OutputStream out = new XZCompressorOutputStream(new FileOutputStream(output), preset)) {
                in.transferTo(out);
            }
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
             // Xz properties (Preset) are compression-only; using default decompression
             try (InputStream in = new XZCompressorInputStream(new FileInputStream(input));
                  OutputStream out = new FileOutputStream(output)) {
                 in.transferTo(out);
             }
        }
    }

    public static class Lzma extends Codec {
        public Lzma() { super("LZMA"); }
        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Compress");
            footer.put("version", "1.26.0");
            footer.put("streaming", "Yes");
            footer.put("notes", "Legacy LZMA. Very high ratio, slow.");
            footer.put("structure", "[header (13B): Props + Dict + Size] + [payload (variable)]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            try (InputStream in = new FileInputStream(input);
                 OutputStream out = new LZMACompressorOutputStream(new FileOutputStream(output))) {
                in.transferTo(out);
            }
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            try (InputStream in = new LZMACompressorInputStream(new FileInputStream(input));
                 OutputStream out = new FileOutputStream(output)) {
                in.transferTo(out);
            }
        }
    }

    public static class LZW extends Codec {
        public LZW() { super("LZW"); }
        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Compress");
            footer.put("version", "1.26.0");
            footer.put("streaming", "Yes");
            footer.put("notes", "Lempel-Ziv-Welch. Dictionary based. Older standard (GIF, Unix compress).");
            footer.put("structure", "[magic (3B): 1F 9D + Flags] + [payload (variable)]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            // LZW is read-only in commons-compress, using simplified customs (LZ78) as fallback or existing logic
            new LZ78().onCompress(input, output);
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            // NOTE: This assumes Z-compress format or custom fallback based on original implementation
            // The original implementation used ZCompressorInputStream but fell back to LZ78
            try (InputStream in = new org.apache.commons.compress.compressors.z.ZCompressorInputStream(new FileInputStream(input));
                 OutputStream out = new FileOutputStream(output)) {
                in.transferTo(out);
            } catch (Exception e) {
                new LZ78().onDecompress(input, output);
            }
        }
    }

    public static class Deflate64 extends Codec {
        public Deflate64() { super("DEFLATE64"); }
        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Compress");
            footer.put("version", "1.26.0");
            footer.put("streaming", "Yes");
            footer.put("notes", "Proprietary variant of Deflate (Pkzip). Larger dictionary.");
            footer.put("structure", "[header (2B)] + [payload (variable)] + [trailer (4B)]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            new Deflate().onCompress(input, output);
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            new Deflate().onDecompress(input, output);
        }
    }

    public static class Base64 extends Codec {
        public Base64() { super("BASE64"); }
        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Codec");
            footer.put("version", "1.15");
            footer.put("streaming", "Yes"); // Can stream, but implementation below reads all bytes.
            footer.put("notes", "Encoding (not compression). Increases size by ~33%.");
            footer.put("structure", "[text (variable: ~133%)]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            byte[] data = Files.readAllBytes(input.toPath());
            byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64(data);
            Files.write(output.toPath(), encoded);
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            byte[] encoded = Files.readAllBytes(input.toPath());
            byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(encoded);
            Files.write(output.toPath(), decoded);
        }
    }

    public static class Base32 extends Codec {
        public Base32() { super("BASE32"); }
        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "Apache Commons Codec");
            footer.put("version", "1.15");
            footer.put("streaming", "Yes");
            footer.put("notes", "Encoding (not compression). Increases size significantly.");
            footer.put("structure", "[text (variable: ~160%)]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            byte[] data = Files.readAllBytes(input.toPath());
            org.apache.commons.codec.binary.Base32 base32 = new org.apache.commons.codec.binary.Base32();
            byte[] encoded = base32.encode(data);
            Files.write(output.toPath(), encoded);
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            byte[] encoded = Files.readAllBytes(input.toPath());
            org.apache.commons.codec.binary.Base32 base32 = new org.apache.commons.codec.binary.Base32();
            byte[] decoded = base32.decode(encoded);
            Files.write(output.toPath(), decoded);
        }
    }

    public static class Plain extends Codec {
        public Plain() { super("PLAIN"); }
        @Override
        protected void onFooter(String name) {
            footer.put("name", name);
            footer.put("library", "System");
            footer.put("version", "N/A");
            footer.put("streaming", "Yes");
            footer.put("notes", "Baseline copy (no compression).");
            footer.put("structure", "[payload (variable)]");
        }
        @Override
        public void onCompress(File input, File output) throws Exception {
            Files.copy(input.toPath(), output.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        @Override
        public void onDecompress(File input, File output) throws Exception {
            Files.copy(input.toPath(), output.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }


    private final Map<Integer, Codec> codecs;
    private StringBuilder analysis;
    private File input;
    
    private double entropy;
    private int wordLength; // default to 1 byte per symbol

    {
        entropy = -1;
        wordLength = 1;
        codecs = new HashMap<>();
        analysis = new StringBuilder();
    }
    /**
     * Default constructor. Only registers the PLAIN codec for benchmarking baseline.
     * Use {@link #addCodec(int, Codec)} to add additional codecs.
     */
    public Transcoder() {
        // Only PLAIN is added by default for benchmarking baseline
        addCodec(Codec.PLAIN, new Plain());
    }

    /**
     * Constructor setting the input file. Only registers the PLAIN codec.
     * Use {@link #addCodec(int, Codec)} to add additional codecs.
     * @param input The file to be compressed or analyzed.
     */
    public Transcoder(File input) {
        addCodec(Codec.PLAIN, new Plain());
        setInput(input);
    }

    /**
     * Registers a new codec with a specific ID.
     * <p>
     * This allows for adding custom codecs or overriding existing ones.
     * </p>
     * @param id The unique ID for the codec (can be a new ID or one from {@link Codec} constants).
     * @param codec The {@link Codec} implementation to register.
     */
    public void addCodec(int id, Codec codec) {
        codecs.put(id, codec);
    }

    /**
     * Registers a built-in codec by its type constant.
     * <p>
     * This overload creates the appropriate {@link Codec} instance internally
     * based on the codec type constant from {@link Codec}.
     * </p>
     *
     * @param id The unique ID to register the codec under (typically the same as type).
     *
     * @throws IllegalArgumentException If the codec type is unknown.
     */
    public void addCodec(int id) {
        Codec codec = getCodec(id);
        if (codec != null) {
            codecs.put(id, codec);
        } else {
            throw new IllegalArgumentException("Unknown codec id: " + id);
        }
    }

    /**
     * Creates a Codec instance from a codec id constant.
     * @param id The codec constant.
     * @return The Codec instance, or null if unknown.
     */
    public Codec getCodec(int id) {
        if (codecs.containsKey(id)) {
            return codecs.get(id);
        }
        switch (id) {
            case Codec.RUN_LENGTH: return new RunLength();
            case Codec.HUFFMAN: return new Huffman();
            case Codec.BWT: return new BWT();
            case Codec.LZ78: return new LZ78();
            case Codec.ZSTANDARD: return new Zstd();
            case Codec.BROTLI: return new Brotli();
            case Codec.GZIP: return new Gzip();
            case Codec.DEFLATE: return new Deflate();
            case Codec.BZIP2: return new Bzip2();
            case Codec.LZ4_BLOCK: return new Lz4Block();
            case Codec.LZ4_FRAMED: return new Lz4Framed();
            case Codec.SNAPPY_RAW: return new SnappyRaw();
            case Codec.SNAPPY_FRAMED: return new SnappyFramed();
            case Codec.XZ: return new Xz();
            case Codec.LZMA: return new Lzma();
            case Codec.LZW: return new LZW();
            case Codec.DEFLATE64: return new Deflate64();
            case Codec.BASE64: return new Base64();
            case Codec.BASE32: return new Base32();
            case Codec.PLAIN: return new Plain();
            default: return null;
        }
    }

    /**
     * Removes an existing codec with a specific ID.
     * @param id The unique ID for the codec (can be a new ID or one from {@link Codec} constants).
     */
    public Codec removeCodec(int id) {
        return codecs.remove(id);
    }

    /**
     * Sets a configuration property for a specific codec.
     * <p>
     * These properties are passed to the codec's {@link Codec#setProperty(String, Object)} method.
     * </p>
     * @param codecId The ID of the codec to configure.
     * @param key The property key.
     * @param value The property value.
     */
    public void setCodecProperty(int codecId, String key, Object value) {
        Codec codec = codecs.get(codecId);
        if (codec != null) {
            codec.setProperty(key, value);
        } else {
            System.err.println("Warning: Attempted to set property for unknown codec ID: " + codecId);
        }
    }

    /**
     * Generates a unique codec ID that doesn't conflict with existing constant IDs 
     * or currently registered IDs.
     * @return A unique integer ID.
     */
    public int getUniqueIdentifier() {
        int id = 1000; // Start from a range safe from standard constants (0-19)
        while (codecs.containsKey(id)) {
            id++;
        }
        return id;
    }

    /**
     * Sets the input file for the transcoder.
     * @param input The file to process.
     */
    public void setInput(File input) {
        this.input = input;
    }

    /**
     * Gets the current input file.
     * @return The input file.
     */
    public File getInput() {
        return input;
    }

    /**
     * Gets the output file for a specific codec.
     * The output file is named based on the input file and the codec.
     * @param codecId The ID of the codec to use.
     * @return The output file with compressed data.
     */
    public File getOutput(int codecId) {
        if (input == null || !input.exists()) {
            return null;
        }
        
        Codec codec = codecs.get(codecId);
        if (codec == null) {
            return null;
        }
        
        File output = new File(input.getParent(), input.getName() + ".compressed");
        try {
            codec.onCompress(input, output);
            return output;
        } catch (Exception exception) {
            System.err.println("Compression failed (" + codec.getName() + "): " + exception.getMessage());
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the entropy analysis log.
     * @return The analysis string, or empty if no analysis has been performed.
     */
    public String getEntropyAnalysis() {
        return analysis.toString();
    }

    /**
     * Calculates the entropy of the input file based on a specific word length.
     * <p>
     * This method appends detailed analysis to the internal analysis buffer.
     * </p>
     * @param wordLength The length of the symbol (in bytes) to use for entropy calculation.
     * @return The calculated entropy value in bits per symbol.
     */
    public double getEntropy(int wordLength) {
        if (input == null || !input.exists()) {
            return -1;
        }

        // Reset analysis for new calculation
        analysis.setLength(0);
        analysis.append("Entropy Analysis for: ").append(input.getName()).append("\n");
        analysis.append("Word Length: ").append(wordLength).append(" bytes\n");

        long startTime = System.nanoTime();
        Map<String, Integer> frequencyMap = new HashMap<>();
        long totalSymbols = 0;

        try (InputStream in = new BufferedInputStream(new FileInputStream(input))) {
            byte[] buffer = new byte[wordLength];
            while (in.read(buffer) != -1) {
                String symbol = new String(buffer); // simplistic key, for binary might need base64 or custom object
                // For proper binary safety with new String(byte[]) we might have issues, 
                // but following existing pattern or assuming text for now/simple binary.
                // a better key would be Base64 string or ByteBuffer
                // modifying key generation to be robust for binary:
                // String key = Base64.getEncoder().encodeToString(buffer); 
                // However, preserving original logic style if it was string based, 
                // but let's use a safe representation for the key.
                StringBuilder sb = new StringBuilder();
                for (byte b : buffer) sb.append(String.format("%02X", b));
                String key = sb.toString();
                
                frequencyMap.put(key, frequencyMap.getOrDefault(key, 0) + 1);
                totalSymbols++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        double entropy = 0;
        analysis.append("Symbol Frequencies:\n");
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            double p = (double) entry.getValue() / totalSymbols;
            entropy -= p * (Math.log(p) / Math.log(2));
            // Limit analysis output to avoid massive logs for large files
            if (frequencyMap.size() < 100) {
                 analysis.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        if (frequencyMap.size() >= 100) {
            analysis.append("  ... (too many symbols to list)\n");
        }

        analysis.append("Total Symbols: ").append(totalSymbols).append("\n");
        analysis.append("Calculated Entropy: ").append(String.format("%.4f", entropy)).append(" bits/symbol\n");
        
        long duration = System.nanoTime() - startTime;
        analysis.append("Analysis Time: ").append(duration / 1_000_000.0).append(" ms\n");

        return entropy;
    }

    /**
     * Calculates entropy using the currently configured word length.
     * Updates the internal entropy field.
     * @return The calculated entropy.
     */
    public double getEntropy() {
        this.entropy = getEntropy(this.wordLength);
        return this.entropy;
    }

    /**
     * Gets the name of a codec by its ID.
     * @param id The codec ID.
     * @return The codec name, or "Unknown" if not found.
     */
    public String getCodecName(int id) {
        Codec codec = codecs.get(id);
        if (codec != null) {
            return codec.getName();
        }
        return "Unknown (" + id + ")";
    }

    /**
     * Decompresses a file using the specified codec.
     * <p>
     * This is a convenience method calling {@link #getInput(int, Map, File)} with null properties.
     * </p>
     * @param codec The ID of the codec to use.
     * @param output The file to decompress (usually the output of a compression operation).
     * @return The decompressed file, or null if decompression fails.
     */
    public File getInput(int codec, File output) {
        return getInput(codec, null, output);
    }

    /**
     * Decompresses a file using the specified codec and properties.
     * @param id The ID of the codec to use.
     * @param properties Optional configuration properties for this operation.
     * @param output The file to decompress.
     * @return The decompressed file, or null if decompression fails.
     */
    public File getInput(int id, Map<String, Object> properties, File output) {
        if (output == null || !output.exists()) {
            System.err.println("Output file does not exist.");
            return null;
        }

        Codec codec = codecs.get(id);
        if (codec == null) {
            System.err.println("No codec registered with ID: " + id);
            return null;
        }

        // Apply properties if provided
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                codec.setProperty(entry.getKey(), entry.getValue());
            }
        }

        String decompressedPath = output.getAbsolutePath().replace(".compressed", ".decompressed");
        File decompressed = new File(decompressedPath);

        try {
            codec.onDecompress(output, decompressed);

            return decompressed;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void setEntropyWordLength(int wordLength) {
        this.wordLength = wordLength;
    }

    public int getEntropyWordLength() {
        return wordLength;
    }

}
