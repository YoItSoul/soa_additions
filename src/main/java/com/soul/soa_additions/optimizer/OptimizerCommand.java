package com.soul.soa_additions.optimizer;

import com.mojang.brigadier.CommandDispatcher;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.compat.CompatScanner;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import com.soul.soa_additions.optimizer.client.FrameMeasurement;
import com.soul.soa_additions.optimizer.client.SettingsAdvisor;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * {@code /soa optimizer} — prints the latest profiler snapshot to chat. Lets ops eyeball
 * heap, GC totals, TPS, and the suggested {@code -Xmx} without alt-tabbing to the CSV.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class OptimizerCommand {

    private OptimizerCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soa")
                .then(Commands.literal("optimizer")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                    JvmStatsSampler.Snapshot snap = JvmStatsSampler.snapshot();
                    if (snap == null) {
                        ctx.getSource().sendFailure(Component.literal("Profiler is not running."));
                        return 0;
                    }
                    var src = ctx.getSource();
                    src.sendSuccess(() -> Component.literal("── SOA JVM Profiler ──").withStyle(ChatFormatting.GOLD), false);
                    src.sendSuccess(() -> line("heap", snap.heapUsedMb() + " / " + snap.heapMaxMb() + " MB ("
                            + String.format("%.1f", snap.heapPct()) + "%)"), false);
                    src.sendSuccess(() -> line("alloc rate (avg)", String.format("%.1f MB/s", snap.allocRateGuessMbS())), false);
                    src.sendSuccess(() -> line("gc total", snap.totalGcMillis() + " ms (longest " + snap.longestGcMillis() + " ms)"), false);
                    if (snap.avgTps() >= 0) src.sendSuccess(() -> line("avg tps", String.format("%.2f", snap.avgTps())), false);
                    src.sendSuccess(() -> line("samples", String.valueOf(snap.sampleCount())), false);
                    src.sendSuccess(() -> line("suggested -Xmx", snap.suggestedXmxMb() + "M"), false);
                    src.sendSuccess(() -> Component.literal("file: " + snap.sessionFile().getFileName()).withStyle(ChatFormatting.DARK_GRAY), false);
                    return 1;
                }))
                .then(Commands.literal("compat")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                    var src = ctx.getSource();
                    src.sendSuccess(() -> Component.literal("Running compat scan…").withStyle(ChatFormatting.GRAY), false);
                    new Thread(() -> {
                        try {
                            var report = CompatScanner.runScan();
                            src.sendSuccess(() -> Component.literal("Report → " + report.getFileName()).withStyle(ChatFormatting.GREEN), false);
                        } catch (Exception e) {
                            src.sendFailure(Component.literal("Compat scan failed: " + e.getMessage()));
                        }
                    }, "SOA-CompatScan-OnDemand").start();
                    return 1;
                }))
                .then(Commands.literal("jvm")
                        .executes(ctx -> jvm(ctx.getSource(), false))
                        .then(Commands.literal("why")
                                .executes(ctx -> jvm(ctx.getSource(), true))))
                // Console, RCON and command blocks only — never a player.
                //
                // This registration executes on the SERVER, so it measures the server's JVM.
                // A player invoking it would file a report describing the server's heap and GC
                // under their own name, which is worse than no report at all. Players get the
                // client-side registration (TuneClientCommand), which measures their own
                // machine and never leaves it; Forge checks the client dispatcher first, so
                // for a player this branch is unreachable anyway. The requires() clause makes
                // that guarantee explicit rather than relying on dispatcher ordering.
                .then(Commands.literal("tune")
                        .requires(src -> src.getEntity() == null)
                        .executes(ctx -> tune(ctx.getSource()))
                        .then(Commands.literal("send")
                                .executes(ctx -> tuneSend(ctx.getSource()))))
                // /soa pregen is deliberately gone (2026-08-12). C2ME's parallel chunk
                // generation made travel effectively instant even at max render distance, so the
                // stutter the whole pre-generation flow existed to hide no longer happens. Use
                // Chunky's own /chunky commands if a world ever needs pre-generating.
        );
    }

    /**
     * {@code /soa jvm} — prints the launch arguments this machine should be using.
     *
     * <p>No permission requirement: the whole point is that any player can read their own
     * numbers and fix their launcher. It only reports on the JVM the player is already
     * running and cannot change anything.</p>
     */
    private static int jvm(CommandSourceStack src, boolean explain) {
        HardwareProfile hw = HardwareProfile.detect();
        HeapMeasurement heap = HeapMeasurement.capture();
        JvmAdvisor.Recommendation rec = JvmAdvisor.recommend(hw, heap);
        GcCapability gc = GcCapability.measure();
        int heapGb = rec.heapGb();
        String args = JvmAdvisor.argsFor(heapGb, gc);
        boolean tuned = JvmAdvisor.runningTunedProfile();
        long ramGb = hw.totalRamGb();

        JvmAdvisor.Profile profile = JvmAdvisor.currentProfile();
        src.sendSuccess(() -> Component.literal("── SOA JVM Arguments ──").withStyle(ChatFormatting.GOLD), false);
        src.sendSuccess(() -> line("profile", profile == JvmAdvisor.Profile.SERVER
                ? "dedicated server — throughput-tuned (long pauses are free here)"
                : "client — low-pause, tuned for frame consistency"), false);
        src.sendSuccess(() -> line("this machine", hw.summary()), false);
        src.sendSuccess(() -> line("running with", JvmAdvisor.runningHeapSummary()), false);

        // The measured numbers are the point: this is why YOUR machine gets this answer.
        if (heap.measured()) {
            src.sendSuccess(() -> line("measured live set",
                    String.format("%,d MB (peak %,d MB, after %d min)",
                            heap.liveSetMb(), heap.peakMb(), heap.uptimeMinutes())), false);
        }
        src.sendSuccess(() -> line("recommended heap", heapGb + "G"), false);
        src.sendSuccess(() -> line("based on", rec.basis()), false);
        // CPU capability expressed the only way GC cares about it. Client-only: the server
        // profile deliberately takes long pauses for throughput, so an achievable-pause
        // calculation says nothing useful there.
        if (profile == JvmAdvisor.Profile.CLIENT) {
            src.sendSuccess(() -> line("gc speed", gc.explain(heapGb)), false);
        }
        src.sendSuccess(() -> line("status", tuned
                ? "running the tuned profile"
                : "NOT running the tuned profile — copy the line below"), false);

        if (ramGb > 0 && JvmAdvisor.belowRecommendedRam(ramGb)) {
            src.sendSuccess(() -> Component.literal(
                    "  Warning: this pack's measured peak heap is ~8G. Below 14G of system RAM "
                            + "you will be trading GC pauses against swapping either way.")
                    .withStyle(ChatFormatting.RED), false);
        }

        // Click to copy — the arguments are far too long to retype from chat.
        src.sendSuccess(() -> Component.literal("  [ click to copy ]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, args))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.literal(args)))), false);
        src.sendSuccess(() -> Component.literal(args).withStyle(ChatFormatting.DARK_GRAY), false);

        if (explain && profile == JvmAdvisor.Profile.SERVER) {
            src.sendSuccess(() -> Component.literal("── why the server profile differs ──")
                    .withStyle(ChatFormatting.GOLD), false);
            src.sendSuccess(() -> Component.literal(
                    "  A server has no frame budget — nobody perceives a pause between ticks — so it "
                            + "takes long pauses (" + (heapGb >= 4 ? "160-200ms" : "160ms") + ") in exchange for "
                            + "collecting more efficiently. Aikar's flags were designed for exactly this and "
                            + "are kept in full here, including MaxTenuringThreshold=1 and SurvivorRatio=32.")
                    .withStyle(ChatFormatting.GRAY), false);
            src.sendSuccess(() -> Component.literal(
                    "  Those same two flags are removed on clients, where they force-promote render and "
                            + "entity data into old gen and cost about 10 dropped frames a minute. Same pack, "
                            + "opposite correct answer.").withStyle(ChatFormatting.GRAY), false);
            src.sendSuccess(() -> line("idle-time GC", GcGovernor.status()), false);
        } else if (explain) {
            src.sendSuccess(() -> Component.literal("── why these changed ──").withStyle(ChatFormatting.GOLD), false);
            for (String note : JvmAdvisor.removedFlagNotes()) {
                src.sendSuccess(() -> Component.literal("  • " + note).withStyle(ChatFormatting.GRAY), false);
            }
            src.sendSuccess(() -> line("idle-time GC", GcGovernor.status()), false);
            src.sendSuccess(() -> Component.literal(
                    "  Sizing uses the live set (memory surviving a collection), not peak usage. "
                            + "Peak counts garbage that simply had not been collected yet — sizing for "
                            + "it inflates the heap, and a bigger heap measurably made this pack's "
                            + "pauses worse, not better.").withStyle(ChatFormatting.DARK_GRAY), false);
            src.sendSuccess(() -> Component.literal(
                    "  Note: only SoftMaxHeapSize, G1PeriodicGC* and Min/MaxHeapFreeRatio can be "
                            + "changed while the game runs. Everything above must be set in your "
                            + "launcher before startup.").withStyle(ChatFormatting.DARK_GRAY), false);
        } else {
            src.sendSuccess(() -> Component.literal("  /soa jvm why — what changed and the measurements behind it")
                    .withStyle(ChatFormatting.DARK_GRAY), false);
        }
        return 1;
    }

    /**
     * {@code /soa tune} — the measured picture of this machine and what it argues for.
     *
     * <p>Shows the measurements, not just the conclusions, so a player can see <em>why</em> they
     * were told something and disagree with it. Structural facts (RAM, cores, refresh rate) are
     * separated from measured behaviour (live set, frame times, tick time) because they carry
     * different weight: the structural ones bound what is possible, the measured ones describe
     * what is actually happening.</p>
     */
    /**
     * Submits one tuning report.
     *
     * <p>Public and permission-free by design. These reports are the only way to learn how the
     * pack behaves on hardware nobody here owns, so gating them behind op would mean collecting
     * data from developers only — precisely the sample that does not need studying. It reads
     * this JVM's own counters and cannot change anything, and the anti-cheat handler ignores
     * commands from players below permission level 2, so running it never flags anyone.</p>
     */
    public static int tuneSend(CommandSourceStack src) {
        HeapMeasurement h = HeapMeasurement.capture();
        if (!h.measured()) {
            src.sendFailure(Component.literal(h.tooEarly()
                    ? "Play a few more minutes first — the measurements need about 5 minutes "
                            + "of runtime to mean anything."
                    : "No usable measurements yet; nothing worth sending."));
            return 0;
        }
        src.sendSuccess(() -> Component.literal("Sending tuning report…")
                .withStyle(ChatFormatting.GRAY), false);
        TuningReport.submit(src.getServer(), ok -> src.sendSuccess(() ->
                ok ? Component.literal("Tuning report sent — thank you.")
                        .withStyle(ChatFormatting.GREEN)
                   : Component.literal("Could not send. Telemetry is off, not consented, "
                        + "or the endpoint is unreachable.").withStyle(ChatFormatting.RED), false));
        return 1;
    }

    public static int tune(CommandSourceStack src) {
        HardwareProfile hw = HardwareProfile.detect();
        HeapMeasurement heap = HeapMeasurement.capture();

        src.sendSuccess(() -> Component.literal("── SOA Machine Report ──").withStyle(ChatFormatting.GOLD), false);
        src.sendSuccess(() -> Component.literal("  structural").withStyle(ChatFormatting.DARK_GRAY), false);
        src.sendSuccess(() -> line("RAM", hw.ramUnknown() ? "unknown" : hw.totalRamGb() + "G"), false);
        src.sendSuccess(() -> line("cores", String.valueOf(hw.cores())), false);
        if (hw.gpuRenderer() != null) {
            src.sendSuccess(() -> line("GPU", hw.gpuRenderer() + "  (label only — not used for tuning)"), false);
        }

        src.sendSuccess(() -> Component.literal("  measured").withStyle(ChatFormatting.DARK_GRAY), false);
        if (heap.measured()) {
            src.sendSuccess(() -> line("live set", String.format("%,d MB (peak %,d MB)",
                    heap.liveSetMb(), heap.peakMb())), false);
        } else {
            src.sendSuccess(() -> line("live set", "not yet — needs ~5 min of play"), false);
        }

        // Null when this runs as a client command while connected to a remote server —
        // there is no integrated server to read a tick time from.
        MinecraftServer server = src.getServer();
        if (server != null) {
            src.sendSuccess(() -> line("server tick",
                    String.format("%.2f ms mean", server.getAverageTickTime())), false);
        }

        // Frame data and graphics advice only exist on a client.
        if (FMLEnvironment.dist.isClient()) {
            clientTuneReport(src, hw);
        } else {
            src.sendSuccess(() -> Component.literal(
                    "  Frame timing is client-side; run this in singleplayer for graphics advice.")
                    .withStyle(ChatFormatting.DARK_GRAY), false);
        }
        return 1;
    }

    /** Split out so the client-only classes are never resolved on a dedicated server. */
    private static void clientTuneReport(CommandSourceStack src, HardwareProfile hw) {
        FrameMeasurement.Frames f = FrameMeasurement.capture();
        if (!f.measured()) {
            src.sendSuccess(() -> line("frames", String.format(
                    "%d Hz (%.1f ms budget) — not enough samples yet", f.refreshHz(), f.budgetMs())), false);
            return;
        }
        src.sendSuccess(() -> line("frames", String.format(
                "p50 %.1f ms, p95 %.1f ms vs %.1f ms budget at %d Hz  (%.0f%% of budget)",
                f.p50Ms(), f.p95Ms(), f.budgetMs(), f.refreshHz(), 100.0 / Math.max(f.headroom(), 0.0001))), false);

        var suggestions = SettingsAdvisor.advise(hw, f);
        if (suggestions.isEmpty()) {
            src.sendSuccess(() -> Component.literal("  Nothing to change — settings suit this machine.")
                    .withStyle(ChatFormatting.GREEN), false);
            return;
        }
        src.sendSuccess(() -> Component.literal("  suggestions").withStyle(ChatFormatting.DARK_GRAY), false);
        for (var s : suggestions) {
            src.sendSuccess(() -> Component.literal("  " + s.setting() + ": ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(s.current() + " → " + s.suggested())
                            .withStyle(ChatFormatting.WHITE)), false);
            src.sendSuccess(() -> Component.literal("      " + s.why())
                    .withStyle(ChatFormatting.DARK_GRAY), false);
        }
    }

    private static Component line(String label, String value) {
        return Component.literal("  " + label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }
}
