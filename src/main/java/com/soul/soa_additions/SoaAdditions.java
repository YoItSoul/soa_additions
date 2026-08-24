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
import com.soul.soa_additions.worldgen.ModFeatures;
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
        com.soul.soa_additions.config.LiteModeConfig.register();
        com.soul.soa_additions.config.CyclicFisherConfig.register();

        ModBlocks.register(modEventBus);
        com.soul.soa_additions.taiga.TaigaBlocks.register(modEventBus);
        com.soul.soa_additions.taiga.TaigaItems.register(modEventBus);
        com.soul.soa_additions.nyx.NyxItems.register(modEventBus);
        com.soul.soa_additions.nyx.NyxBlocks.register(modEventBus);
        com.soul.soa_additions.nyx.NyxEnchantments.register(modEventBus);
        com.soul.soa_additions.nyx.NyxEntities.register(modEventBus);
        com.soul.soa_additions.nyx.NyxSounds.register(modEventBus);
        com.soul.soa_additions.sound.ModSounds.register(modEventBus);
        com.soul.soa_additions.nyx.NyxConfig.register();
        ModFeatures.register(modEventBus);
        com.soul.soa_additions.worldgen.ModPoi.register(modEventBus);
        // Forced top-priority datapack shadowing other mods' data files
        // (ScalingHealth difficulty, ProgressiveBosses stats, Terralith toast).
        modEventBus.addListener(com.soul.soa_additions.datapack.SoaOverridesPack::onAddPackFinders);
        // Curios soft-dep: queue GreedyBag onto ModItems.ITEMS before the
        // DeferredRegister fires. CuriosIntegration never gets class-loaded
        // when Curios is absent, so GreedyBagItem (implements ICurio) stays
        // unlinked and Forge doesn't hit NoClassDefFoundError.
        if (ModList.get().isLoaded("curios")) {
            com.soul.soa_additions.curios.CuriosIntegration.init(modEventBus);
        }
        com.soul.soa_additions.item.TConEvoBlocks.register(modEventBus);
        com.soul.soa_additions.item.TConEvoItems.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // SoA Potions — port of GreedyCraft's PotionCore brews to native MobEffects.
        // Brewing recipes are registered natively via SoaBrewing.registerBrewing()
        // from commonSetup below (Forge 1.20.1 lacks RegisterBrewingRecipesEvent
        // and KubeJS 6 dropped its brewing recipe schema, so the legacy
        // kubejs/server_scripts/recipes/ported_gc/vanilla_brewing.js is a no-op).
        com.soul.soa_additions.potion.SoaPotions.register(modEventBus);
        com.soul.soa_additions.potion.SoaBrewingPotions.register(modEventBus);
        com.soul.soa_additions.potion.TconEvoEffects.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(com.soul.soa_additions.potion.SoaPotionEvents.class);
        // GC tick rules (motion clamp, boss y-cap, effect hygiene, portal
        // gates) — ported from KubeJS tick handlers in 3.58.3, see SoaTickRules.
        MinecraftForge.EVENT_BUS.register(com.soul.soa_additions.event.SoaTickRules.class);
        // Harvest Moon luck boon — grants the vanilla Luck attribute for the
        // night, which is where hand-cast fishing reads player luck from.
        MinecraftForge.EVENT_BUS.register(com.soul.soa_additions.nyx.event.HarvestMoonLuck.class);
        // GC endgame admin commands (/executor, /infinitykill) — see GcParityCommands.
        MinecraftForge.EVENT_BUS.register(com.soul.soa_additions.command.GcParityCommands.class);
        com.soul.soa_additions.loot.LootModifierSerializers.register(modEventBus);
        com.soul.soa_additions.loot.artifact.TconevoArtifacts.register(modEventBus);
        com.soul.soa_additions.item.SoaRecipeSerializers.register(modEventBus);
        com.soul.soa_additions.loot.LootConditions.register(modEventBus);
        com.soul.soa_additions.donor.ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(StartupProfiler::onCommonSetup);
        modEventBus.addListener(StartupProfiler::onLoadComplete);
        modEventBus.addListener(this::onLoadComplete);
        MinecraftForge.EVENT_BUS.register(this);

        SoaTiers.bootstrap();
        com.soul.soa_additions.nyx.NyxMaterials.bootstrap();
        ConfigScanner.startScanning();

        // Blood Arsenal — soft dependency on Blood Magic.
        // All BA classes live in the bloodarsenal subpackage and are never
        // classloaded unless BM is present, so no NoClassDefFoundError.
        if (ModList.get().isLoaded("bloodmagic")) {
            com.soul.soa_additions.bloodarsenal.BloodArsenalPlugin.init(modEventBus);
        }

        // InsaneLib bridge — port of GC HungerTweaker's onExhausted RNG (Tip
        // 52: low-food drain skip). Subscribes to InsaneLib's PlayerExhaustion
        // Event only when the mod is loaded.
        if (ModList.get().isLoaded("insanelib")) {
            com.soul.soa_additions.quest.InsaneLibHungerBridge.init();
        }

        // Reskillable traits — port of GC compatskills/traits.zs (9 custom
        // traits: bloodlust/fortified/experience_grinder/turbo_miner/
        // essence_reaper/magic_brew/strip_miner/building_master/
        // devourer_of_souls). Each gated on per-skill level checks via
        // SkillCapability. Class never loads when Reskillable is absent.
        if (ModList.get().isLoaded("reskillable")) {
            com.soul.soa_additions.reskillable.ReskillableTraits.init();
            // Auto-classifier for items NOT in skill_locks.json (Tinker
            // tools + any unlisted modded gear) is disabled for now — the
            // armor curve mis-classified vanilla iron gear and the broader
            // gating needs more calibration before re-enabling.
            // com.soul.soa_additions.reskillable.ToolSkillAutoLock.init();
        }

        if (ModList.get().isLoaded("smithery")) {
            com.soul.soa_additions.smithery.SmitheryIntegration.init(modEventBus);
        }

        // Client-only: extract the bundled SoA Radiance shaderpack. Was dead
        // code until 3.58.2 — install() existed but nothing ever called it.
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
            ShaderpackInstaller.install();
        }

        // Thaumic Remnants — removed (dead feature).
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
            com.soul.soa_additions.registry.HardnessOverrides.apply();
            com.soul.soa_additions.potion.SoaBrewing.registerBrewing();
        });
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        // Cyclic Fishing Net x Aquaculture. Deferred to load-complete because it
        // reports on mixin application, which is only knowable once every mod is
        // constructed and the transformer has run. Logs what it found either way.
        com.soul.soa_additions.cyclicaqua.CyclicAquaFisher.detect();

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
