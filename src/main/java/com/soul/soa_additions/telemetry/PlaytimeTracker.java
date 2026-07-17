package com.soul.soa_additions.telemetry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Counts in-world playtime on the client. Ticked once per client tick from
 * {@link ClientTelemetryHooks}; only ticks spent with a level loaded count,
 * so menu/loading time is excluded.
 *
 * <p>The lifetime total persists in {@code config/soa_additions/playtime_minutes.txt}
 * ({@link #flush()} is piggybacked on telemetry sends — no extra IO cadence).
 * Purely numeric counters and file IO, safe to class-load on either dist.
 */
public final class PlaytimeTracker {

    private static final Path FILE = Path.of("config", "soa_additions", "playtime_minutes.txt");
    private static final long TICKS_PER_MINUTE = 20L * 60L;

    /** Lifetime minutes recorded by PREVIOUS sessions; -1 = not loaded yet. */
    private static volatile long baseMinutes = -1;
    private static final AtomicLong sessionTicks = new AtomicLong();

    private PlaytimeTracker() {}

    public static void tick(boolean inWorld) {
        if (inWorld) sessionTicks.incrementAndGet();
    }

    public static int sessionMinutes() {
        return (int) (sessionTicks.get() / TICKS_PER_MINUTE);
    }

    public static int totalMinutes() {
        return (int) (base() + sessionMinutes());
    }

    /** Persist base + current session. Never advances {@link #baseMinutes}
     *  in-memory, so repeated flushes can't double-count the session. */
    public static synchronized void flush() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, Long.toString(base() + sessionMinutes()));
        } catch (IOException ignored) {
        }
    }

    private static long base() {
        long b = baseMinutes;
        if (b >= 0) return b;
        long loaded = 0;
        try {
            if (Files.exists(FILE)) {
                loaded = Long.parseLong(Files.readString(FILE).trim());
                if (loaded < 0) loaded = 0;
            }
        } catch (IOException | NumberFormatException ignored) {
        }
        baseMinutes = loaded;
        return loaded;
    }
}
