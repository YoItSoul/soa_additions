package com.soul.soa_additions.bloodarsenal;

import com.soul.soa_additions.bloodarsenal.modifier.ModifierRegistry;
import com.soul.soa_additions.bloodarsenal.modifier.impl.*;
import com.soul.soa_additions.bloodarsenal.recipe.BARecipeTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstrap entry point for Blood Arsenal content. Called from
 * {@link com.soul.soa_additions.SoaAdditions} only when Blood Magic is present.
 *
 * <p>Every class in this package and its sub-packages may freely import
 * {@code wayoftime.bloodmagic.*} types — they are guaranteed to be on the
 * classpath when this method executes.</p>
 */
public final class BloodArsenalPlugin {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/blood-arsenal");

    private BloodArsenalPlugin() {}

    private static void registerModifiers() {
        // HEAD modifiers
        ModifierRegistry.register(new ModifierSharpness());
        ModifierRegistry.register(new ModifierFlame());
        ModifierRegistry.register(new ModifierBadPotion());
        ModifierRegistry.register(new ModifierBloodlust());
        ModifierRegistry.register(new ModifierCritStriker());
        ModifierRegistry.register(new ModifierVampiric());

        // CORE modifiers
        ModifierRegistry.register(new ModifierLooting());
        ModifierRegistry.register(new ModifierFortunate());
        ModifierRegistry.register(new ModifierSilky());
        ModifierRegistry.register(new ModifierSmelting());
        ModifierRegistry.register(new ModifierXperienced());

        // HANDLE modifiers
        ModifierRegistry.register(new ModifierQuickDraw());
        ModifierRegistry.register(new ModifierShadowTool());
        ModifierRegistry.register(new ModifierBeneficialPotion());

        // ABILITY modifiers
        ModifierRegistry.register(new ModifierAOD());
        ModifierRegistry.register(new ModifierSigil());

        // Incompatibilities
        ModifierRegistry.addIncompatibility("flame", "bad_potion");
        ModifierRegistry.addIncompatibility("fortunate", "silky");
        ModifierRegistry.addIncompatibility("silky", "smelting");
    }

    public static void init(IEventBus modEventBus) {
        LOG.info("Blood Magic detected — initialising Blood Arsenal content");

        BAConfig.register();

        BAItems.register(modEventBus);
        BABlocks.register(modEventBus);
        BABlockEntities.register(modEventBus);
        BACreativeTab.register(modEventBus);
        BAFluids.register(modEventBus);
        BARecipeTypes.register(modEventBus);

        // Register modifiers
        registerModifiers();

        // Forge (game) event bus listeners for runtime behaviour
        MinecraftForge.EVENT_BUS.register(com.soul.soa_additions.bloodarsenal.event.BAEventHandler.class);

        LOG.info("Blood Arsenal content registered");
    }
}
