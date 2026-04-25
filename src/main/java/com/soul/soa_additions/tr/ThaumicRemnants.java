package com.soul.soa_additions.tr;

import com.soul.soa_additions.tr.aura.ChunkAura;
import com.soul.soa_additions.tr.core.Aspects;
import com.soul.soa_additions.tr.data.AspectMapLoader;
import com.soul.soa_additions.tr.knowledge.KnownAspects;
import com.soul.soa_additions.tr.knowledge.ScannedTargets;
import com.soul.soa_additions.tr.network.TrNetworking;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstrap entry point for Thaumic Remnants content. Original mod (rebrand of
 * the earlier Aetherium Arcanum design) — no soft dependency gate, called
 * unconditionally from {@link com.soul.soa_additions.SoaAdditions}.
 *
 * <p>Items, blocks, capabilities, and creative tabs registered through this
 * plugin live in the {@code tr} namespace via per-subsystem DeferredRegisters.
 * The {@code @Mod} container is still {@code soa_additions} — see Taiga for
 * the same convention (per the {@code feedback_mods_toml_single_entry.md}
 * memory note).
 */
public final class ThaumicRemnants {

    public static final String MODID = "tr";
    public static final String DISPLAY_NAME = "Thaumic Remnants";
    public static final Logger LOG = LoggerFactory.getLogger("soa_additions/tr");

    private ThaumicRemnants() {}

    public static void init(IEventBus modEventBus) {
        LOG.info("Initialising Thaumic Remnants content");

        Aspects.bootstrap();

        TrItems.register(modEventBus);
        TrBlocks.register(modEventBus);
        TrBlockEntities.register(modEventBus);
        TrCreativeTab.register(modEventBus);
        KnownAspects.register(modEventBus);
        ScannedTargets.register(modEventBus);
        ChunkAura.register(modEventBus);
        TrNetworking.register();
        AspectMapLoader.register();
        // KnownAspects.Events and ScannedTargets.Events are auto-subscribed
        // to the Forge bus via @Mod.EventBusSubscriber on each Events class —
        // no manual register call needed. The previous manual register
        // pattern silently dropped ScannedTargets' listener (cause never
        // identified); the annotation path is what the rest of the codebase
        // already uses successfully (TrCommands, AspectTooltipHandler, ...).

        LOG.info("Thaumic Remnants content registered ({} aspects, {} items)",
                Aspects.all().size(), TrItems.ASPECT_RUNES.size());
    }
}
