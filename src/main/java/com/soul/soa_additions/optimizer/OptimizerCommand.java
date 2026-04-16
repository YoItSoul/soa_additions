package com.soul.soa_additions.optimizer;

import com.mojang.brigadier.CommandDispatcher;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.compat.CompatScanner;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
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
                })));
    }

    private static Component line(String label, String value) {
        return Component.literal("  " + label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }
}
