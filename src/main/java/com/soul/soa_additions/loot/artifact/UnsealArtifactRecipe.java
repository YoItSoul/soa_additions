package com.soul.soa_additions.loot.artifact;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Sealed artifact + Artifact Unsealer (Plate of Unsealing) → unsealed artifact.
 * The plate is consumed — single use, as in 1.12 TConEvo.
 */
public class UnsealArtifactRecipe extends CustomRecipe {

    private static final ResourceLocation UNSEALER =
            ResourceLocation.fromNamespaceAndPath("tconevo", "artifact_unsealer");

    public UnsealArtifactRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return findSealed(container) != null && countRelevant(container) == 2 && hasUnsealer(container);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        ItemStack sealed = findSealed(container);
        return sealed == null ? ItemStack.EMPTY : ArtifactBuilder.unseal(sealed);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TconevoArtifacts.UNSEAL_SERIALIZER.get();
    }

    private static ItemStack findSealed(CraftingContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack s = container.getItem(i);
            if (!s.isEmpty() && ArtifactBuilder.isSealed(s)) return s;
        }
        return null;
    }

    private static boolean hasUnsealer(CraftingContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack s = container.getItem(i);
            if (!s.isEmpty() && UNSEALER.equals(ForgeRegistries.ITEMS.getKey(s.getItem()))) return true;
        }
        return false;
    }

    private static int countRelevant(CraftingContainer container) {
        int n = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) n++;
        }
        return n;
    }
}
