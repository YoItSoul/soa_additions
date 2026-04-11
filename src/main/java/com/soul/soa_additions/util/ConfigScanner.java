package com.soul.soa_additions.util;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Watches the config directory to find config files that are never touched by any mod during a session
 * and dumps them to {@code config/unused_configs.txt} on first player login.
 */
public final class ConfigScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_ConfigDump");
    private static final Set<String> KNOWN_CONFIGS = new HashSet<>();
    private static final AtomicBoolean HAS_COMPLETED_SCAN = new AtomicBoolean(false);
    private static final AtomicBoolean IS_WATCHER_RUNNING = new AtomicBoolean(true);
    private static final List<String> UNUSED_CONFIGS = new ArrayList<>();

    private static WatchService watchService;
    private static Thread watchThread;

    private ConfigScanner() {}

    public static void startScanning() {
        LOGGER.info("Starting config scan process");
        MinecraftForge.EVENT_BUS.register(ConfigScanner.class);
        collectKnownConfigs();
        watchForConfigAccess(FMLPaths.CONFIGDIR.get());
        LOGGER.info("Initial scan setup complete");
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (HAS_COMPLETED_SCAN.compareAndSet(false, true)) {
            LOGGER.info("Player joined world, finalizing scan");
            finalizeScan();
            MinecraftForge.EVENT_BUS.unregister(ConfigScanner.class);
        }
    }

    private static void collectKnownConfigs() {
        KNOWN_CONFIGS.clear();
        int count = 0;
        for (IModInfo mod : LoadingModList.get().getMods()) {
            String modId = mod.getModId();
            KNOWN_CONFIGS.add(modId + ".toml");
            KNOWN_CONFIGS.add(modId + ".json");
            KNOWN_CONFIGS.add(modId + ".cfg");
            KNOWN_CONFIGS.add("config-" + modId + ".toml");
            KNOWN_CONFIGS.add(modId + "-common.toml");
            KNOWN_CONFIGS.add(modId + "-client.toml");
            KNOWN_CONFIGS.add(modId + "-server.toml");
            count++;
        }
        LOGGER.info("Collected {} potential config patterns", count);
    }

    private static void watchForConfigAccess(Path directory) {
        LOGGER.info("Setting up config access watcher for: {}", directory);
        try {
            watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            watchThread = new Thread(ConfigScanner::runWatchLoop, "SOA-ConfigScannerWatcher");
            watchThread.setDaemon(true);
            watchThread.start();
        } catch (IOException e) {
            LOGGER.error("Failed to setup config watcher", e);
        }
    }

    private static void runWatchLoop() {
        try {
            while (IS_WATCHER_RUNNING.get()) {
                WatchKey key;
                try {
                    key = watchService.poll(1L, TimeUnit.SECONDS);
                } catch (ClosedWatchServiceException e) {
                    break;
                }
                if (key == null) continue;
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path file = (Path) event.context();
                    if (file != null) {
                        KNOWN_CONFIGS.add(file.getFileName().toString());
                    }
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Watch thread stopped");
    }

    private static void finalizeScan() {
        try {
            IS_WATCHER_RUNNING.set(false);
            if (watchThread != null && watchThread.isAlive()) {
                try {
                    watchThread.join(5000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (watchService != null) {
                watchService.close();
            }
            scanForUnusedConfigs(FMLPaths.CONFIGDIR.get());
            writeUnusedConfigsList();
        } catch (IOException e) {
            LOGGER.error("Error during scan finalization", e);
        }
    }

    private static void scanForUnusedConfigs(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(Files::isRegularFile)
                    .filter(ConfigScanner::isConfigFile)
                    .filter(p -> !isConfigUsed(p))
                    .forEach(p -> UNUSED_CONFIGS.add(p.getFileName().toString()));
        }
        LOGGER.info("Found {} unused configs", UNUSED_CONFIGS.size());
    }

    private static void writeUnusedConfigsList() throws IOException {
        Path dumpFile = FMLPaths.CONFIGDIR.get().resolve("unused_configs.txt");
        Files.write(dumpFile, UNUSED_CONFIGS, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        LOGGER.info("Written unused configs list to: {}", dumpFile);
    }

    private static boolean isConfigFile(Path file) {
        String filename = file.getFileName().toString().toLowerCase();
        return filename.endsWith(".toml") || filename.endsWith(".json") || filename.endsWith(".cfg");
    }

    private static boolean isConfigUsed(Path file) {
        try {
            return KNOWN_CONFIGS.contains(file.getFileName().toString())
                    || Files.getLastModifiedTime(file).toMillis() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1L);
        } catch (IOException e) {
            LOGGER.error("Error checking config usage: {}", file.getFileName(), e);
            return true;
        }
    }
}
