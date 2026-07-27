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
        BASounds.register(modEventBus);
        BAEffects.register(modEventBus);

        // Stasis tools swap textures with activation state (client only)
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
            modEventBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e) ->
                    e.enqueueWork(BloodArsenalPlugin::registerItemProperties));
        }

        // Register modifiers
        registerModifiers();

        // Forge (game) event bus listeners for runtime behaviour
        MinecraftForge.EVENT_BUS.register(com.soul.soa_additions.bloodarsenal.event.BAEventHandler.class);

        // Imperfect rituals activate on BM's blank Ritual Stone (BM 1.20 has
        // no dedicated imperfect stone block — see ImperfectRitualStoneHandler).
        MinecraftForge.EVENT_BUS.register(com.soul.soa_additions.bloodarsenal.ritual.ImperfectRitualStoneHandler.class);

        LOG.info("Blood Arsenal content registered");
    }

    /** Client: "activated" model predicate for the four stasis tools (1.12 texture pairs). */
    private static void registerItemProperties() {
        net.minecraft.resources.ResourceLocation prop =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("bloodarsenal", "activated");
        net.minecraft.world.item.Item[] tools = {
                BAItems.STASIS_SWORD.get(), BAItems.STASIS_PICKAXE.get(),
                BAItems.STASIS_AXE.get(), BAItems.STASIS_SHOVEL.get()
        };
        for (net.minecraft.world.item.Item tool : tools) {
            net.minecraft.client.renderer.item.ItemProperties.register(tool, prop,
                    (stack, level, entity, seed) ->
                            stack.getItem() instanceof wayoftime.bloodmagic.common.item.IActivatable act
                                    && act.getActivated(stack) ? 1.0f : 0.0f);
        }
    }
}
