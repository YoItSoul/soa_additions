package com.soul.soa_additions.oresight;

import com.soul.soa_additions.potion.SoaBrewingPotions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Custom potion items for the Ore Sight system. Three subclasses (drinkable,
 * splash, lingering) extend the corresponding vanilla PotionItem variants and
 * override {@link #getName(ItemStack)} to splice the tracked block's display
 * name into the item name — e.g. {@code "Gold Ore Sight Potion"} instead of
 * the static {@code "Potion of Ore Sight"} that vanilla resolves from the
 * Potion registry name.
 *
 * <p>The Potion registry entry on the stack ({@code soa_additions:ore_sight}
 * vs {@code long_ore_sight}) still controls which effect/duration the bottle
 * grants. The block id lives on NBT under {@link OreSightBrewing#NBT_BLOCK}.
 * Color tint is inherited from {@link PotionItem#getColor(ItemStack)} which
 * reads the Potion's effect colors, so the bottle picks up the cyan from
 * {@link OreSightEffect}.</p>
 */
public final class OreSightPotionItem extends PotionItem {

    public OreSightPotionItem(Properties props) { super(props); }

    @Override
    public Component getName(ItemStack stack) {
        return OreSightPotionItem.formatName(stack, "item.soa_additions.ore_sight_potion");
    }

    /** Splash variant. */
    public static final class Splash extends SplashPotionItem {
        public Splash(Properties props) { super(props); }
        @Override public Component getName(ItemStack stack) {
            return OreSightPotionItem.formatName(stack, "item.soa_additions.ore_sight_splash_potion");
        }
    }

    /** Lingering variant. */
    public static final class Lingering extends LingeringPotionItem {
        public Lingering(Properties props) { super(props); }
        @Override public Component getName(ItemStack stack) {
            return OreSightPotionItem.formatName(stack, "item.soa_additions.ore_sight_lingering_potion");
        }
    }

    /**
     * Compose the display name based on the stack's Potion + NBT.
     * <ol>
     *   <li>Master potion → use the {@code .master} variant of the lang key
     *       (no block splice).</li>
     *   <li>Regular ore-sight with NBT block → splice the block's display
     *       name into the base lang key.</li>
     *   <li>Anything else (missing NBT, unknown block) → fall back to
     *       {@code .unknown}.</li>
     * </ol>
     */
    private static Component formatName(ItemStack stack, String langKey) {
        Potion p = PotionUtils.getPotion(stack);
        if (p == SoaBrewingPotions.MASTER_ORE_SIGHT.get()
                || p == SoaBrewingPotions.LONG_MASTER_ORE_SIGHT.get()) {
            return Component.translatable(langKey + ".master");
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(OreSightBrewing.NBT_BLOCK)) {
            Block block = ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation(tag.getString(OreSightBrewing.NBT_BLOCK)));
            if (block != null) {
                return Component.translatable(langKey, block.getName());
            }
        }
        return Component.translatable(langKey + ".unknown");
    }
}
