package com.soul.soa_additions.loot.artifact;

import com.soul.smithery.api.SmitheryAPI;
import com.soul.smithery.api.modifier.ModifierEffect;
import com.soul.smithery.api.tool.ToolType;
import com.soul.smithery.item.tool.SmitheryToolData;
import com.soul.smithery.item.tool.ToolComposition;
import com.soul.smithery.item.tool.ToolCompositions;
import com.soul.soa_additions.smithery.SoaSmitheryModifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds sealed artifact tool stacks from {@link ArtifactDefs.Def} entries.
 *
 * <p>1.12 TConEvo dropped artifacts as fully-built Tinker tools in a sealed state;
 * the Plate of Unsealing (crafting) removes the seal. Sealed state here is the
 * {@code SoaSealed} NBT flag: usage is blocked by {@link ArtifactEvents} and the
 * hover name reads "Sealed Artifact" until unsealed.</p>
 */
public final class ArtifactBuilder {

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public static final String SEALED_TAG = "SoaSealed";
    public static final String NAME_TAG = "SoaArtifactName";

    /** 1.12 TCon tool names → Smithery tool types (nearest equivalent where removed). */
    private static final Map<String, String> TOOL_MAP = Map.ofEntries(
            Map.entry("hatchet", "axe"),
            Map.entry("hammer", "mining_hammer"),
            Map.entry("mattock", "paxel"),
            Map.entry("longsword", "broadsword"),
            Map.entry("shortbow", "bow"),
            Map.entry("longbow", "bow"),
            Map.entry("frypan", "battlesign"),
            Map.entry("scythe", "kama"),
            Map.entry("lumberaxe", "lumberaxe"),
            Map.entry("shuriken", "shuriken"));

    private ArtifactBuilder() {}

    /** Builds the sealed artifact stack for {@code def}; empty stack if the def can't be realized. */
    public static ItemStack build(ArtifactDefs.Def def) {
        String typeName = def.armour() != null ? def.armour() : TOOL_MAP.getOrDefault(def.tool(), def.tool());
        if ("tconevo.sceptre".equals(typeName)) return ItemStack.EMPTY; // no sceptre tool type (yet)

        ResourceLocation typeId = ResourceLocation.fromNamespaceAndPath("smithery", typeName);
        ToolType type = SmitheryAPI.TOOL_TYPES.get(typeId);
        Item item = ForgeRegistries.ITEMS.getValue(typeId);
        if (type == null || item == null) {
            LOGGER.warn("Artifact {}: unknown tool type {}", def.defId(), typeName);
            return ItemStack.EMPTY;
        }

        int slots = type.slots().size();
        List<ResourceLocation> mats = new ArrayList<>(slots);
        for (int i = 0; i < slots; i++) {
            String name = def.materials().isEmpty() ? "iron"
                    : def.materials().get(Math.min(i, def.materials().size() - 1));
            ResourceLocation matId = resolveMaterial(name);
            if (matId == null) {
                LOGGER.warn("Artifact {}: unknown material {}", def.defId(), name);
                return ItemStack.EMPTY;
            }
            mats.add(matId);
        }

        ItemStack stack = new ItemStack(item);

        List<ModifierEffect> applied = new ArrayList<>();
        applied.add(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_ARTIFACT));
        for (ArtifactDefs.Mod mod : def.mods()) {
            ResourceLocation modId = resolveModifier(mod.id());
            if (modId == null) {
                LOGGER.warn("Artifact {}: unknown modifier {} — skipped", def.defId(), mod.id());
                continue;
            }
            applied.add(mod.level() > 1
                    ? ModifierEffect.of(modId, Map.of("level", mod.level()))
                    : ModifierEffect.of(modId));
        }
        SmitheryToolData.setAppliedModifiers(stack, applied);
        stack = ToolCompositions.apply(stack, new ToolComposition(typeId, mats));

        var tag = stack.getOrCreateTag();
        tag.putBoolean(SEALED_TAG, true);
        tag.putString(NAME_TAG, def.name());
        if (!def.lore().isEmpty()) {
            ListTag loreList = new ListTag();
            loreList.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal(def.lore()).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))));
            stack.getOrCreateTagElement("display").put("Lore", loreList);
        }
        stack.setHoverName(Component.literal("Sealed Artifact")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        return stack;
    }

    /** True if {@code stack} is a sealed (not yet unsealed) artifact. */
    public static boolean isSealed(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(SEALED_TAG);
    }

    /** Returns an unsealed copy: seal flag removed, true artifact name revealed. */
    public static ItemStack unseal(ItemStack sealed) {
        ItemStack out = sealed.copy();
        var tag = out.getOrCreateTag();
        String name = tag.getString(NAME_TAG);
        tag.remove(SEALED_TAG);
        if (!name.isEmpty()) {
            out.setHoverName(Component.literal(name).withStyle(ChatFormatting.GOLD));
        }
        return out;
    }

    private static ResourceLocation resolveMaterial(String name) {
        ResourceLocation soa = ResourceLocation.fromNamespaceAndPath("soa_additions", name);
        if (SmitheryAPI.MATERIALS.get(soa) != null) return soa;
        ResourceLocation builtin = ResourceLocation.fromNamespaceAndPath("smithery", name);
        if (SmitheryAPI.MATERIALS.get(builtin) != null) return builtin;
        return null;
    }

    private static ResourceLocation resolveModifier(String name) {
        ResourceLocation soa = ResourceLocation.fromNamespaceAndPath("soa_additions", name);
        if (SmitheryAPI.MODIFIERS.get(soa) != null) return soa;
        ResourceLocation builtin = ResourceLocation.fromNamespaceAndPath("smithery", name);
        if (SmitheryAPI.MODIFIERS.get(builtin) != null) return builtin;
        return null;
    }
}
