package com.soul.soa_additions.smithery.tool;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.List;
import java.util.Optional;

/**
 * Souls of Avarice's read interface onto Smithery composed gear.
 *
 * <p>This is the public door. Every method is safe to call whether or not Smithery is installed:
 * the {@code ModList.isLoaded} guard lives here, and {@link SmitheryToolReader} — the only class
 * that links against {@code com.soul.smithery.*} — is never touched when the mod is absent.
 * Callers get {@code false}, {@link #NO_LEVEL}, {@link Optional#empty()} or an empty list.</p>
 *
 * <p>Two tiers of access, and picking the right one matters:</p>
 * <ul>
 *   <li>{@link #read} builds a whole {@link ToolView} — every slot, modifier, synergy and stat.
 *       Use it for tooltips, one-off checks, and anywhere you want more than one field. It
 *       allocates, so it does not belong in a per-tick loop over an inventory.</li>
 *   <li>{@link #isGear}, {@link #toolTypeOf}, {@link #harvestLevel} and
 *       {@link #harvestLevelApplies} read a single field with no view allocation. Use these on
 *       hot paths. Even so, compute them <em>once per stack</em> and reuse the answer across
 *       tasks — {@code harvestLevel} recomputes the full stat block internally, since Smithery
 *       never stores it.</li>
 * </ul>
 */
public final class SmitheryTools {

    /**
     * Returned by {@link #harvestLevel} when the stack carries no Smithery harvest level.
     * Deliberately the same sentinel as {@code MiningLevels.NONE}, which aliases this constant
     * so the two can never drift apart.
     */
    public static final int NO_LEVEL = Integer.MIN_VALUE;

    /** How many example stacks {@link #exampleGear} will build before giving up. */
    private static final int EXAMPLE_LIMIT = 24;

    /** Resolved lazily: {@link ModList} isn't populated while classes are still loading. */
    private static Boolean loaded;

    private static String exampleKey;
    private static int exampleGeneration = -1;
    private static List<ItemStack> exampleCache = List.of();

    private SmitheryTools() {}

    public static boolean loaded() {
        Boolean cached = loaded;
        if (cached == null) {
            cached = ModList.get().isLoaded("smithery");
            loaded = cached;
        }
        return cached;
    }

    /** True when the stack is composed Smithery gear — a built tool, weapon or armour piece. */
    public static boolean isGear(ItemStack stack) {
        return loaded() && SmitheryToolReader.isGear(stack);
    }

    /** {@code smithery:pickaxe}, {@code smithery:chestplate}, … or null for anything else. */
    public static ResourceLocation toolTypeOf(ItemStack stack) {
        return loaded() ? SmitheryToolReader.toolTypeOf(stack) : null;
    }

    /**
     * Harvest level of the head material, or {@link #NO_LEVEL}. Recomputed from the composition
     * on every call — Smithery stores the materials, never the level — so cache the result if
     * you need it more than once for the same stack.
     */
    public static int harvestLevel(ItemStack stack) {
        return loaded() ? SmitheryToolReader.harvestLevel(stack, NO_LEVEL) : NO_LEVEL;
    }

    /**
     * Whether this tool family's harvest level can gate a block drop at all. Smithery's own
     * answer — swords carry a level that only feeds damage, and it says so.
     */
    public static boolean harvestLevelApplies(ItemStack stack) {
        return loaded() && SmitheryToolReader.harvestLevelApplies(stack);
    }

    /** The full read. Empty when Smithery is absent or the stack is not composed gear. */
    public static Optional<ToolView> read(ItemStack stack) {
        return loaded() ? Optional.ofNullable(SmitheryToolReader.read(stack)) : Optional.empty();
    }

    /**
     * Buildable example tools that would satisfy "harvest level at least {@code minLevel}" —
     * what the quest book links to so a player can see which materials qualify instead of
     * reading a bare number. Only the head material varies across the returned list; handles
     * and binder are plain wood.
     *
     * @param toolType restrict to one family, or null for the default {@code smithery:pickaxe}
     */
    public static List<ItemStack> exampleGear(int minLevel, ResourceLocation toolType) {
        if (!loaded()) return List.of();
        String key = minLevel + "|" + toolType;
        // Materials are registered at mod init and again on datapack reload, so the material
        // count doubles as a generation counter: a reload that adds or removes one rebuilds
        // the examples, and the steady state is a map lookup.
        int generation = SmitheryToolReader.materialCount();
        if (generation != exampleGeneration || !key.equals(exampleKey)) {
            exampleCache = SmitheryToolReader.exampleGear(minLevel, toolType, EXAMPLE_LIMIT);
            exampleGeneration = generation;
            exampleKey = key;
        }
        return exampleCache;
    }
}
