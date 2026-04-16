package com.soul.soa_additions.bloodarsenal.compat.jei;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.bloodarsenal.recipe.SanguineInfusionRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.awt.*;
import java.util.List;

/**
 * JEI recipe category for Sanguine Infusion.
 * Matches the original Blood Arsenal 1.12 circular layout:
 * - Center slot: first ingredient (the "infuse" item, e.g. sentient sword)
 * - Ring of slots: remaining ingredients arranged in a circle
 * - Output slot: top-right
 * - LP cost text displayed below output
 */
@OnlyIn(Dist.CLIENT)
public class SanguineInfusionCategory implements IRecipeCategory<SanguineInfusionRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(SoaAdditions.MODID, "sanguine_infusion");
    public static final RecipeType<SanguineInfusionRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, SanguineInfusionRecipe.class);

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SoaAdditions.MODID, "textures/gui/sanguine_infusion.png");

    private final IDrawable background;
    private final IDrawable icon;

    public SanguineInfusionCategory(IGuiHelper guiHelper) {
        // Original: 103x103 background from sanguine_infusion.png
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 103, 103);
        // Icon: use stasis plate block as category icon
        this.icon = guiHelper.createDrawableItemStack(
                new net.minecraft.world.item.ItemStack(
                        net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                                new ResourceLocation(SoaAdditions.MODID, "ba_stasis_plate"))));
    }

    @Override
    public RecipeType<SanguineInfusionRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.soa_additions.recipe.sanguine_infusion");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SanguineInfusionRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) return;

        // Center slot (27, 57) - original coordinates from 1.12 source
        // This is the "infuse" item (first ingredient, e.g. the sentient sword for stasis tools)
        int centerX = 27;
        int centerY = 57;

        builder.addSlot(RecipeIngredientRole.INPUT, centerX, centerY)
                .addIngredients(ingredients.get(0));

        // Output slot (77, 8) - original coordinates
        builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 8)
                .addItemStack(recipe.getOutput());

        // Remaining ingredients arranged in a circle around center
        if (ingredients.size() > 1) {
            int ringCount = ingredients.size() - 1;
            double angleBetweenEach = 360.0 / ringCount;

            // Start point: directly above center, offset by radius ~34px
            // Original: Point(centerX + 11, centerY - 34) rotated around Point(centerX - 1, centerY)
            double startX = centerX + 11;
            double startY = centerY - 34;
            double aboutX = centerX - 1;
            double aboutY = centerY;

            double currentX = startX;
            double currentY = startY;

            for (int i = 1; i < ingredients.size(); i++) {
                builder.addSlot(RecipeIngredientRole.INPUT, (int) currentX, (int) currentY)
                        .addIngredients(ingredients.get(i));

                // Rotate point for next slot
                double rad = angleBetweenEach * Math.PI / 180.0;
                double newX = Math.cos(rad) * (currentX - aboutX) - Math.sin(rad) * (currentY - aboutY) + aboutX;
                double newY = Math.sin(rad) * (currentX - aboutX) + Math.cos(rad) * (currentY - aboutY) + aboutY;
                currentX = newX;
                currentY = newY;
            }
        }
    }

    @Override
    public void draw(SanguineInfusionRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw LP cost text, matching original position and style
        String lpText = Component.translatable("jei.soa_additions.recipe.required_lp",
                recipe.getLpCost()).getString();
        int textWidth = net.minecraft.client.Minecraft.getInstance().font.width(lpText);
        guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                lpText,
                110 - textWidth / 2, 40,
                Color.GRAY.getRGB(), false);
    }
}
