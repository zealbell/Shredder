package com.verifier;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

/**
 * Computes checksums (CRC32, Adler-32) and hashes (MD5, SHA-1, SHA-256) for
 * files on a small background pool, reading each file only once and updating
 * all five algorithms per buffer. Results are cached per file identity
 * (path + size + mtime) so re-expanding a node is instant.
 */
final class DigestService {

    /** Algorithm display names, in the order rows appear under a file. */
    static final String[] ALGORITHMS = {"CRC32", "ADLER-32", "MD5", "SHA-1", "SHA-256"};

    private final ExecutorService pool = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            r -> {
                Thread t = new Thread(r, "digest-worker");
                t.setDaemon(true);
                return t;
            });

    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();
    private final SimpleIntegerProperty pending = new SimpleIntegerProperty(0);

    /** Number of files currently queued or being hashed (FX-thread property). */
    ReadOnlyIntegerProperty pendingProperty() {
        return pending;
    }

    /**
     * Computes (or serves from cache) all digests for {@code file} and calls
     * exactly one of the consumers back on the FX application thread.
     */
    void compute(Path file, Consumer<Map<String, String>> onDone, Consumer<String> onError) {
        String key = cacheKey(file);
        if (key != null) {
            Map<String, String> hit = cache.get(key);
            if (hit != null) {
                onDone.accept(hit);
                return;
            }
        }
        pending.set(pending.get() + 1);
        pool.submit(() -> {
            try {
                Map<String, String> result = digest(file);
                if (key != null) {
                    cache.put(key, result);
                }
                Platform.runLater(() -> {
                    pending.set(pending.get() - 1);
                    onDone.accept(result);
                });
            } catch (Exception ex) {
                String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
                Platform.runLater(() -> {
                    pending.set(pending.get() - 1);
                    onError.accept(message);
                });
            }
        });
    }

    void shutdown() {
        pool.shutdownNow();
    }

    /** Identity of the file contents; null means "don't cache". */
    private static String cacheKey(Path file) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
            return file.toAbsolutePath() + "|" + attrs.size() + "|" + attrs.lastModifiedTime().toMillis();
        } catch (IOException e) {
            return null;
        }
    }

    /** Single pass over the file updating every algorithm at once. */
    private static Map<String, String> digest(Path file) throws IOException, NoSuchAlgorithmException {
        CRC32 crc32 = new CRC32();
        Adler32 adler32 = new Adler32();
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        byte[] buffer = new byte[64 * 1024];
        try (InputStream in = Files.newInputStream(file)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                crc32.update(buffer, 0, read);
                adler32.update(buffer, 0, read);
                md5.update(buffer, 0, read);
                sha1.update(buffer, 0, read);
                sha256.update(buffer, 0, read);
            }
        }

        Map<String, String> result = new LinkedHashMap<>();
        result.put("CRC32", String.format("%08x", crc32.getValue()));
        result.put("ADLER-32", String.format("%08x", adler32.getValue()));
        result.put("MD5", hex(md5.digest()));
        result.put("SHA-1", hex(sha1.digest()));
        result.put("SHA-256", hex(sha256.digest()));
        return result;
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
