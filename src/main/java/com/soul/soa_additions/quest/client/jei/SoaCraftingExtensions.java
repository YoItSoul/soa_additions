package com.soul.soa_additions.quest.client.jei;

import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.item.TabletDupeRecipe;
import com.soul.soa_additions.loot.artifact.ArtifactBuilder;
import com.soul.soa_additions.loot.artifact.ArtifactDefs;
import com.soul.soa_additions.loot.artifact.UnsealArtifactRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI layouts for the pack's two {@link net.minecraft.world.item.crafting.CustomRecipe crafting
 * recipes}.
 *
 * <p>JEI's vanilla crafting category can lay out a {@code ShapedRecipe} or {@code ShapelessRecipe}
 * on its own, because both expose a fixed ingredient list. A {@code CustomRecipe} decides what it
 * accepts in code and reports no ingredients at all, so JEI throws
 * {@code "Failed to create recipe extension for recipe"} and logs a stack trace for every one of
 * them on load. Registering an extension per recipe class — see
 * {@link SoaJeiPlugin#registerVanillaCategoryExtensions} — replaces the error with a real recipe
 * entry, since the extension supplies the inputs and outputs the recipe itself can't describe.</p>
 */
final class SoaCraftingExtensions {

    private SoaCraftingExtensions() {}

    /**
     * Sealed artifact + Artifact Unsealer → unsealed artifact.
     *
     * <p>Both input slots cycle: the artifact slot walks every definition in the loot table and
     * the output walks the matching unsealed result, so hovering shows what a given artifact
     * turns into rather than one arbitrary example.</p>
     */
    static final class Unseal implements ICraftingCategoryExtension {

        private static final ResourceLocation UNSEALER =
                new ResourceLocation("tconevo", "artifact_unsealer");

        private final UnsealArtifactRecipe recipe;
        private List<ItemStack> sealed;
        private List<ItemStack> unsealed;

        Unseal(UnsealArtifactRecipe recipe) {
            this.recipe = recipe;
        }

        /** Built on first display, not construction — artifact defs come from a datapack and
         *  aren't loaded yet when JEI registers its extensions. */
        private void resolve() {
            if (sealed != null) return;
            List<ItemStack> in = new ArrayList<>();
            List<ItemStack> out = new ArrayList<>();
            for (ArtifactDefs.Def def : ArtifactDefs.all().values()) {
                ItemStack stack = ArtifactBuilder.build(def);
                if (stack.isEmpty()) continue;
                in.add(stack);
                out.add(ArtifactBuilder.unseal(stack));
            }
            sealed = List.copyOf(in);
            unsealed = List.copyOf(out);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper gridHelper,
                              IFocusGroup focuses) {
            resolve();
            Item unsealer = ForgeRegistries.ITEMS.getValue(UNSEALER);
            List<ItemStack> plate = unsealer == null || unsealer == net.minecraft.world.item.Items.AIR
                    ? List.of()
                    : List.of(new ItemStack(unsealer));
            gridHelper.createAndSetInputs(builder, List.of(sealed, plate), 2, 1);
            gridHelper.createAndSetOutputs(builder, unsealed);
        }

        @Override public ResourceLocation getRegistryName() { return recipe.getId(); }
        @Override public int getWidth() { return 2; }
        @Override public int getHeight() { return 1; }
    }

    /**
     * A lone Tablet of Enlightenment copies itself. The tablet is its own crafting remainder, so
     * the original stays in the grid and the copy is the output — which is why input and output
     * are the same stack here.
     */
    static final class TabletDupe implements ICraftingCategoryExtension {

        private final TabletDupeRecipe recipe;

        TabletDupe(TabletDupeRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper gridHelper,
                              IFocusGroup focuses) {
            List<ItemStack> tablet = List.of(new ItemStack(ModItems.TABLET_OF_ENLIGHTENMENT.get()));
            gridHelper.createAndSetInputs(builder, List.of(tablet), 1, 1);
            gridHelper.createAndSetOutputs(builder, tablet);
        }

        @Override public ResourceLocation getRegistryName() { return recipe.getId(); }
        @Override public int getWidth() { return 1; }
        @Override public int getHeight() { return 1; }
    }
}
