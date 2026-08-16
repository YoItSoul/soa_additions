package com.soul.soa_additions.optimizer;

import java.lang.management.ManagementFactory;
import java.util.Locale;

/**
 * What this machine is, detected once, used by everything that gives the player advice.
 *
 * <p>The single source of truth for hardware capability. {@link JvmAdvisor} consumes it to
 * produce launch flags; the in-game settings recommendation consumes it to produce render
 * distance and quality suggestions. Deliberately one detector rather than two: the launch-time
 * and runtime advice answer the same question ("what should this machine run?") from the same
 * inputs, and two detectors would eventually disagree and tell one player two different things.</p>
 *
 * <p><strong>No process execution.</strong> Everything here is JMX or an OpenGL string already
 * held by the running context. Shelling out to {@code wmic} or {@code systeminfo} would be the
 * obvious way to get richer data and is exactly the behaviour that gets a mod flagged on
 * CurseForge.</p>
 */
public record HardwareProfile(long totalRamGb, int cores, String gpuRenderer, String gpuVendor) {

    /** Broad capability bands used for settings advice. */
    public enum Tier {
        /** Handhelds, old laptops, 8G machines. Wants Lite Mode. */
        LOW,
        /** The common case: 16G, a mid GPU. */
        MEDIUM,
        /** 24G+ and a discrete GPU with headroom for shaders. */
        HIGH
    }

    /**
     * GPU strings are client-only, so the client sets them here at startup rather than this
     * class reaching for OpenGL — which would make it unloadable on a dedicated server.
     */
    private static volatile String clientGpuRenderer;
    private static volatile String clientGpuVendor;

    public static void setClientGpu(String renderer, String vendor) {
        clientGpuRenderer = renderer;
        clientGpuVendor = vendor;
    }

    public static HardwareProfile detect() {
        return new HardwareProfile(detectTotalRamGb(), Runtime.getRuntime().availableProcessors(),
                clientGpuRenderer, clientGpuVendor);
    }

    /**
     * Physical RAM in GB, or -1 if the JVM will not tell us.
     *
     * <p>Named {@code detectTotalRamGb} rather than {@code totalRamGb} because a record's
     * component accessor must be public — a private static method of the same name is read as
     * a failed attempt to implement it, not as a helper.</p>
     */
    private static long detectTotalRamGb() {
        try {
            java.lang.management.OperatingSystemMXBean base = ManagementFactory.getOperatingSystemMXBean();
            if (base instanceof com.sun.management.OperatingSystemMXBean os) {
                return Math.round(os.getTotalMemorySize() / (double) (1L << 30));
            }
        } catch (Throwable ignored) {
            // Some JREs restrict the com.sun extension; fall through to unknown.
        }
        return -1;
    }

    /** True when RAM could not be determined and callers should fall back to a safe default. */
    public boolean ramUnknown() {
        return totalRamGb <= 0;
    }

    public Tier tier() {
        if (ramUnknown()) return Tier.MEDIUM;
        if (totalRamGb < 12 || cores <= 4) return Tier.LOW;
        if (totalRamGb < 24 || cores <= 8) return Tier.MEDIUM;
        return Tier.HIGH;
    }

    /**
     * Whether the renderer looks like a real discrete GPU.
     *
     * <p>Matched against the live {@code GL_RENDERER} string rather than an adapter
     * enumeration on purpose. Enumerating adapters is unreliable — the dev machine for this
     * pack lists a Parsec virtual display, a Meta virtual monitor and a USB display device
     * ahead of its actual Radeon — whereas {@code GL_RENDERER} names the device actually
     * doing the drawing.</p>
     */
    public boolean discreteGpu() {
        if (gpuRenderer == null) return false;
        String r = gpuRenderer.toLowerCase(Locale.ROOT);
        boolean integrated = r.contains("intel") && !r.contains("arc");
        boolean software = r.contains("llvmpipe") || r.contains("softpipe") || r.contains("swiftshader");
        return !integrated && !software;
    }

    /** Heap this machine should launch with. Delegates so there is one table, not two. */
    public int recommendedHeapGb() {
        return JvmAdvisor.recommendedHeapGb(ramUnknown() ? 16 : totalRamGb);
    }

    /** One-line summary for command output and the first-run screen. */
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(ramUnknown() ? "unknown RAM" : totalRamGb + "G RAM");
        sb.append(", ").append(cores).append(" cores");
        if (gpuRenderer != null && !gpuRenderer.isBlank()) {
            sb.append(", ").append(gpuRenderer.length() > 40
                    ? gpuRenderer.substring(0, 40) + "…" : gpuRenderer);
        }
        return sb.toString();
    }
}
