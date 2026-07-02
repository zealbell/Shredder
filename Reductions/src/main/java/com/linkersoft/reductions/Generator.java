package com.linkersoft.reductions;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;

/**
 * Utility class for generating random files and managing file system blobs.
 */
public class Generator {

    public static final int MASK = 0xF0000000;
    public static final int B = 0x10000000;
    public static final int KB = 0x20000000;
    public static final int MB = 0x40000000;
    public static final int GB = 0x80000000;

    private static final String BASE_PATH = "src/main/resources/benchmarking/corpora/random";
    private static final byte[] BLOB_HEADER = "BLOB".getBytes();
    private static final byte BLOB_VERSION = 1;

    /**
     * Generates a random file of a specific size using a secure random number generator.
     * <p>
     * This method creates a file filled with random bytes. The file is saved in a directory structure
     * mirroring the unit and size (e.g., src/main/resources/benchmarking/corpora/random/KB/10KB/5.random).
     * This is useful for creating diverse datasets for benchmarking compression or storage.
     * </p>
     *
     * @param sunit The defined size and unit constraint (e.g., 10KB, 5MB), encoded as an integer.
     * @param index The unique index for the file name (e.g., "5.random") to distinguish multiple files.
     */
    public static void setRandomFile(int sunit, int index) {
        setRandomInternally(sunit, index, new SecureRandom());
    }

    /**
     * Generates a random file of a specific size using a seeded random number generator for reproducibility.
     * <p>
     * This method is identical to {@link #setRandomFile(int, int)} but accepts a seed.
     * Using the same seed guarantees that the generated file content will be bitwise identical
     * every time, which is critical for deterministic testing of reduction and compression algorithms.
     * </p>
     *
     * @param sunit The defined size and unit constraint (e.g., 10KB, 5MB), encoded as an integer.
     * @param index The unique index for the file name.
     * @param seed  The initial seed for the {@link Random} generator.
     */
    public static void setRandomFile(int sunit, int index, int seed) {
        setRandomInternally(sunit, index, new Random(seed));
    }

    public static File getRandomFile(int sunit) {
        int unitMask = sunit & MASK;
        int size = sunit & ~MASK;

        String unit;
        if (unitMask == B) {
            unit = "B";
        } else if (unitMask == KB) {
            unit = "KB";
        } else if (unitMask == MB) {
            unit = "MB";
        } else if (unitMask == GB) {
            unit = "GB";
        } else {
            throw new IllegalArgumentException("Unknown unit in sunit: " + Integer.toHexString(unitMask));
        }

        String folder = size + unit;
        return new File(BASE_PATH, unit + File.separator + folder);
    }
    
    /**
     * Calculates the CRC32 checksum of a file.
     *
     * @param location The absolute path to the file.
     * @return The CRC32 checksum of the file content.
     */
    public static long getChecksum(String location) {
        CRC32 crc = new CRC32();
        try (FileInputStream in = new FileInputStream(location);
             FileChannel channel = in.getChannel()) {

            long size = channel.size();
            long position = 0;
            long bufferSize = 128 * 1024 * 1024; // 128MB chunks

            while (position < size) {
                long remaining = size - position;
                long mapSize = Math.min(remaining, bufferSize);
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, position, mapSize);
                crc.update(buffer);
                position += mapSize;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return -1;
        }
        return crc.getValue();
    }

    /**
     * Calculates the CRC32 checksum of a generated random file.
     *
     * @param sunit The size and unit of the file.
     * @param index The index of the file.
     * @return The CRC32 checksum of the resolved file.
     */
    public static long getChecksum(int sunit, int index) {
        File directory = getRandomFile(sunit);
        File file = new File(directory, index + ".random");
        if (!file.exists()) {
            System.err.println("File not found for checksum: " + file.getAbsolutePath());
            return -1;
        }
        return getChecksum(file.getAbsolutePath());
    }

    /**
     * Converts a folder at source path to a single binary blob file at target path.
     * <p>
     * Blob Format:
     * Header: "BLOB" (4 bytes) + Version (1 byte)
     * Item:
     * - Tag (1 byte): 0x00=File, 0x01=Directory
     * - Name Length (VarInt)
     * - Name (UTF-8 bytes)
     * - Content Length (VarInt): File size or Size of encoded children
     * - Content: File bytes or Sequence of Items
     * </p>
     *
     * @param source Path to the source directory.
     * @param target Path to the target .blob file.
     * @return The created blob file.
     */
    public static File toBlob(String source, String target) {
        File sourceDir = new File(source);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Source must be an existing directory: " + source);
        }

        File targetFile = new File(target);
        
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(targetFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            bos.write(BLOB_HEADER);
            bos.write(BLOB_VERSION);

            List<File> allFiles = new ArrayList<>();
            long totalSourceSize = encodeDirectory(sourceDir, bos, allFiles);
            
            bos.flush();
            long blobSize = targetFile.length();
            long sizeDiff = blobSize - totalSourceSize;


            System.out.println("\n================================================================================================================");
            System.out.println("Blob Creation: " + sourceDir.getAbsolutePath());
            System.out.println("================================================================================================================");
            for (File f : allFiles) {
                System.out.println(" - " + f.getAbsolutePath().substring(sourceDir.getAbsolutePath().length() + 1));
            }
            System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
            System.out.println("Size (Source): " + formatSize(totalSourceSize) + " ("  + (totalSourceSize * 8) + " Bits → " + String.format("%.3f KB", (totalSourceSize / 1024.0))  + " | " + String.format("%.6f MB", (totalSourceSize / (1024.0 * 1024.0))) + " | " + String.format("%.9f GB", (totalSourceSize / (1024.0 * 1024.0 * 1024.0))) + ")");
            System.out.println("Size (Blob): " + formatSize(blobSize) + " ("  + (blobSize * 8) + " Bits → " + String.format("%.3f KB", (blobSize / 1024.0))  + " | " + String.format("%.6f MB", (blobSize / (1024.0 * 1024.0))) + " | " + String.format("%.9f GB", (blobSize / (1024.0 * 1024.0 * 1024.0))) + ")");
            System.out.println("Size (Difference/Overhead): " + formatSize(sizeDiff) + " ("  + (sizeDiff * 8) + " Bits → " + String.format("%.3f KB", (sizeDiff / 1024.0))  + " | " + String.format("%.6f MB", (sizeDiff / (1024.0 * 1024.0))) + " | " + String.format("%.9f GB", (sizeDiff / (1024.0 * 1024.0 * 1024.0))) + ")");
            System.out.println("----------------------------------------------------------------------------------------------------------------\n");

        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return targetFile;
    }

    /**
     * Restores a folder from a blob file.
     *
     * @param source Path to the source .blob file.
     * @param target Path to the target directory to create.
     * @return The target directory file.
     */
    public static File fromBlob(String source, String target) {
        File blobFile = new File(source);
        File targetDir = new File(target);

        if (!blobFile.exists()) {
            throw new IllegalArgumentException("Source blob does not exist: " + source);
        }

        try (FileInputStream fis = new FileInputStream(blobFile);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            byte[] header = new byte[4];
            if (bis.read(header) != 4 || !new String(header).equals("BLOB")) {
                throw new IOException("Invalid blob header");
            }
            int version = bis.read();
            if (version != BLOB_VERSION) {
                throw new IOException("Unsupported blob version: " + version);
            }

            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            decodeDirectoryItems(bis, targetDir);

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return targetDir;
    }

    private static void setRandomInternally(int sunit, int index, Random random) {
        int unitMask = sunit & MASK;
        int size = sunit & ~MASK;

        long total;

        if (unitMask == B) {
            total = size;
        } else if (unitMask == KB) {
            total = (long) size * 1024;
        } else if (unitMask == MB) {
            total = (long) size * 1024 * 1024;
        } else if (unitMask == GB) {
            total = (long) size * 1024 * 1024 * 1024;
        } else {
            throw new IllegalArgumentException("Unknown unit in sunit: " + Integer.toHexString(unitMask));
        }

        File directory = getRandomFile(sunit);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                System.err.println("Failed to create directory: " + directory.getAbsolutePath());
                return;
            }
        }

        File output = new File(directory, index + ".random");

        try (OutputStream out = new FileOutputStream(output)) {
            byte[] buffer = new byte[64 * 1024]; // 64KB buffer
            long written = 0;

            while (written < total) {
                int bytesToWrite = (int) Math.min(buffer.length, total - written);
                random.nextBytes(buffer);
                out.write(buffer, 0, bytesToWrite);
                written += bytesToWrite;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static long encodeDirectory(File dir, OutputStream out, List<File> fileList) throws IOException {
        long dirContentSize = 0;
        File[] children = dir.listFiles();
        if (children != null) {
            ByteArrayOutputStream childrenBuffer = new ByteArrayOutputStream();
            
            for (File child : children) {
                fileList.add(child);
                if (child.isDirectory()) {
                    childrenBuffer.write(0x01); // Tag: Directory
                    byte[] nameBytes = child.getName().getBytes("UTF-8");
                    writeVarInt(childrenBuffer, nameBytes.length);
                    childrenBuffer.write(nameBytes);
                    
                    ByteArrayOutputStream subChildBuffer = new ByteArrayOutputStream();
                    long subSize = encodeDirectory(child, subChildBuffer, fileList);
                    byte[] subChildBytes = subChildBuffer.toByteArray();
                    
                    writeVarInt(childrenBuffer, subChildBytes.length);
                    childrenBuffer.write(subChildBytes);
                    
                    dirContentSize += subSize;
                } else {
                    childrenBuffer.write(0x00); // Tag: File
                    byte[] nameBytes = child.getName().getBytes("UTF-8");
                    writeVarInt(childrenBuffer, nameBytes.length);
                    childrenBuffer.write(nameBytes);
                    
                    long fileLen = child.length();
                    writeVarInt(childrenBuffer, (int) fileLen); 
                    
                    try (FileInputStream fis = new FileInputStream(child)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = fis.read(buffer)) != -1) {
                            childrenBuffer.write(buffer, 0, read);
                        }
                    }
                    dirContentSize += fileLen;
                }
            }
            childrenBuffer.writeTo(out);
        }
        return dirContentSize;
    }

    private static void decodeDirectoryItems(InputStream in, File currentDir) throws IOException {
        while (true) {
            in.mark(1);
            int tag = in.read();
            if (tag == -1) break; // EOF

            int nameLen = readVarInt(in);
            byte[] nameBytes = new byte[nameLen];
            int readName = in.read(nameBytes);
            if (readName != nameLen) throw new IOException("Unexpected EOF reading name");
            String name = new String(nameBytes, "UTF-8");

            int contentLen = readVarInt(in);
            File child = new File(currentDir, name);

            if (tag == 0x01) { // Directory
                child.mkdirs();
                byte[] dirContent = new byte[contentLen];
                int readDir = 0;
                while(readDir < contentLen) {
                    int r = in.read(dirContent, readDir, contentLen - readDir);
                    if (r == -1) break;
                    readDir += r;
                }
                
                try (ByteArrayInputStream bais = new ByteArrayInputStream(dirContent)) {
                    decodeDirectoryItems(bais, child);
                }
            } else if (tag == 0x00) { // File
                try (FileOutputStream fos = new FileOutputStream(child)) {
                    byte[] buffer = new byte[8192];
                    int remaining = contentLen;
                    while (remaining > 0) {
                        int toRead = Math.min(buffer.length, remaining);
                        int r = in.read(buffer, 0, toRead);
                        if (r == -1) throw new IOException("Unexpected EOF reading file content");
                        fos.write(buffer, 0, r);
                        remaining -= r;
                    }
                }
            } else {
                throw new IOException("Unknown tag: " + tag);
            }
        }
    }

    private static void writeVarInt(OutputStream out, int value) throws IOException {
        while ((value & 0xFFFFFF80) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value & 0x7F);
    }

    private static int readVarInt(InputStream in) throws IOException {
        int value = 0;
        int shift = 0;
        int b;
        while ((b = in.read()) != -1) {
            value |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return value;
            }
            shift += 7;
        }
        throw new EOFException();
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

}
