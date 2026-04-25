package com.soul.soa_additions.tconstructevo;

import com.soul.soa_additions.SoaAdditions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstrap entry point for TConstruct Evolution content — a 1.20.1 port of
 * the spirit of {@code tconevo-1.12.2-1.1.5} by phantamanta44, rewritten to
 * fit TConstruct 3.x's module/hook composition system.
 *
 * <p>Every class under {@code com.soul.soa_additions.tconstructevo} may freely
 * import {@code slimeknights.tconstruct.*} and {@code slimeknights.mantle.*} —
 * those are guaranteed to be on the classpath when {@link #init} runs. Sub-
 * packages under {@code integration/*} are gated individually, see
 * {@link #initIntegrations}.</p>
 */
public final class TConstructEvoPlugin {

    public static final String MODID = "tconevo";
    public static final Logger LOG = LoggerFactory.getLogger("soa_additions/tcon-evo");

    private TConstructEvoPlugin() {}

    public static void init(IEventBus modEventBus) {
        LOG.info("Tinkers' Construct detected — initialising TConstructEvo content");

        TConEvoConfig.register();

        TConEvoItems.register(modEventBus);
        // Touch holder classes so their RegistryObject statics initialise before
        // the DeferredRegister fires its RegisterEvent. Order does not matter
        // among these touches — every register(...) call inside writes into the
        // same DeferredRegister<Item> from TConEvoItems.
        com.soul.soa_additions.tconstructevo.item.TCEMiscItems.bootstrap();
        com.soul.soa_additions.tconstructevo.item.sceptre.TCESceptre.bootstrap();
        TConEvoPotions.register(modEventBus);
        TConEvoEntities.register(modEventBus);
        TConEvoCreativeTab.register(modEventBus);
        TConEvoAttributes.register(modEventBus);

        // GreedyCraft tool + armor traits — marker registrations.
        // Must classload BEFORE TConEvoModifiers.register so the static initializers
        // in the holder classes queue their entries into the deferred register.
        com.soul.soa_additions.tconstructevo.integration.greedy.GreedyToolTraits.bootstrap();
        com.soul.soa_additions.tconstructevo.integration.greedy.GreedyArmorTraits.bootstrap();

        // Modifiers are registered through TConstruct's DeferredRegister on the mod bus.
        TConEvoModifiers.register(modEventBus);

        // Loot modifier serializer for artifact injection.
        com.soul.soa_additions.tconstructevo.item.artifact.ArtifactLootSerializers.register(modEventBus);

        // Forge (game) event bus listeners for runtime behaviour
        MinecraftForge.EVENT_BUS.register(com.soul.soa_additions.tconstructevo.event.TConEvoEventHandler.class);
        MinecraftForge.EVENT_BUS.register(com.soul.soa_additions.tconstructevo.event.UnbreakableBreakHandler.class);

        initIntegrations(modEventBus);

        LOG.info("TConstructEvo core content registered");
    }

    /**
     * Each integration is guarded by a {@link ModList} check so the mod still
     * loads cleanly when an integration target is absent. The integration
     * classes themselves are never classloaded when their mod is absent — they
     * live in sub-packages that are only referenced from inside this method.
     */
    private static void initIntegrations(IEventBus modEventBus) {
        if (ModList.get().isLoaded("draconicevolution")) {
            LOG.info("  + Draconic Evolution");
            com.soul.soa_additions.tconstructevo.integration.draconicevolution.DraconicIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("avaritia")) {
            LOG.info("  + Avaritia");
            com.soul.soa_additions.tconstructevo.integration.avaritia.AvaritiaIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("botania")) {
            LOG.info("  + Botania");
            com.soul.soa_additions.tconstructevo.integration.botania.BotaniaIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("mekanism")) {
            LOG.info("  + Mekanism");
            com.soul.soa_additions.tconstructevo.integration.mekanism.MekanismIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("enderio")) {
            LOG.info("  + EnderIO");
            com.soul.soa_additions.tconstructevo.integration.enderio.EnderIOIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("thermal") || ModList.get().isLoaded("thermal_foundation")) {
            LOG.info("  + Thermal");
            com.soul.soa_additions.tconstructevo.integration.thermal.ThermalIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("solarflux")) {
            LOG.info("  + Solar Flux Reborn");
            com.soul.soa_additions.tconstructevo.integration.solarflux.SolarFluxIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("projecte")) {
            LOG.info("  + ProjectE");
            com.soul.soa_additions.tconstructevo.integration.projecte.ProjectEIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("ae2")) {
            LOG.info("  + Applied Energistics 2");
            com.soul.soa_additions.tconstructevo.integration.appeng.AppEngIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("curios")) {
            LOG.info("  + Curios (Baubles replacement)");
            com.soul.soa_additions.tconstructevo.integration.curios.CuriosIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("bloodmagic")) {
            LOG.info("  + Blood Magic (TConstruct traits)");
            com.soul.soa_additions.tconstructevo.integration.bloodmagic.BloodMagicIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("gamestages")) {
            LOG.info("  + GameStages");
            com.soul.soa_additions.tconstructevo.integration.gamestages.GameStagesIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("tinkers_levelling")) {
            LOG.info("  + Tinkers' Levelling Addon");
            com.soul.soa_additions.tconstructevo.integration.toolleveling.ToolLevelingIntegration.init(modEventBus);
        }
        if (ModList.get().isLoaded("jei")) {
            LOG.info("  + JEI (runtime hooks register via @JeiPlugin)");
            // JEI plugin registers itself via @JeiPlugin annotation — no bootstrap needed.
        }
    }
}
