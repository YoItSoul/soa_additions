package com.soul.soa_additions.optimizer.client;

import com.soul.soa_additions.optimizer.HardwareProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns measurements into graphics advice.
 *
 * <p>Every suggestion here is derived from something observed on this machine — measured frame
 * times against the budget its actual refresh rate implies — rather than from a hardware model
 * name. That matters because the same GPU is comfortable at 1080p/60Hz without shaders and
 * hopeless at 1440p/144Hz with them, and only the measurement knows which one the player is
 * actually doing.</p>
 *
 * <p>Nothing is applied automatically. The screen shows these and the player chooses; a pack
 * silently rewriting someone's video settings is worse than a pack that stutters.</p>
 */
public final class SettingsAdvisor {

    /** Below this fraction of budget at p95, settings need to come down. */
    private static final double STRUGGLING = 0.6;

    /** Above this, there is room to spend on quality. */
    private static final double ROOMY = 1.6;

    private SettingsAdvisor() {}

    /** One recommendation: what to change, from what to what, and the measurement behind it. */
    public record Suggestion(String setting, String current, String suggested, String why) {}

    public static List<Suggestion> advise(HardwareProfile hw, FrameMeasurement.Frames f) {
        List<Suggestion> out = new ArrayList<>();
        Options o = Minecraft.getInstance().options;

        if (!f.measured()) {
            // No credible frame data yet: say so rather than inventing advice.
            return out;
        }

        int render = o.renderDistance().get();
        int sim = o.simulationDistance().get();
        double headroom = f.headroom();
        String budgetNote = String.format("p95 frame %.1fms against a %.1fms budget at %dHz",
                f.p95Ms(), f.budgetMs(), f.refreshHz());

        if (headroom < STRUGGLING) {
            if (render > 8) {
                out.add(new Suggestion("Render distance", render + " chunks",
                        Math.max(8, render - 4) + " chunks",
                        "Terrain is the largest per-frame cost; " + budgetNote));
            }
            if (isShaderPackActive()) {
                out.add(new Suggestion("Shaders", "on", "off, or a lighter pack",
                        "Shadow and lighting passes dominate the frame when shaders are on; " + budgetNote));
            }
            if (hw.tier() == HardwareProfile.Tier.LOW) {
                out.add(new Suggestion("Lite Mode", "off", "on",
                        "Disables purely cosmetic mods and frees memory; this machine reports "
                                + (hw.ramUnknown() ? "limited RAM" : hw.totalRamGb() + "G RAM")
                                + " and " + hw.cores() + " cores"));
            }
        } else if (headroom > ROOMY) {
            if (render < 16) {
                out.add(new Suggestion("Render distance", render + " chunks",
                        Math.min(16, render + 4) + " chunks",
                        "Spare frame budget available; " + budgetNote));
            }
            if (!isShaderPackActive() && hw.discreteGpu()) {
                out.add(new Suggestion("Shaders", "off", "worth trying",
                        "Spare frame budget available; " + budgetNote));
            }
        }

        // Simulation distance is a server-thread cost, independent of frame budget.
        if (sim > 6 && hw.cores() <= 6) {
            out.add(new Suggestion("Simulation distance", sim + " chunks", "6 chunks",
                    "Entity ticking scales with this and it competes for the same cores; "
                            + hw.cores() + " cores detected"));
        }

        return out;
    }

    /** True when Oculus/Iris has a pack loaded. Reflective: Oculus is not a compile dependency. */
    private static boolean isShaderPackActive() {
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            Object using = instance.getClass().getMethod("isShaderPackInUse").invoke(instance);
            return using instanceof Boolean b && b;
        } catch (Throwable t) {
            return false;
        }
    }
}
