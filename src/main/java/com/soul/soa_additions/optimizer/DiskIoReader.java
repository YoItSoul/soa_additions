package com.soul.soa_additions.optimizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads cumulative process disk-IO byte counters. Linux-only via {@code /proc/self/io};
 * other platforms return {@link #UNSUPPORTED}. The sampler turns successive readings into
 * a {@code disk_read_mb_s} / {@code disk_write_mb_s} rate.
 *
 * <p>We deliberately avoid JFR's per-event {@code jdk.FileRead}/{@code jdk.FileWrite} so the
 * profiler stays near-zero overhead.
 */
final class DiskIoReader {

    static final long[] UNSUPPORTED = new long[]{-1L, -1L};

    private static final Path PROC_IO = Path.of("/proc/self/io");
    private static final boolean AVAILABLE = Files.isReadable(PROC_IO);

    private DiskIoReader() {}

    static boolean isAvailable() {
        return AVAILABLE;
    }

    /** @return {@code [readBytes, writeBytes]} cumulative since process start, or {@link #UNSUPPORTED}. */
    static long[] read() {
        if (!AVAILABLE) return UNSUPPORTED;
        try {
            long read = -1, write = -1;
            List<String> lines = Files.readAllLines(PROC_IO);
            for (String line : lines) {
                if (line.startsWith("read_bytes:"))  read  = Long.parseLong(line.substring(11).trim());
                else if (line.startsWith("write_bytes:")) write = Long.parseLong(line.substring(12).trim());
            }
            if (read < 0 || write < 0) return UNSUPPORTED;
            return new long[]{read, write};
        } catch (IOException | NumberFormatException e) {
            return UNSUPPORTED;
        }
    }
}
