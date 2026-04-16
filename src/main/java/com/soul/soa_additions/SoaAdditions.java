package com.soul.soa_additions;

import com.soul.soa_additions.anticheat.AntiCheatHandler;
import com.soul.soa_additions.block.ModBlocks;
import com.soul.soa_additions.compat.StartupProfiler;
import com.soul.soa_additions.block.entity.ModBlockEntities;
import com.soul.soa_additions.config.ModConfigs;
import com.soul.soa_additions.item.ModCreativeTabs;
import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.network.ModNetworking;
import com.soul.soa_additions.optimizer.JvmStatsSampler;
import com.soul.soa_additions.registry.SoaTiers;
import com.soul.soa_additions.telemetry.Telemetry;
import com.soul.soa_additions.util.ConfigScanner;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.versions.forge.ForgeVersion;
import net.minecraft.SharedConstants;

@Mod(SoaAdditions.MODID)
public final class SoaAdditions {

    public static final String MODID = "soa_additions";

    public SoaAdditions() {
        StartupProfiler.onConstruct();
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModConfigs.register();
        com.soul.soa_additions.config.QuestBookConfig.register();
        com.soul.soa_additions.config.HeadshotConfig.register();

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        com.soul.soa_additions.donor.ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(StartupProfiler::onCommonSetup);
        modEventBus.addListener(StartupProfiler::onLoadComplete);
        modEventBus.addListener(this::onLoadComplete);
        MinecraftForge.EVENT_BUS.register(this);

        SoaTiers.bootstrap();
        ConfigScanner.startScanning();

        // Blood Arsenal — soft dependency on Blood Magic.
        // All BA classes live in the bloodarsenal subpackage and are never
        // classloaded unless BM is present, so no NoClassDefFoundError.
        if (ModList.get().isLoaded("bloodmagic")) {
            com.soul.soa_additions.bloodarsenal.BloodArsenalPlugin.init(modEventBus);
        }
        // JvmStatsSampler.start() reads config values, so it has to wait
        // until FMLCommonSetupEvent — configs aren't loaded during mod
        // construction and calling .get() here throws in dev (and will
        // throw in prod in a future Forge version).
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetworking.register();
            AntiCheatHandler.scanServerInstalledMods();
            JvmStatsSampler.start();
        });
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        // Fire one telemetry report per launch, async, daemon thread. No startup cost.
        String mcVersion;
        try {
            mcVersion = SharedConstants.getCurrentVersion().getName();
        } catch (Throwable t) {
            mcVersion = "unknown";
        }
        String forgeVersion;
        try {
            forgeVersion = ForgeVersion.getVersion();
        } catch (Throwable t) {
            forgeVersion = "unknown";
        }
        Telemetry.sendAsync(mcVersion, forgeVersion);
    }

    @SubscribeEvent
    public void onServerStarted(final ServerStartedEvent event) {
        // Fire once per launch. Starts a spark profile and, when it finishes,
        // re-POSTs the telemetry with the spark.lucko.me URL attached. The
        // server upserts on install_id so this overwrites the initial row.
        Telemetry.sendSparkUpdateAsync();
        // Only start the server-side heartbeat on a true dedicated server.
        // Integrated servers (singleplayer, LAN-opened worlds) are tracked as
        // players via the client heartbeat — we don't want those reporting as
        // servers or duplicating beats.
        if (event.getServer().isDedicatedServer()) {
            Telemetry.startHeartbeat();
        }
    }

    @SubscribeEvent
    public void onServerStopping(final ServerStoppingEvent event) {
        // Only dedicated servers started a server-side heartbeat; mirror that here.
        if (event.getServer().isDedicatedServer()) {
            Telemetry.stopHeartbeat();
        }
    }
}
