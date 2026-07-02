package com.shredder;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Background job that shreds every file under a root folder:
 * <ol>
 *   <li>recursively lists all files,</li>
 *   <li>overwrites each file's contents with {@code (byte) 0},</li>
 *   <li>deletes every file, then removes the now-empty folders.</li>
 * </ol>
 *
 * Runs off the UI thread as a JavaFX {@link Task}; progress and a running log
 * are published so the UI can bind to them.
 */
public class ShredService extends Task<ShredStats> {

    private static final int BUFFER_SIZE = 1 << 20; // 1 MiB of zeros

    private final Path root;
    private final ShredStats stats = new ShredStats();

    public ShredService(Path root) {
        this.root = root;
    }

    @Override
    protected ShredStats call() throws Exception {
        long start = System.currentTimeMillis();

        // Pin the bar to a determinate 0% up front; without this the task's
        // progress starts at -1 and the bar shows the indeterminate to-and-fro
        // animation while we scan.
        updateProgress(0, 1);

        // Phase 1: enumerate files and folders (folders kept for later removal).
        updateMessage("Scanning " + root + " …");
        List<Path> files = new ArrayList<>();
        List<Path> folders = new ArrayList<>();
        collect(files, folders);

        long total = files.size();
        updateMessage("Found " + total + " file(s) in " + folders.size() + " folder(s).");

        // Phase 2: overwrite with zeros, then delete each file.
        ByteBuffer zeros = ByteBuffer.allocateDirect(BUFFER_SIZE); // already zero-filled
        long done = 0;
        for (Path file : files) {
            if (isCancelled()) {
                updateMessage("Cancelled.");
                break;
            }
            // Show which file is being shredded right now, below the bar.
            updateTitle(file.getFileName().toString());
            try {
                long size = Files.size(file);
                overwriteWithZeros(file, size, zeros);
                Files.deleteIfExists(file);
                stats.filesShredded++;
                stats.bytesOverwritten += size;
            } catch (IOException ex) {
                stats.errors++;
                updateMessage("Skipped (" + ex.getClass().getSimpleName() + "): " + file);
            }
            updateProgress(++done, total);
        }
        // All files handled — no "current file" to show anymore.
        updateTitle("");

        // Phase 3: remove folders deepest-first so parents empty out.
        if (!isCancelled()) {
            folders.sort(Comparator.comparingInt(Path::getNameCount).reversed());
            for (Path folder : folders) {
                try {
                    if (Files.deleteIfExists(folder)) {
                        stats.foldersDeleted++;
                    }
                } catch (IOException ex) {
                    stats.errors++;
                }
            }
        }

        stats.elapsedMillis = System.currentTimeMillis() - start;
        updateMessage(isCancelled() ? "Stopped." : "Done.");
        return stats;
    }

    /** Walk the tree, gathering regular files and directories separately. */
    private void collect(List<Path> files, List<Path> folders) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                // Overwrite only real files; a symlink is unlinked (deleted) without
                // touching whatever it points at.
                if (attrs.isSymbolicLink() || attrs.isOther()) {
                    files.add(file);
                } else if (attrs.isRegularFile()) {
                    files.add(file);
                }
                return isCancelled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                folders.add(dir);
                return isCancelled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                stats.errors++;
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /** Write {@code (byte) 0} across the whole file and flush it to disk. */
    private void overwriteWithZeros(Path file, long size, ByteBuffer zeros) throws IOException {
        if (Files.isSymbolicLink(file)) {
            return; // never write through a link; it is simply deleted afterwards
        }
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            long remaining = size;
            while (remaining > 0) {
                zeros.clear();
                int chunk = (int) Math.min(BUFFER_SIZE, remaining);
                zeros.limit(chunk);
                remaining -= channel.write(zeros);
            }
            channel.force(true);
        }
    }
}
