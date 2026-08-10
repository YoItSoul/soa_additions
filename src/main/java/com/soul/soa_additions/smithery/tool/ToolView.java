package com.soul.soa_additions.smithery.tool;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Everything Souls of Avarice can read off a Smithery composed tool, flattened into plain data.
 *
 * <p>Nothing in this file mentions a Smithery type. That is the whole point: Smithery is an
 * optional dependency ({@code mandatory=false} in mods.toml), so any class that touches
 * {@code com.soul.smithery.*} can only be loaded behind a {@code ModList.isLoaded} check.
 * A view is produced once by {@link SmitheryTools#read} — which does live behind that check —
 * and from then on it is ordinary data that quest tasks, tooltips, loot code and the quest book
 * can pass around freely without any guard at all.</p>
 *
 * <p>Some of this is stored on the stack and some is computed. Stored: the composition
 * ({@code smithery:tool_composition} — tool type, one material per slot, optional embossment),
 * the applied modifiers, their progress counters, extra attributes, and max durability. Computed
 * fresh from the composition on every read: everything in {@link Stats}, the active synergies,
 * and the modifier slot counts. Harvest level in particular is <em>never</em> written to NBT —
 * it is the harvest level of the material in the first additive (head) slot — which is why an
 * NBT-subset filter can't express "a tool of level 6" and this interface exists.</p>
 */
public record ToolView(
        ResourceLocation toolType,
        List<Part> parts,
        // Embossed material, or null when the tool carries no embossment.
        ResourceLocation embossment,
        Stats stats,
        List<AppliedModifier> modifiers,
        Map<ResourceLocation, Integer> modifierProgress,
        List<ResourceLocation> synergies,
        List<ExtraAttribute> extraAttributes,
        int maxDurability,
        int damage,
        int modifierSlotsTotal,
        int modifierSlotsUsed,
        int modifierSlotsFree,
        boolean harvestLevelApplies) {

    /**
     * One composition slot. {@code additive} mirrors Smithery's {@code DurabilityRole}: additive
     * parts (heads, blades) contribute durability and carry the tool's harvest level; multiplier
     * parts (handles, bindings) scale it.
     */
    public record Part(ResourceLocation partType, ResourceLocation material, boolean additive) {}

    /**
     * A modifier applied to the tool, with the raw parameter map Smithery stored alongside it.
     * Values are boxed numbers or booleans; use {@link #paramFloat} rather than casting.
     */
    public record AppliedModifier(ResourceLocation id, Map<String, Object> params) {

        public float paramFloat(String key, float fallback) {
            Object v = params.get(key);
            return v instanceof Number n ? n.floatValue() : fallback;
        }

        public int paramInt(String key, int fallback) {
            Object v = params.get(key);
            return v instanceof Number n ? n.intValue() : fallback;
        }

        public boolean paramBool(String key, boolean fallback) {
            Object v = params.get(key);
            return v instanceof Boolean b ? b : fallback;
        }
    }

    /** An attribute modifier Smithery wrote onto the stack outside the normal stat pipeline. */
    public record ExtraAttribute(String name, ResourceLocation attribute, double amount,
                                 String operation, String slot) {}

    /**
     * The computed stat block. Melee fields are meaningful on every tool; {@code armorDefense}
     * and {@code armorToughness} only on armour; {@code drawSpeed}/{@code range}/
     * {@code bonusDamage}/{@code accuracy}/{@code ammoCount} only on bows and crossbows. Fields
     * that don't apply to a given tool type come back zero rather than absent.
     */
    public record Stats(int harvestLevel, float attackDamage, float miningSpeed,
                        float armorDefense, float armorToughness,
                        float drawSpeed, float range, float bonusDamage, float accuracy,
                        int ammoCount, float passiveBonusDamage) {}

    // ------------------------------------------------------------------ convenience

    /** Harvest level of the head material. Shorthand for {@code stats().harvestLevel()}. */
    public int harvestLevel() {
        return stats.harvestLevel();
    }

    /**
     * Material in the first additive slot — the "head", and the only slot that decides harvest
     * level. Empty for a tool type with no additive slot at all.
     */
    public Optional<ResourceLocation> head() {
        for (Part p : parts) {
            if (p.additive()) return Optional.ofNullable(p.material());
        }
        return Optional.empty();
    }

    /** Material in the named part slot ({@code smithery:handle}, {@code smithery:binding}, …). */
    public Optional<ResourceLocation> materialIn(ResourceLocation partType) {
        for (Part p : parts) {
            if (p.partType().equals(partType)) return Optional.ofNullable(p.material());
        }
        return Optional.empty();
    }

    /** Every slot material in slot order, embossment excluded. May contain duplicates. */
    public List<ResourceLocation> materials() {
        return parts.stream().map(Part::material).filter(java.util.Objects::nonNull).toList();
    }

    /** True when the material appears in any slot, or as the embossment. */
    public boolean hasMaterial(ResourceLocation material) {
        if (material.equals(embossment)) return true;
        for (Part p : parts) {
            if (material.equals(p.material())) return true;
        }
        return false;
    }

    public boolean hasModifier(ResourceLocation modifier) {
        for (AppliedModifier m : modifiers) {
            if (m.id().equals(modifier)) return true;
        }
        return false;
    }

    public Optional<AppliedModifier> modifier(ResourceLocation modifier) {
        for (AppliedModifier m : modifiers) {
            if (m.id().equals(modifier)) return Optional.of(m);
        }
        return Optional.empty();
    }

    /** Progress toward the next level of a levelling modifier, or 0 if it has none recorded. */
    public int modifierProgress(ResourceLocation modifier) {
        return modifierProgress.getOrDefault(modifier, 0);
    }

    public boolean hasSynergy(ResourceLocation synergy) {
        return synergies.contains(synergy);
    }

    /** Remaining durability, i.e. {@code maxDurability - damage}. */
    public int durabilityLeft() {
        return Math.max(0, maxDurability - damage);
    }
}
