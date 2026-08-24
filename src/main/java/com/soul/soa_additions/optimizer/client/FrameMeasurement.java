package com.soul.soa_additions.optimizer.client;

import net.minecraft.client.Minecraft;

import java.util.Arrays;

/**
 * What the client is actually achieving, which is the only honest input for graphics advice.
 *
 * <h2>Why not detect the GPU model</h2>
 *
 * <p>Knowing the adapter is called "Radeon RX 7800 XT" tunes nothing. Turning a model name into
 * a setting requires a lookup table of every GPU ever shipped, which is unmaintainable and
 * wrong the day a new card appears — and it would still be guessing, because the same card
 * performs completely differently at 1080p versus 4K, with shaders versus without, and on a
 * 60Hz versus a 165Hz panel.</p>
 *
 * <p>So the GPU name is kept only as a label the player recognises. The tuning inputs are
 * measured: what frame times this machine actually produces, against the budget its actual
 * display implies.</p>
 *
 * <h2>The budget</h2>
 *
 * <p>Refresh rate is the thing almost nothing accounts for. A 60Hz panel gives a 16.7ms budget;
 * a 144Hz panel gives 6.9ms. Identical hardware is comfortable on one and struggling on the
 * other, and advice that ignores this is guessing. It also decides whether a given GC pause
 * matters — a 14.7ms pause is invisible at 60Hz and drops two frames at 144Hz.</p>
 *
 * <p>Frame times come from Minecraft's own {@code FrameTimer}, the ring buffer behind the F3
 * lagometer, so this adds no per-frame instrumentation of its own.</p>
 */
public final class FrameMeasurement {

    /** Enough samples for a percentile to mean something. */
    private static final int MIN_SAMPLES = 60;

    private FrameMeasurement() {}

    public record Frames(int refreshHz, double budgetMs, double p50Ms, double p95Ms,
                         int fps, boolean measured) {

        /** Headroom at p95: 1.0 means exactly hitting budget, below 1.0 means missing it. */
        public double headroom() {
            return p95Ms <= 0 ? 0 : budgetMs / p95Ms;
        }

        public boolean comfortable() {
            return measured && headroom() >= 1.0;
        }

        /** Missing budget badly enough that settings should come down. */
        public boolean struggling() {
            return measured && headroom() < 0.6;
        }
    }

    public static Frames capture() {
        Minecraft mc = Minecraft.getInstance();

        int hz = 60;
        try {
            int r = mc.getWindow().getRefreshRate();
            if (r > 0) hz = r;
        } catch (Throwable ignored) {
            // Windowed/headless oddities: 60Hz is the safe assumption.
        }
        double budget = 1000.0 / hz;

        int fps = 0;
        try {
            fps = mc.getFps();
        } catch (Throwable ignored) {
            // Non-fatal; percentiles below are the number that matters.
        }

        double[] sorted = frameTimesMs(mc);
        if (sorted == null || sorted.length < MIN_SAMPLES) {
            return new Frames(hz, budget, 0, 0, fps, false);
        }
        double p50 = sorted[sorted.length / 2];
        double p95 = sorted[(int) (sorted.length * 0.95)];
        return new Frames(hz, budget, p50, p95, fps, true);
    }

    /**
     * Frame durations in milliseconds, sorted, or null if unavailable.
     *
     * <p>Called directly rather than reflectively: the workspace is Mojang-mapped but the shipped
     * jar runs against SRG, where {@code getFrameTimer} and {@code getLog} do not exist by those
     * names. Every reflective lookup threw in production, so the advice this feeds was silently
     * dead outside the dev environment. A compiled call site is remapped for us.</p>
     */
    private static double[] frameTimesMs(Minecraft mc) {
        try {
            long[] log = mc.getFrameTimer().getLog();
            if (log.length == 0) return null;

            double[] out = new double[log.length];
            int n = 0;
            for (long nanos : log) {
                if (nanos > 0) out[n++] = nanos / 1_000_000.0;
            }
            if (n < MIN_SAMPLES) return null;
            double[] trimmed = Arrays.copyOf(out, n);
            Arrays.sort(trimmed);
            return trimmed;
        } catch (Throwable t) {
            return null;
        }
    }
}
