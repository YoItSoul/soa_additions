package com.soul.soa_additions.optimizer;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Integration with the spark profiler mod. Can be used in two modes:
 *
 * <ul>
 *   <li>{@link #tryStartProfile()} — fire-and-forget 120s profile, no URL back.
 *       Used by the spike-triggered flow in JvmStatsSampler.</li>
 *   <li>{@link #tryStartProfileAndCaptureUrl(int)} — start a profile and return
 *       a {@link CompletableFuture} that completes with the bytebin URL once the
 *       profile finishes and uploads. Used by the telemetry system.</li>
 * </ul>
 */
public final class SparkIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_SparkIntegration");
    private static final String SPARK_COMMAND = "spark profiler --timeout 120";
    private static final Pattern URL_PATTERN = Pattern.compile("https://spark\\.lucko\\.me/[A-Za-z0-9]+");

    private SparkIntegration() {}

    public static boolean isSparkInstalled() {
        return ModList.get().isLoaded("spark");
    }

    /** Fire-and-forget; returns true if the profile was scheduled. */
    public static boolean tryStartProfile() {
        if (!isSparkInstalled()) return false;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        server.execute(() -> {
            try {
                CommandSourceStack source = server.createCommandSourceStack()
                        .withPermission(4)
                        .withSuppressedOutput();
                server.getCommands().performPrefixedCommand(source, SPARK_COMMAND);
                LOGGER.warn("Spark profiler started (120s) via spike trigger");
            } catch (Throwable t) {
                LOGGER.warn("Failed to start spark profiler", t);
            }
        });
        return true;
    }

    /**
     * Starts a spark profile and returns a future that completes with the bytebin URL
     * when the profile finishes (or {@code null} on failure / spark not installed / timeout).
     *
     * <p>Mechanism: creates a custom {@link CommandSource} that captures every feedback
     * message spark writes to it. When a spark.lucko.me URL appears in the buffer, the
     * future completes. A watchdog completes the future with {@code null} if no URL
     * arrives within {@code timeoutSeconds + 30s}.
     */
    public static CompletableFuture<String> tryStartProfileAndCaptureUrl(int timeoutSeconds) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (!isSparkInstalled()) {
            future.complete(null);
            return future;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            future.complete(null);
            return future;
        }

        StringBuilder buffer = new StringBuilder();
        CommandSource capturing = new CommandSource() {
            @Override
            public void sendSystemMessage(Component component) {
                String text;
                try {
                    text = component.getString();
                } catch (Throwable t) {
                    return;
                }
                synchronized (buffer) {
                    buffer.append(text).append('\n');
                }
                Matcher m = URL_PATTERN.matcher(text);
                if (m.find() && !future.isDone()) {
                    future.complete(m.group());
                }
            }
            @Override public boolean acceptsSuccess()      { return true; }
            @Override public boolean acceptsFailure()      { return true; }
            @Override public boolean shouldInformAdmins()  { return false; }
        };

        server.execute(() -> {
            try {
                CommandSourceStack stack = new CommandSourceStack(
                        capturing,
                        Vec3.ZERO,
                        Vec2.ZERO,
                        server.overworld(),
                        4,
                        "SoA-Telemetry",
                        Component.literal("SoA-Telemetry"),
                        server,
                        null
                );
                String cmd = "spark profiler --timeout " + timeoutSeconds;
                server.getCommands().performPrefixedCommand(stack, cmd);
                LOGGER.info("Spark profile started for telemetry ({}s)", timeoutSeconds);
            } catch (Throwable t) {
                LOGGER.warn("Failed to start spark profile for telemetry", t);
                if (!future.isDone()) future.complete(null);
            }
        });

        // Watchdog: give up if no URL arrives within timeout + 30s grace
        CompletableFuture.delayedExecutor(timeoutSeconds + 30L, TimeUnit.SECONDS).execute(() -> {
            if (!future.isDone()) {
                LOGGER.info("Spark profile telemetry capture timed out with no URL");
                future.complete(null);
            }
        });

        return future;
    }
}
