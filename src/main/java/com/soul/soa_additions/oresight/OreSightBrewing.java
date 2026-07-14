package com.soul.soa_additions.oresight;

import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.potion.SoaBrewingPotions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Custom brewing recipes that drive the ore-sight system.
 *
 * <ol>
 *   <li><b>Step 1 (vanilla mix):</b> Water Bottle + {@code thaumon:mutagen} →
 *       Mutagenic Potion.</li>
 *   <li><b>Step 2 ({@link OreSightCreateRecipe}):</b> Mutagenic Potion + (any
 *       {@link net.minecraft.world.item.BlockItem} whose block is an ore via
 *       {@link OreSight#isOreBlock}) → Ore Sight Potion. The output carries
 *       the input block's id under the {@link #NBT_BLOCK} NBT key.</li>
 *   <li><b>Step 3 ({@link OreSightExtendRecipe}):</b> Ore Sight Potion +
 *       Redstone → Long Ore Sight Potion (NBT-preserved).</li>
 * </ol>
 *
 * <p>One potion per ore variant. Brew with {@code gold_ore} → highlights
 * {@code gold_ore} only; brew with {@code deepslate_gold_ore} → highlights
 * that variant only. Silk-touch is implicitly required to obtain the input
 * block-item, which is the intended progression gate.</p>
 */
public final class OreSightBrewing {

    /** NBT key on the Ore Sight potion ItemStack: stores the tracked ore
     *  block's registry id (e.g. {@code "minecraft:gold_ore"}). */
    public static final String NBT_BLOCK = "OreSightBlock";

    /** thaumon:mutagen item id. */
    public static final ResourceLocation MUTAGEN_ID =
            new ResourceLocation("thaumon", "mutagen");

    private OreSightBrewing() {}

    /** Called from {@link com.soul.soa_additions.potion.SoaBrewing#registerBrewing()}. */
    public static void register() {
        // Step 1 — water + thaumon:mutagen → mutagenic
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)),
                Ingredient.of(ForgeRegistries.ITEMS.getValue(MUTAGEN_ID) == null
                        ? Items.AIR
                        : ForgeRegistries.ITEMS.getValue(MUTAGEN_ID)),
                PotionUtils.setPotion(new ItemStack(Items.POTION), SoaBrewingPotions.MUTAGENIC.get()));

        // Step 2 — mutagenic + ore-block → ore_sight (NBT-tagged)
        BrewingRecipeRegistry.addRecipe(new OreSightCreateRecipe());

        // Step 3 — ore_sight + redstone → long_ore_sight (NBT-preserved)
        BrewingRecipeRegistry.addRecipe(new OreSightExtendRecipe());
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Step 2: mutagenic + ore-block → ore_sight
    // ─────────────────────────────────────────────────────────────────────

    public static final class OreSightCreateRecipe implements IBrewingRecipe {

        @Override
        public boolean isInput(ItemStack stack) {
            if (!(stack.getItem() == Items.POTION
                    || stack.getItem() == Items.SPLASH_POTION
                    || stack.getItem() == Items.LINGERING_POTION)) return false;
            return PotionUtils.getPotion(stack) == SoaBrewingPotions.MUTAGENIC.get();
        }

        @Override
        public boolean isIngredient(ItemStack ingredient) {
            return OreSight.oreFromIngredient(ingredient) != null;
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
            if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
            Block ore = OreSight.oreFromIngredient(ingredient);
            if (ore == null) return ItemStack.EMPTY;
            ResourceLocation oreId = ForgeRegistries.BLOCKS.getKey(ore);
            if (oreId == null) return ItemStack.EMPTY;

            Item outItem = customForBottle(input.getItem());
            if (outItem == null) return ItemStack.EMPTY;
            ItemStack out = new ItemStack(outItem);
            PotionUtils.setPotion(out, SoaBrewingPotions.ORE_SIGHT.get());
            CompoundTag tag = out.getOrCreateTag();
            tag.putString(NBT_BLOCK, oreId.toString());
            return out;
        }
    }

    /** Map a vanilla bottle item to our matching custom potion item. Returns
     *  null if the input isn't one of the three bottle forms. */
    static Item customForBottle(Item vanilla) {
        if (vanilla == Items.POTION)            return ModItems.ORE_SIGHT_POTION.get();
        if (vanilla == Items.SPLASH_POTION)     return ModItems.ORE_SIGHT_SPLASH_POTION.get();
        if (vanilla == Items.LINGERING_POTION)  return ModItems.ORE_SIGHT_LINGERING_POTION.get();
        return null;
    }

    /** True iff this stack is one of our three custom ore-sight potion items. */
    public static boolean isOreSightPotionItem(Item item) {
        return item == ModItems.ORE_SIGHT_POTION.get()
                || item == ModItems.ORE_SIGHT_SPLASH_POTION.get()
                || item == ModItems.ORE_SIGHT_LINGERING_POTION.get();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Step 3: ore_sight + redstone → long_ore_sight  (NBT-preserved)
    // ─────────────────────────────────────────────────────────────────────

    public static final class OreSightExtendRecipe implements IBrewingRecipe {

        @Override
        public boolean isInput(ItemStack stack) {
            if (!isOreSightPotionItem(stack.getItem())) return false;
            Potion p = PotionUtils.getPotion(stack);
            return p == SoaBrewingPotions.ORE_SIGHT.get();
        }

        @Override
        public boolean isIngredient(ItemStack ingredient) {
            return ingredient.is(Items.REDSTONE);
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
            if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
            ItemStack out = new ItemStack(input.getItem());
            PotionUtils.setPotion(out, SoaBrewingPotions.LONG_ORE_SIGHT.get());
            CompoundTag in = input.getTag();
            if (in != null && in.contains(NBT_BLOCK)) {
                out.getOrCreateTag().putString(NBT_BLOCK, in.getString(NBT_BLOCK));
            }
            return out;
        }
    }
}
