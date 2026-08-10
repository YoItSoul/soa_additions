package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.mining.MiningLevels;
import com.soul.soa_additions.quest.model.QuestTask;
import com.soul.soa_additions.smithery.tool.SmitheryTools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Hold {@code count} tools whose harvest level is at least {@code level}.
 *
 * <p>This can't be an {@link ItemTask} with an NBT filter. Smithery writes the composition to
 * the stack — {@code smithery:tool_composition}, holding the tool type and one material per
 * slot — but never the harvest level, which it recomputes from the head material on demand.
 * An NBT-subset filter would mean one task per head material that happens to sit at the wanted
 * level, and would silently go stale the first time a material is retuned.</p>
 *
 * <p>It is deliberately its own type rather than a flag on {@link ItemTask}: the matching rule
 * is a stat threshold, not an identity comparison, and keeping it separate means a broken
 * harvest-level quest can never be confused with a broken item quest. It still rides the same
 * {@link com.soul.soa_additions.quest.events.InventoryItemPoller inventory poller} as
 * {@code ItemTask}, so it picks up tools from any source — built, traded, given — and costs
 * nothing at all when no quest in the tree uses one.</p>
 *
 * <p>JSON: {@code { "type": "soa_additions:harvest_level", "level": 6 }}. Optional fields:</p>
 * <ul>
 *   <li>{@code count} — how many qualifying tools to hold. Default 1.</li>
 *   <li>{@code tool_type} — restrict to one family, e.g. {@code "smithery:pickaxe"}.</li>
 *   <li>{@code smithery_only} — default true. Set false to let any tiered tool count, so a
 *       netherite pickaxe satisfies level 4.</li>
 *   <li>{@code mining_only} — default true. Only gear whose level can actually gate a block
 *       counts, which drops swords and hoes exactly as the mining-level tooltip does.</li>
 * </ul>
 */
public record HarvestLevelTask(int level, ResourceLocation toolType, boolean smitheryOnly,
                               boolean miningOnly, int count) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "harvest_level");

    /**
     * The per-stack facts every harvest-level task needs, read once and shared.
     *
     * <p>Harvest level is not stored on the stack — {@code MiningLevels.harvestLevelOf} recomputes
     * Smithery's whole stat block to answer — so asking each task to read each stack itself would
     * cost (tasks × stacks) recomputes per poll. The poller builds one probe per stack instead and
     * matches every task against that.</p>
     */
    public record Probe(int level, ResourceLocation toolType, boolean smitheryGear, boolean mining) {

        /** Shared answer for the overwhelming majority of stacks: not a tool at all. */
        public static final Probe NONE = new Probe(MiningLevels.NONE, null, false, false);

        public static Probe of(ItemStack stack) {
            if (stack.isEmpty()) return NONE;
            boolean gear = SmitheryTools.isGear(stack);
            // Composed gear reads its level straight off the composition rather than through
            // MiningLevels, which answers NONE for anything Smithery says can't gate a block.
            // That gate is what {@code mining_only} controls, so it has to stay one decision
            // made below — not be baked into the level lookup where the flag can't reach it.
            int level = gear ? SmitheryTools.harvestLevel(stack) : MiningLevels.harvestLevelOf(stack);
            if (level == MiningLevels.NONE) return NONE;
            return new Probe(level, gear ? SmitheryTools.toolTypeOf(stack) : null, gear,
                    MiningLevels.showsMiningLevel(stack));
        }
    }

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return count; }

    /** Match against a probe the caller already built. Preferred on the poll path. */
    public boolean matches(Probe probe) {
        if (probe.level() == MiningLevels.NONE) return false;
        if (smitheryOnly && !probe.smitheryGear()) return false;
        if (toolType != null && !toolType.equals(probe.toolType())) return false;
        if (miningOnly && !probe.mining()) return false;
        return probe.level() >= level;
    }

    /** Convenience for one-off checks (commands, tests). Builds a throwaway probe. */
    public boolean matches(ItemStack stack) {
        return matches(Probe.of(stack));
    }

    @Override public String describe() {
        String what = toolType != null
                ? TaskNames.item(toolType.toString())
                : (smitheryOnly ? "Smithery tool" : "tool");
        return "Obtain " + count + "x " + what + " of mining level "
                + MiningLevels.levelName(level).getString();
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("level", level);
        if (count != 1) out.addProperty("count", count);
        if (toolType != null) out.addProperty("tool_type", toolType.toString());
        if (!smitheryOnly) out.addProperty("smithery_only", false);
        if (!miningOnly) out.addProperty("mining_only", false);
    }

    public static HarvestLevelTask fromJson(JsonObject body) {
        if (!body.has("level")) {
            throw new IllegalArgumentException("HarvestLevelTask requires 'level'");
        }
        return new HarvestLevelTask(
                body.get("level").getAsInt(),
                body.has("tool_type") ? new ResourceLocation(body.get("tool_type").getAsString()) : null,
                !body.has("smithery_only") || body.get("smithery_only").getAsBoolean(),
                !body.has("mining_only") || body.get("mining_only").getAsBoolean(),
                body.has("count") ? Math.max(1, body.get("count").getAsInt()) : 1);
    }
}
