package com.soul.soa_additions.quest.client.jei;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.oresight.OreSight;
import com.soul.soa_additions.oresight.OreSightBrewing;
import com.soul.soa_additions.potion.SoaBrewingPotions;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI integration entry point. JEI scans for {@link JeiPlugin} annotated
 * classes and instantiates them at startup; we use the hook for two things:
 *
 * <ol>
 *   <li>Capture the {@link IJeiRuntime} so {@link JeiCompat} can open recipe
 *       views from the quest book.</li>
 *   <li>Register brewing recipes that vanilla's auto-scanner can't surface —
 *       specifically the ore-sight chain, which uses dynamic
 *       {@link net.minecraftforge.common.brewing.IBrewingRecipe} matchers
 *       rather than static {@link net.minecraftforge.common.brewing.BrewingRecipe}
 *       triples. Without explicit registration JEI shows nothing for the
 *       Mutagenic / Ore Sight / Long Ore Sight steps.</li>
 * </ol>
 */
@JeiPlugin
public final class SoaJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(SoaAdditions.MODID, "jei_plugin");

    @Override public ResourceLocation getPluginUid() { return UID; }

    @Override public void onRuntimeAvailable(IJeiRuntime runtime) {
        JeiCompat.setRuntime(runtime);
    }

    @Override public void onRuntimeUnavailable() {
        JeiCompat.setRuntime(null);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        List<IJeiBrewingRecipe> brewing = new ArrayList<>();

        // ── Step 1: water + thaumon:mutagen → mutagenic
        var mutagenItem = ForgeRegistries.ITEMS.getValue(OreSightBrewing.MUTAGEN_ID);
        if (mutagenItem != null) {
            ItemStack waterPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
            ItemStack mutagenicPotion = PotionUtils.setPotion(new ItemStack(Items.POTION),
                    SoaBrewingPotions.MUTAGENIC.get());
            brewing.add(factory.createBrewingRecipe(
                    List.of(new ItemStack(mutagenItem)),
                    waterPotion, mutagenicPotion));
        }

        // ── Step 2: mutagenic + ore-block → ore_sight (per ore)
        // ── Step 3: ore_sight + redstone → long_ore_sight (per ore)
        Potion baseMutagenic = SoaBrewingPotions.MUTAGENIC.get();
        Potion baseOreSight = SoaBrewingPotions.ORE_SIGHT.get();
        Potion longOreSight = SoaBrewingPotions.LONG_ORE_SIGHT.get();
        ItemStack mutagenicPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), baseMutagenic);
        ItemStack redstone = new ItemStack(Items.REDSTONE);

        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (!OreSight.isOreBlock(block)) continue;
            ItemStack blockStack = new ItemStack(block.asItem());
            if (blockStack.isEmpty()) continue;
            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
            if (blockId == null) continue;

            // Output: ore_sight potion tagged with this block's id (custom item)
            ItemStack oreSightPotion = PotionUtils.setPotion(
                    new ItemStack(ModItems.ORE_SIGHT_POTION.get()), baseOreSight);
            CompoundTag tag = oreSightPotion.getOrCreateTag();
            tag.putString(OreSightBrewing.NBT_BLOCK, blockId.toString());

            brewing.add(factory.createBrewingRecipe(
                    List.of(blockStack), mutagenicPotion, oreSightPotion));

            // Long variant: ore_sight + redstone → long_ore_sight (NBT preserved)
            ItemStack longPotion = PotionUtils.setPotion(
                    new ItemStack(ModItems.ORE_SIGHT_POTION.get()), longOreSight);
            longPotion.getOrCreateTag().putString(OreSightBrewing.NBT_BLOCK, blockId.toString());

            brewing.add(factory.createBrewingRecipe(
                    List.of(redstone), oreSightPotion.copy(), longPotion));
        }

        registration.addRecipes(RecipeTypes.BREWING, brewing);
    }
}
