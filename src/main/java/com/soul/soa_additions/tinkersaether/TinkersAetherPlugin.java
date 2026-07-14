package com.soul.soa_additions.tinkersaether;

import com.soul.soa_additions.tinkersaether.modifier.TinkersAetherModifiers;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstrap entry point for the Tinkers' Aether port — eleven custom modifier
 * classes ported from {@code tinkersaether-1.4.1}, registered at
 * {@link slimeknights.tconstruct.library.modifiers.ModifierManager.ModifierRegistrationEvent}.
 * Hard-depends on Tinkers' Construct 3.x for the modifier API. Called from
 * {@link com.soul.soa_additions.SoaAdditions} only when {@code tconstruct} is
 * present.
 *
 * <p>Items, blocks, textures, recipes, lang, loot injects, and material stats
 * already live alongside the rest of {@code soa_additions} (JSON datapack
 * resources + ModItems/ModBlocks DeferredRegister entries). This plugin is
 * specifically the Java behavior glue.</p>
 */
public final class TinkersAetherPlugin {

    public static final Logger LOG = LoggerFactory.getLogger("soa_additions/tinkersaether");

    private TinkersAetherPlugin() {}

    public static void init(IEventBus modEventBus) {
        LOG.info("Tinkers' Construct detected — initialising Tinkers' Aether port");
        TinkersAetherModifiers.register(modEventBus);
        LOG.info("Tinkers' Aether modifiers queued");
    }
}
