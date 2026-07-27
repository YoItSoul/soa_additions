package com.soul.soa_additions.item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * GC "etablet_dupe": a lone Tablet of Enlightenment in a crafting grid yields a
 * full NBT copy — and since the tablet is its own crafting remainder, the
 * original stays in the grid. Exact port of GC's recipe function
 * (recipe_functions.zs) + itemGetContainerItem pair.
 */
public class TabletDupeRecipe extends CustomRecipe {

    public TabletDupeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    private static ItemStack findTablet(CraftingContainer container) {
        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack s = container.getItem(i);
            if (s.isEmpty()) continue;
            if (!(s.getItem() instanceof TabletOfEnlightenmentItem) || !found.isEmpty()) {
                return ItemStack.EMPTY;   // wrong item, or more than one stack
            }
            found = s;
        }
        return found;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !findTablet(container).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        ItemStack tablet = findTablet(container);
        return tablet.isEmpty() ? ItemStack.EMPTY : tablet.copyWithCount(1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SoaRecipeSerializers.TABLET_DUPE.get();
    }
}
