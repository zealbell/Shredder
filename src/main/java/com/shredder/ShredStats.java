package com.shredder;

/**
 * Mutable tally of what a shred run touched. Read on the JavaFX thread once the
 * background task has finished.
 */
public final class ShredStats {
    long filesShredded;
    long foldersDeleted;
    long bytesOverwritten;
    long errors;
    long elapsedMillis;

    public long getFilesShredded()    { return filesShredded; }
    public long getFoldersDeleted()   { return foldersDeleted; }
    public long getBytesOverwritten() { return bytesOverwritten; }
    public long getErrors()           { return errors; }
    public long getElapsedMillis()    { return elapsedMillis; }

    /** Human-readable byte count, e.g. 1.4 MB. */
    public static String humanBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        String units = "KMGTPE";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        exp = Math.min(exp, units.length());
        double value = bytes / Math.pow(1024, exp);
        return String.format("%.1f %sB", value, units.charAt(exp - 1));
    }
}
