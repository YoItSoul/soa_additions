package com.soul.soa_additions.mining;

import com.soul.smithery.api.tool.ToolType;
import com.soul.smithery.item.tool.SmitheryToolData;
import com.soul.smithery.item.tool.SmitheryToolItem;
import com.soul.smithery.item.tool.ToolComposition;
import com.soul.smithery.item.tool.ToolStats;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Reads the mining level off a Smithery composed tool.
 *
 * <p>Smithery tools extend plain {@code Item}, not {@code TieredItem} — the harvest level lives
 * in the composition's computed {@link ToolStats}, not in a {@code Tier} — so
 * {@link MiningLevels} can't get at it the vanilla way. Every class named here belongs to
 * Smithery, which is an optional dependency, so this class is only ever touched behind a
 * {@code ModList.isLoaded("smithery")} check in {@link MiningLevels}.</p>
 *
 * <p>Which families have a meaningful level is Smithery's call, answered by
 * {@link SmitheryToolItem#harvestLevelApplies}. This used to be a hand-copied list of sword-family
 * tool types mirroring Smithery's private {@code usesHarvestTier}, which would have drifted
 * silently the first time Smithery changed that list. Requires Smithery 1.9.8+, declared as a
 * version floor in mods.toml.</p>
 */
final class SmitheryToolLevels {

    /**
     * Families whose level is real but can never decide anything <em>in this pack</em>: no
     * hoe-mineable block here is tier-gated. Unlike {@link SmitheryToolItem#harvestLevelApplies},
     * which describes a Smithery mechanic, this is a Souls of Avarice display judgement about
     * Souls of Avarice's blocks — so it stays on this side of the boundary.
     */
    private static final Set<String> NOT_WORTH_SHOWING = Set.of("hoe");

    private SmitheryToolLevels() {}

    /** Whether the SOA tooltip should carry a mining level for this tool. */
    static boolean showsMiningLevel(ItemStack stack) {
        if (!isMiningTool(stack)) return false;
        ToolType type = ((SmitheryToolItem) stack.getItem()).toolType();
        return !NOT_WORTH_SHOWING.contains(type.id().getPath());
    }

    static int harvestLevel(ItemStack stack) {
        if (!isMiningTool(stack)) return MiningLevels.NONE;
        ToolComposition comp = SmitheryToolData.getComposition(stack);
        if (comp == null) return MiningLevels.NONE;
        return ToolStats.compute(comp, SmitheryToolData.getAppliedModifiers(stack)).harvestLevel;
    }

    /** True when this stack is a Smithery tool whose harvest level can gate a drop. */
    static boolean isMiningTool(ItemStack stack) {
        if (!(stack.getItem() instanceof SmitheryToolItem tool)) return false;
        return SmitheryToolItem.harvestLevelApplies(tool.toolType());
    }
}
