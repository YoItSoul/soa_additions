package com.soul.soa_additions.tconstructevo.item.artifact;

import com.soul.soa_additions.tconstructevo.TConstructEvoPlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Translates a parsed {@link ArtifactSpec} into the live {@link ItemStack} the
 * player receives. Mirrors {@code ArtifactTypeTool.buildArtifact} from the
 * 1.12.2 source: resolve tool item → resolve materials → call TC3's
 * {@code ToolBuildHandler.buildItemFromMaterials} → grant free modifier slots
 * → apply each declared modifier at its level → set styled custom name and
 * lore tag.
 *
 * <p>Returns {@code null} when any tool/material/modifier id cannot be
 * resolved; the caller logs and skips. The 1.12.2 {@code ArtifactType.BuildingException}
 * indirection isn't ported — TC3 does its own validation deeper in the
 * tool stack pipeline.</p>
 */
public final class ArtifactBuilder {

    private static final ChatFormatting NAME_COLOR = ChatFormatting.YELLOW;
    private static final ChatFormatting LORE_COLOR = ChatFormatting.DARK_PURPLE;

    private ArtifactBuilder() {}

    public static ItemStack build(ArtifactSpec spec) {
        Item rawTool = ForgeRegistries.ITEMS.getValue(parseToolId(spec.tool));
        if (!(rawTool instanceof IModifiable tool)) {
            TConstructEvoPlugin.LOG.warn("Artifact \"{}\": tool \"{}\" is not a Tinkers tool", spec.name, spec.tool);
            return null;
        }

        List<MaterialVariant> matVariants = new ArrayList<>();
        for (String matId : spec.materials) {
            MaterialId id = new MaterialId(parseMaterialId(matId));
            IMaterial mat = MaterialRegistry.getMaterial(id);
            if (mat == IMaterial.UNKNOWN) {
                TConstructEvoPlugin.LOG.warn("Artifact \"{}\": unknown material \"{}\"", spec.name, matId);
                return null;
            }
            matVariants.add(MaterialVariant.of(mat));
        }

        ItemStack stack = ToolBuildHandler.buildItemFromMaterials(tool, new MaterialNBT(matVariants));
        if (stack.isEmpty()) {
            TConstructEvoPlugin.LOG.warn("Artifact \"{}\": tool build returned empty stack", spec.name);
            return null;
        }

        ToolStack toolStack = ToolStack.from(stack);
        if (spec.freeMods > 0) {
            toolStack.getPersistentData().addSlots(SlotType.UPGRADE, spec.freeMods);
        }
        for (ArtifactSpec.ModEntry mod : spec.modifiers) {
            toolStack.addModifier(new ModifierId(parseModifierId(mod.id())), mod.level());
        }

        stack.setHoverName(Component.literal(spec.name)
                .withStyle(NAME_COLOR, ChatFormatting.UNDERLINE));

        if (!spec.lore.isEmpty()) {
            CompoundTag display = stack.getOrCreateTagElement("display");
            ListTag loreList = new ListTag();
            for (String line : spec.lore) {
                Component loreLine = Component.literal(line)
                        .withStyle(LORE_COLOR, ChatFormatting.ITALIC);
                loreList.add(StringTag.valueOf(Component.Serializer.toJson(loreLine)));
            }
            display.put("Lore", loreList);
        }

        return stack;
    }

    private static ResourceLocation parseToolId(String raw) {
        if (raw.indexOf(':') >= 0) return new ResourceLocation(raw);
        return new ResourceLocation("tconstruct", raw);
    }

    private static ResourceLocation parseMaterialId(String raw) {
        if (raw.indexOf(':') >= 0) return new ResourceLocation(raw);
        return new ResourceLocation("tconstruct", raw);
    }

    private static ResourceLocation parseModifierId(String raw) {
        if (raw.indexOf(':') >= 0) return new ResourceLocation(raw);
        return new ResourceLocation("tconstruct", raw);
    }
}
