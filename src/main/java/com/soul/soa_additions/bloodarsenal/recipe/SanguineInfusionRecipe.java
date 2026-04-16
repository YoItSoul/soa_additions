package com.soul.soa_additions.bloodarsenal.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Sanguine Infusion recipe — used by the Sanguine Infusion ritual.
 * Takes up to 8 input ingredients placed on stasis plates,
 * produces a single output item. LP cost configurable per recipe.
 */
public class SanguineInfusionRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack output;
    private final int lpCost;

    public SanguineInfusionRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients,
                                  ItemStack output, int lpCost) {
        this.id = id;
        this.ingredients = ingredients;
        this.output = output;
        this.lpCost = lpCost;
    }

    @Override
    public boolean matches(Container container, Level level) {
        // Check each ingredient against the container (stasis plates)
        boolean[] used = new boolean[container.getContainerSize()];

        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!used[i] && ingredient.test(container.getItem(i))) {
                    used[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BARecipeTypes.SANGUINE_INFUSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return BARecipeTypes.SANGUINE_INFUSION_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    public int getLpCost() {
        return lpCost;
    }

    public ItemStack getOutput() {
        return output;
    }

    // ── Serializer ──────────────────────────────────────────────────────

    public static class Serializer implements RecipeSerializer<SanguineInfusionRecipe> {

        @Override
        public SanguineInfusionRecipe fromJson(ResourceLocation id, JsonObject json) {
            int lpCost = GsonHelper.getAsInt(json, "lp_cost", 10000);

            JsonArray ingredientArray = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < ingredientArray.size(); i++) {
                ingredients.add(Ingredient.fromJson(ingredientArray.get(i)));
            }

            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            return new SanguineInfusionRecipe(id, ingredients, output, lpCost);
        }

        @Override
        public @Nullable SanguineInfusionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buf));
            }
            ItemStack output = buf.readItem();
            int lpCost = buf.readVarInt();

            return new SanguineInfusionRecipe(id, ingredients, output, lpCost);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SanguineInfusionRecipe recipe) {
            buf.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buf);
            }
            buf.writeItem(recipe.output);
            buf.writeVarInt(recipe.lpCost);
        }
    }
}
