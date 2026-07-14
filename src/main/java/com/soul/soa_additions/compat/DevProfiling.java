package com.soul.soa_additions.compat;

/**
 * Master opt-in switch for the pack-development diagnostics (StartupProfiler,
 * TickAttribution, ChunkLoadAttribution, CompatScanner, ConfigScanner).
 *
 * <p>These tools stack-sample at 5-10 Hz, walk every mod jar on boot, and write
 * per-session reports — great while tuning the pack, but they are pure overhead
 * for players (safepoint pauses, disk I/O, unbounded report growth). They are
 * therefore OFF unless the JVM is launched with {@code -Dsoa.devProfiling=true}.
 *
 * <p>A system property (not a Forge config) because {@link StartupProfiler}
 * must decide in the mod constructor, before any config file has loaded.
 * The {@code /soaoptimizer} command can still run the CompatScanner manually
 * regardless of this flag — an explicit command is its own opt-in.
 */
public final class DevProfiling {

    public static final boolean ENABLED = Boolean.getBoolean("soa.devProfiling");

    private DevProfiling() {}
}
