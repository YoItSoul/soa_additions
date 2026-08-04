package com.soul.soa_additions.cyclicaqua;

import com.mojang.logging.LogUtils;
import com.soul.soa_additions.config.CyclicFisherConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

/**
 * Gate and reporting front for the Cyclic Fishing Net &times; Aquaculture
 * integration.
 *
 * <p>This is the only class in the package the rest of the mod touches, and the
 * only one that is safe to load unconditionally: it names Cyclic and
 * Aquaculture types nowhere, so it links cleanly with either mod (or both)
 * absent. {@link NetFishing} and {@link WitherCatch}, which do name them, are
 * reached only through the calls below and only once {@link #isActive()} has
 * confirmed both mods are really there.</p>
 *
 * <p>"Really there" means three separate checks, all of which must pass:
 * both mod ids are loaded, both of the concrete classes the integration binds
 * against resolve, and the mixin actually landed on {@code TileFisher}. The
 * last one is observable because the mixin implants
 * {@link SoaFisherPatched} — a target that does not implement it was not
 * patched, whatever the mod list says. All three are printed at startup.</p>
 */
public final class CyclicAquaFisher {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREFIX = "[SoA Fishing Net]";

    private static final String CYCLIC_ID = "cyclic";
    private static final String AQUACULTURE_ID = "aquaculture";
    private static final String TILE_FISHER = "com.lothrazar.cyclic.block.fishing.TileFisher";
    private static final String AQUA_ROD = "com.teammetallurgy.aquaculture.item.AquaFishingRodItem";

    private static boolean active;
    private static boolean loggedFirstCatch;

    private CyclicAquaFisher() {}

    /**
     * Runs once at {@code FMLLoadComplete}, by which point every mod is
     * constructed and every mixin has been applied. Reports what was found and
     * decides whether the integration is live.
     */
    public static void detect() {
        boolean cyclic = ModList.get().isLoaded(CYCLIC_ID);
        boolean aquaculture = ModList.get().isLoaded(AQUACULTURE_ID);
        LOGGER.info("{} Cyclic: {}", PREFIX, cyclic ? "DETECTED " + version(CYCLIC_ID) : "not installed");
        LOGGER.info("{} Aquaculture: {}", PREFIX, aquaculture ? "DETECTED " + version(AQUACULTURE_ID) : "not installed");

        if (!cyclic || !aquaculture) {
            LOGGER.info("{} integration: INACTIVE (needs both mods) - Fishing Net keeps Cyclic's stock behaviour",
                    PREFIX);
            return;
        }

        Class<?> tileFisher = resolve(TILE_FISHER);
        boolean aquaRod = resolve(AQUA_ROD) != null;
        LOGGER.info("{} Cyclic TileFisher class: {}", PREFIX, tileFisher != null ? "FOUND" : "MISSING");
        LOGGER.info("{} Aquaculture AquaFishingRodItem class: {}", PREFIX, aquaRod ? "FOUND" : "MISSING");

        if (tileFisher == null || !aquaRod) {
            LOGGER.warn("{} integration: FAILED - both mods are loaded but the expected classes are not "
                    + "where they should be. A version change is the likely cause; the Fishing Net is "
                    + "untouched and everything else is unaffected.", PREFIX);
            return;
        }

        boolean patched = SoaFisherPatched.class.isAssignableFrom(tileFisher);
        LOGGER.info("{} TileFisher mixin applied: {}", PREFIX, patched ? "YES" : "NO");
        if (!patched) {
            LOGGER.warn("{} integration: FAILED - the mixin did not apply to {}. Check the log above for "
                    + "mixin errors. The Fishing Net is untouched.", PREFIX, TILE_FISHER);
            return;
        }

        active = true;
        MinecraftForge.EVENT_BUS.register(WitherCatch.class);
        LOGGER.info("{} integration: ACTIVE - the net now casts with hooks, bait, lure, luck and "
                + "open-water loot, and Mending no longer makes rods immortal. Tunables live in "
                + "config/soa_additions-cyclic_fisher.toml.", PREFIX);
    }

    /** True once startup has verified both mods and a landed mixin. */
    public static boolean isActive() {
        return active;
    }

    /**
     * Guard for the mixin's hot path: active, and not switched off in config.
     * The config read is defensive because a block could conceivably tick before
     * the config file has been bound.
     */
    public static boolean shouldReplace() {
        if (!active) {
            return false;
        }
        try {
            return CyclicFisherConfig.ENABLED.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    /** Entry point from the mixin. Delegates to the half that knows both mods. */
    public static void fish(BlockEntity fisher, ItemStack rod, BlockPos center) {
        Level level = fisher.getLevel();
        if (level != null && !level.isClientSide()) {
            logFirstCatch(fisher, center);
        }
        NetFishing.fish(fisher, rod, center);
    }

    /**
     * Lets a lava-capable hook fish lava, by widening Cyclic's water-only test.
     * Whether the rod's hook can actually handle that fluid is re-checked inside
     * {@link NetFishing}, so a false positive here just costs one no-op call.
     */
    public static boolean isFishableFluid(Level level, BlockPos pos) {
        if (!shouldReplace()) {
            return false;
        }
        try {
            return CyclicFisherConfig.ALLOW_HOOK_FLUIDS.get()
                    && level.getFluidState(pos).is(net.minecraft.tags.FluidTags.LAVA);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /** One line, the first time a net actually fishes, as proof the path is live. */
    private static void logFirstCatch(BlockEntity fisher, BlockPos center) {
        if (loggedFirstCatch) {
            return;
        }
        loggedFirstCatch = true;
        Level level = fisher.getLevel();
        LOGGER.info("{} first live catch attempt: net at {} in {}, fishing {}. Integration confirmed working.",
                PREFIX,
                fisher.getBlockPos().toShortString(),
                level == null ? "unknown" : level.dimension().location(),
                center.toShortString());
    }

    private static String version(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("unknown version");
    }

    private static Class<?> resolve(String name) {
        try {
            return Class.forName(name, false, CyclicAquaFisher.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }
}
