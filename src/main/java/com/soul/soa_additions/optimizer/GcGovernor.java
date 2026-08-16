package com.soul.soa_additions.optimizer;

import com.soul.soa_additions.SoaAdditions;
import com.sun.management.HotSpotDiagnosticMXBean;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Moves G1's concurrent old-generation work onto idle time.
 *
 * <p>This is the only kind of runtime GC tuning HotSpot actually permits. Of the flags that
 * matter for pause behaviour, none are writable after startup — see {@link JvmAdvisor}. The
 * complete set of {@code manageable} flags on Java 17 is {@code G1PeriodicGCInterval},
 * {@code G1PeriodicGCSystemLoadThreshold}, {@code SoftMaxHeapSize}, {@code MinHeapFreeRatio},
 * {@code MaxHeapFreeRatio} and the heap-dump toggles. So this class does one narrow, real
 * thing rather than pretending to be a general tuner.</p>
 *
 * <p><strong>The idea.</strong> {@code G1PeriodicGCInterval} asks G1 to start a concurrent
 * cycle when the VM is otherwise quiet. Left at its default of 0 it never fires, so old-gen
 * marking happens whenever occupancy demands it — which, while you are exploring, is exactly
 * when you can least afford the concurrent cycle's Remark pause (measured mean 30ms). Turning
 * it on only while no world is ticking gets that work done at the main menu instead, then
 * turns it back off so it never competes with gameplay.</p>
 *
 * <h2>Cost of the governor itself</h2>
 *
 * <p>An optimiser that costs frames defeats itself, so this deliberately never touches the
 * render or server thread. It runs on one daemon thread that wakes every
 * {@value #CHECK_SECONDS} seconds, reads a static field, and in the common case does nothing
 * because the idle state has not changed. No tick handler, no per-frame work, no allocation
 * in the steady state. Contrast {@code JvmStatsSampler}, which does real work per sample and
 * is correctly defaulted off for normal play.</p>
 *
 * <p>Everything here is best-effort. The flag is probed for writability before use, and any
 * failure disables the governor permanently rather than retrying — a non-G1 collector or a
 * future JVM should degrade to doing nothing, not to log spam.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GcGovernor {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_GcGovernor");

    /** How long G1 should wait between idle-time concurrent cycles. */
    private static final String IDLE_INTERVAL_MS = "10000";
    private static final String DISABLED = "0";

    private static final String FLAG_INTERVAL = "G1PeriodicGCInterval";

    /** Idle windows are menus and world transitions; sub-second reaction buys nothing. */
    private static final int CHECK_SECONDS = 2;

    private static HotSpotDiagnosticMXBean bean;
    private static volatile boolean unavailable;
    private static volatile boolean idleModeActive;
    private static ScheduledExecutorService executor;

    private GcGovernor() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(GcGovernor::start);
    }

    private static synchronized void start() {
        if (executor != null || unavailable) return;
        if (!ensureBean()) return;

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SOA-GcGovernor");
            t.setDaemon(true);
            // Below normal: this must never contend with the render or server thread.
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        executor.scheduleWithFixedDelay(GcGovernor::poll,
                CHECK_SECONDS, CHECK_SECONDS, TimeUnit.SECONDS);
        LOGGER.info("Idle-time GC scheduling active — concurrent cycles will run while no "
                + "world is ticking.");
    }

    private static void poll() {
        try {
            update(!worldIsTicking());
        } catch (Throwable t) {
            // A scheduled task that throws is silently cancelled forever, so swallow here.
            unavailable = true;
        }
    }

    /**
     * True when a server is running and actually simulating. At the main menu, on the world
     * loading screen, or between worlds there is nothing to stutter, so the concurrent cycle
     * is free.
     *
     * <p>Deliberately not checking for a paused singleplayer world: {@code isPaused} lives on
     * {@code IntegratedServer}, and reaching for a client-only type here would make this class
     * unloadable on a dedicated server. Menus and world transitions are where the long idle
     * windows are anyway.</p>
     */
    private static boolean worldIsTicking() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null && server.isRunning();
    }

    private static void update(boolean idle) {
        if (unavailable || idle == idleModeActive) return;

        String value = idle ? IDLE_INTERVAL_MS : DISABLED;
        try {
            bean.setVMOption(FLAG_INTERVAL, value);
            idleModeActive = idle;
            LOGGER.debug("{} = {} (idle={})", FLAG_INTERVAL, value, idle);
        } catch (Throwable t) {
            unavailable = true;
            LOGGER.info("Idle-time GC scheduling unavailable ({}: {}). This is harmless — it "
                            + "only means old-gen cycles keep their default timing.",
                    t.getClass().getSimpleName(), t.getMessage());
        }
    }

    private static boolean ensureBean() {
        try {
            HotSpotDiagnosticMXBean candidate =
                    ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
            if (candidate == null) {
                unavailable = true;
                return false;
            }
            // Probe writability rather than assuming: a non-G1 collector still exposes the
            // flag but rejects the write, and we want that discovered once, not per poll.
            if (!candidate.getVMOption(FLAG_INTERVAL).isWriteable()) {
                unavailable = true;
                LOGGER.info("{} is not writeable on this JVM — idle-time GC scheduling off.",
                        FLAG_INTERVAL);
                return false;
            }
            bean = candidate;
            return true;
        } catch (Throwable t) {
            unavailable = true;
            return false;
        }
    }

    /** Status string for {@code /soa jvm}. */
    public static String status() {
        if (unavailable) return "unavailable on this JVM";
        if (executor == null) return "starting";
        return idleModeActive
                ? "engaged — G1 running concurrent cycles while the world is not ticking"
                : "standby — world is ticking, periodic GC off";
    }
}
