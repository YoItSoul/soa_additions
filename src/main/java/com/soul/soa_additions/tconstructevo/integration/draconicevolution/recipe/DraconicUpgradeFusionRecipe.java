package com.soul.soa_additions.tconstructevo.integration.draconicevolution.recipe;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.api.crafting.FusionRecipe;
import com.brandon3055.draconicevolution.api.crafting.IFusionInventory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * Fusion recipe that applies a Tinkers' Construct modifier to whatever tool
 * the player drops into the catalyst slot.
 *
 * <p>Extends DE's {@link FusionRecipe} so we inherit the matching /
 * crafting-state plumbing for free (same {@code getType()} → fusion recipes,
 * same injector handling). Only the {@link #assemble} step is different:
 * instead of returning the JSON-declared result, we clone the catalyst tool,
 * add one level of the configured modifier, and return the upgraded copy.
 * The crafting core then consumes the catalyst stack (the un-upgraded tool)
 * as usual and places the upgraded clone in the output slot.</p>
 *
 * <p>Parallels the 1.12.2 {@code DraconicUpgradeRecipe} from tconevo-1.1.5;
 * the 1.12.2 version abused {@code getRecipeOutput} for the same trick.
 * TC3's {@link ToolStack#addModifier} is the direct equivalent of the old
 * {@code Modifier.apply}, and the rebuildStats call replaces tconevo's
 * {@code ToolUtils.rebuildToolStack}.</p>
 *
 * <p>The JSON {@code result} field is retained only so JEI / recipe book
 * displays have something non-empty to render; at craft time it is ignored
 * in favour of the catalyst-derived output.</p>
 */
public class DraconicUpgradeFusionRecipe extends FusionRecipe {

    private final ResourceLocation modifierId;

    public DraconicUpgradeFusionRecipe(ResourceLocation id,
                                       ItemStack displayResult,
                                       Ingredient catalyst,
                                       long totalEnergy,
                                       TechLevel techLevel,
                                       Collection<FusionRecipe.FusionIngredient> ingredients,
                                       ResourceLocation modifierId) {
        super(id, displayResult, catalyst, totalEnergy, techLevel, ingredients);
        this.modifierId = modifierId;
    }

    public ResourceLocation getModifierId() {
        return modifierId;
    }

    @Override
    public ItemStack assemble(IFusionInventory inv, RegistryAccess registryAccess) {
        ItemStack catalyst = inv.getCatalystStack();
        if (catalyst.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = catalyst.copy();
        result.setCount(1);
        ToolStack tool = ToolStack.from(result);
        tool.addModifier(new ModifierId(this.modifierId), 1);
        tool.rebuildStats();
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DraconicFusionRecipes.DRACONIC_TOOL_UPGRADE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<DraconicUpgradeFusionRecipe> {

        @Override
        public DraconicUpgradeFusionRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            Ingredient catalyst = CraftingHelper.getIngredient(GsonHelper.getAsJsonObject(json, "catalyst"), false);
            List<FusionRecipe.FusionIngredient> fusionIngredients = new ArrayList<>();
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "ingredients")) {
                Ingredient ingredient;
                if (element.isJsonObject() && element.getAsJsonObject().has("ingredient")) {
                    ingredient = CraftingHelper.getIngredient(element.getAsJsonObject().get("ingredient"), false);
                } else {
                    ingredient = CraftingHelper.getIngredient(element, false);
                }
                boolean consume = !element.isJsonObject()
                        || GsonHelper.getAsBoolean(element.getAsJsonObject(), "consume", true);
                fusionIngredients.add(new FusionRecipe.FusionIngredient(ingredient, consume));
            }
            long totalEnergy = GsonHelper.getAsLong(json, "total_energy");
            TechLevel techLevel = TechLevel.valueOf(GsonHelper.getAsString(json, "tier", TechLevel.DRACONIUM.name()));
            ResourceLocation modifierId = new ResourceLocation(GsonHelper.getAsString(json, "modifier"));
            return new DraconicUpgradeFusionRecipe(id, result, catalyst, totalEnergy, techLevel, fusionIngredients, modifierId);
        }

        @Override
        public DraconicUpgradeFusionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ItemStack result = buffer.readItem();
            Ingredient catalyst = Ingredient.fromNetwork(buffer);
            int count = buffer.readByte();
            List<FusionRecipe.FusionIngredient> fusionIngredients = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                boolean consume = buffer.readBoolean();
                Ingredient ingredient = Ingredient.fromNetwork(buffer);
                fusionIngredients.add(new FusionRecipe.FusionIngredient(ingredient, consume));
            }
            long totalEnergy = buffer.readLong();
            TechLevel techLevel = TechLevel.VALUES[Mth.clamp(buffer.readByte(), 0, TechLevel.values().length - 1)];
            ResourceLocation modifierId = buffer.readResourceLocation();
            return new DraconicUpgradeFusionRecipe(id, result, catalyst, totalEnergy, techLevel, fusionIngredients, modifierId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DraconicUpgradeFusionRecipe recipe) {
            buffer.writeItemStack(recipe.getResultItem(null), false);
            recipe.getCatalyst().toNetwork(buffer);
            List<com.brandon3055.draconicevolution.api.crafting.IFusionRecipe.IFusionIngredient> ings = recipe.fusionIngredients();
            buffer.writeByte(ings.size());
            for (com.brandon3055.draconicevolution.api.crafting.IFusionRecipe.IFusionIngredient ing : ings) {
                buffer.writeBoolean(ing.consume());
                ing.get().toNetwork(buffer);
            }
            buffer.writeLong(recipe.getEnergyCost());
            buffer.writeByte(recipe.getRecipeTier().index);
            buffer.writeResourceLocation(recipe.getModifierId());
        }
    }
}
