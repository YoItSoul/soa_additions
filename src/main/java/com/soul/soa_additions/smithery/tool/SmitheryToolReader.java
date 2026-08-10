package com.soul.soa_additions.smithery.tool;

import com.soul.smithery.api.SmitheryAPI;
import com.soul.smithery.api.material.Material;
import com.soul.smithery.api.modifier.ModifierEffect;
import com.soul.smithery.api.part.PartEligibility;
import com.soul.smithery.api.synergy.SynergyDefinition;
import com.soul.smithery.api.tool.DurabilityRole;
import com.soul.smithery.api.tool.ToolType;
import com.soul.smithery.item.tool.SmitheryArmorItem;
import com.soul.smithery.item.tool.SmitheryToolData;
import com.soul.smithery.item.tool.SmitheryToolItem;
import com.soul.smithery.item.tool.ToolComposition;
import com.soul.smithery.item.tool.ToolCompositions;
import com.soul.smithery.item.tool.ToolStats;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The one class in Souls of Avarice that reads Smithery's own types. Everything it returns is
 * plain data ({@link ToolView}, {@link ItemStack}, primitives), so callers never link against
 * Smithery and never need a presence check of their own.
 *
 * <p>Package-private on purpose — {@link SmitheryTools} is the public door, and it is the thing
 * that holds the {@code ModList.isLoaded("smithery")} guard. Loading this class when Smithery
 * is absent throws {@link NoClassDefFoundError}, exactly as {@code SmitheryToolLevels} in the
 * mining package already documents.</p>
 */
final class SmitheryToolReader {

    private SmitheryToolReader() {}

    // ------------------------------------------------------------------ cheap single-field reads

    /** True when the stack carries a Smithery composition, whatever item class it is. */
    static boolean isGear(ItemStack stack) {
        return !stack.isEmpty() && SmitheryToolData.hasComposition(stack);
    }

    static ResourceLocation toolTypeOf(ItemStack stack) {
        ToolComposition comp = compositionOf(stack);
        return comp == null ? null : comp.toolTypeId();
    }

    /**
     * Harvest level without building a whole {@link ToolView}. This runs per inventory slot per
     * quest poll and per tooltip frame, so it stays allocation-light.
     */
    static int harvestLevel(ItemStack stack, int absent) {
        ToolComposition comp = compositionOf(stack);
        if (comp == null) return absent;
        return ToolStats.compute(comp, SmitheryToolData.getAppliedModifiers(stack)).harvestLevel;
    }

    /** Whether Smithery considers this tool family's harvest level able to gate a block drop. */
    static boolean harvestLevelApplies(ItemStack stack) {
        ToolComposition comp = compositionOf(stack);
        if (comp == null) return false;
        return SmitheryToolItem.harvestLevelApplies(comp.toolType());
    }

    // ------------------------------------------------------------------ full read

    /** Null when the stack is not composed Smithery gear. */
    static ToolView read(ItemStack stack) {
        ToolComposition comp = compositionOf(stack);
        if (comp == null) return null;

        List<ModifierEffect> applied = SmitheryToolData.getAppliedModifiers(stack);
        ToolStats stats = ToolStats.compute(comp, applied);
        ToolType type = comp.toolType();

        List<ToolView.Part> parts = new ArrayList<>();
        List<ResourceLocation> slotMaterials = comp.slotMaterials();
        if (type != null) {
            List<ToolType.Slot> slots = type.slots();
            for (int i = 0; i < slots.size(); i++) {
                ToolType.Slot slot = slots.get(i);
                parts.add(new ToolView.Part(
                        slot.partType().id(),
                        i < slotMaterials.size() ? slotMaterials.get(i) : null,
                        slot.role() == DurabilityRole.ADDITIVE));
            }
        }

        List<ToolView.AppliedModifier> modifiers = new ArrayList<>(applied.size());
        for (ModifierEffect effect : applied) {
            modifiers.add(new ToolView.AppliedModifier(
                    effect.modifierId(), Map.copyOf(effect.params())));
        }

        List<ResourceLocation> synergies = new ArrayList<>();
        for (SynergyDefinition synergy : stats.activeSynergies) {
            synergies.add(synergy.id());
        }

        List<ToolView.ExtraAttribute> extras = new ArrayList<>();
        for (SmitheryToolData.ExtraAttribute extra : SmitheryToolData.getExtraAttributes(stack)) {
            extras.add(new ToolView.ExtraAttribute(
                    extra.name(), extra.attributeId(), extra.amount(),
                    extra.operation().name(), extra.slot().getName()));
        }

        Map<ResourceLocation, Integer> progress =
                new LinkedHashMap<>(SmitheryToolData.getModifierProgress(stack));

        return new ToolView(
                comp.toolTypeId(),
                List.copyOf(parts),
                comp.embossedMaterial().orElse(null),
                new ToolView.Stats(
                        stats.harvestLevel, stats.attackDamage, stats.miningSpeed,
                        stats.armorDefense, stats.armorToughness,
                        stats.drawSpeed, stats.range, stats.bonusDamage, stats.accuracy,
                        stats.ammoCount, stats.passiveBonusDamage),
                List.copyOf(modifiers),
                Map.copyOf(progress),
                List.copyOf(synergies),
                List.copyOf(extras),
                SmitheryToolData.getMaxDurability(stack, stats.maxDurability),
                stack.getDamageValue(),
                SmitheryToolItem.totalModifierSlots(comp),
                SmitheryToolItem.appliedModifierCount(stack),
                SmitheryToolItem.freeModifierSlots(stack),
                type != null && SmitheryToolItem.harvestLevelApplies(type));
    }

    // ------------------------------------------------------------------ example gear

    /** Plain wood, the material every handle and binder accepts — the neutral filler. */
    private static final ResourceLocation WOOD = new ResourceLocation("smithery", "wood");

    /**
     * One buildable tool per head material that reaches {@code minLevel}, for the quest book's
     * "here's what satisfies this" link. Only the head varies: handles and binder are plain
     * wood, so the cycle reads as "these are the heads for this tier" rather than a parade of
     * unrelated exotic tools.
     *
     * <p>Materials sitting <em>exactly</em> on the requested level are preferred, since that is
     * what the quest text lists. A rung with nothing on it — level 13 has no material of its own
     * — falls back to the cheapest materials above it, which is what actually clears the bar.</p>
     *
     * <p>Every composition is run through {@link ToolComposition#isValid()} before it ships. An
     * ineligible material in any slot yields a broken, wrongly-named item rather than a tool, and
     * a decorative mock-up in a quest book is worse than no example at all.</p>
     */
    static List<ItemStack> exampleGear(int minLevel, ResourceLocation toolTypeFilter, int limit) {
        ResourceLocation wantedType = toolTypeFilter != null
                ? toolTypeFilter
                : new ResourceLocation("smithery", "pickaxe");

        Item toolItem = null;
        ToolType type = null;
        for (Item item : BuiltInRegistries.ITEM) {
            ToolType candidate = toolTypeOf(item);
            if (candidate != null && wantedType.equals(candidate.id())) {
                toolItem = item;
                type = candidate;
                break;
            }
        }
        if (toolItem == null || type.slots().isEmpty()) return List.of();

        List<ToolType.Slot> slots = type.slots();
        ResourceLocation headPart = null;
        for (ToolType.Slot slot : slots) {
            if (slot.role() == DurabilityRole.ADDITIVE) { headPart = slot.partType().id(); break; }
        }
        if (headPart == null) return List.of();

        List<Material> heads = headMaterials(headPart, minLevel);
        List<ItemStack> out = new ArrayList<>();
        for (Material head : heads) {
            if (out.size() >= limit) break;
            List<ResourceLocation> materials = new ArrayList<>(slots.size());
            for (int i = 0; i < slots.size(); i++) {
                ResourceLocation partId = slots.get(i).partType().id();
                // Slot 0 is the head — the only slot that decides harvest level. Everything
                // else is wood unless this part refuses it.
                if (i == 0) {
                    materials.add(head.id());
                } else if (PartEligibility.isAllowed(partId, WOOD)) {
                    materials.add(WOOD);
                } else {
                    materials.add(firstAllowed(partId, head.id()));
                }
            }
            ToolComposition comp = new ToolComposition(type.id(), materials);
            if (!comp.isValid()) continue;
            out.add(ToolCompositions.apply(new ItemStack(toolItem), comp));
        }
        return List.copyOf(out);
    }

    /**
     * Head materials for a rung: those sitting exactly on it, or — when the rung has none of its
     * own — the cheapest ones above it. Sorted so the cycle order is stable between openings.
     */
    private static List<Material> headMaterials(ResourceLocation headPart, int minLevel) {
        List<Material> exact = new ArrayList<>();
        List<Material> above = new ArrayList<>();
        for (Material material : SmitheryAPI.MATERIALS.all()) {
            if (!PartEligibility.isAllowed(headPart, material.id())) continue;
            int level = material.stats().harvestLevel();
            if (level == minLevel) exact.add(material);
            else if (level > minLevel) above.add(material);
        }
        List<Material> picked = exact.isEmpty() ? above : exact;
        picked.sort(Comparator
                .comparingInt((Material m) -> m.stats().harvestLevel())
                .thenComparing(m -> m.id().toString()));
        return picked;
    }

    /** Any material this part accepts, for slots the head material isn't eligible for. */
    private static ResourceLocation firstAllowed(ResourceLocation partType, ResourceLocation fallback) {
        for (Material material : SmitheryAPI.MATERIALS.all()) {
            if (PartEligibility.isAllowed(partType, material.id())) return material.id();
        }
        return fallback;
    }

    /**
     * Smithery's composable item classes share no common interface — {@code SmitheryToolItem}
     * extends {@code Item} while {@code SmitheryArmorItem} extends {@code DyeableArmorItem} —
     * so the tool type has to be pulled per class. {@code ToolCompositions.isComposable} accepts
     * exactly these two, so this covers every case that reaches it.
     */
    private static ToolType toolTypeOf(Item item) {
        if (item instanceof SmitheryToolItem tool) return tool.toolType();
        if (item instanceof SmitheryArmorItem armor) return armor.toolType();
        return null;
    }

    /** Reads the stored composition, or null when the stack has none. */
    private static ToolComposition compositionOf(ItemStack stack) {
        if (stack.isEmpty() || !SmitheryToolData.hasComposition(stack)) return null;
        ToolComposition comp = SmitheryToolData.getComposition(stack);
        return comp != null && comp.isValid() ? comp : null;
    }

    /** How many distinct materials Smithery currently knows about; the example-gear cache key. */
    static int materialCount() {
        return SmitheryAPI.MATERIALS.size();
    }
}
