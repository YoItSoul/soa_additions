package com.soul.soa_additions.bloodarsenal.compat.jei;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.bloodarsenal.recipe.BARecipeTypes;
import com.soul.soa_additions.bloodarsenal.recipe.SanguineInfusionRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * JEI plugin for Blood Arsenal content — registers the Sanguine Infusion
 * recipe category and populates it from the datapack recipe manager.
 */
@JeiPlugin
@OnlyIn(Dist.CLIENT)
public class BAJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID =
            new ResourceLocation(SoaAdditions.MODID, "ba_jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new SanguineInfusionCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = net.minecraft.client.Minecraft.getInstance().level.getRecipeManager();
        List<SanguineInfusionRecipe> recipes = recipeManager
                .getAllRecipesFor(BARecipeTypes.SANGUINE_INFUSION_TYPE.get());
        registration.addRecipes(SanguineInfusionCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Master Ritual Stone as catalyst, matching original BA
        registration.addRecipeCatalyst(
                new ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                        new ResourceLocation("bloodmagic", "masterritualstone"))),
                SanguineInfusionCategory.RECIPE_TYPE);
        // Stasis Plate as secondary catalyst
        registration.addRecipeCatalyst(
                new ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                        new ResourceLocation(SoaAdditions.MODID, "ba_stasis_plate"))),
                SanguineInfusionCategory.RECIPE_TYPE);
    }
}
