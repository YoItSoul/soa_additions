package com.soul.soa_additions.registry;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

public final class SoaTiers {

    public static final TagKey<Block> INFERNIUM_TAG = blockTag("needs_infernium_tool");
    public static final TagKey<Block> VOID_TAG      = blockTag("needs_void_tool");
    public static final TagKey<Block> ABYSSAL_TAG   = blockTag("needs_abyssal_tool");
    public static final TagKey<Block> ETHER_TAG     = blockTag("needs_ether_tool");

    public static final Tier INFERNIUM = registerTier("infernium", 5, INFERNIUM_TAG, Tiers.NETHERITE);
    public static final Tier VOID      = registerTier("void",      6, VOID_TAG,      INFERNIUM);
    public static final Tier ABYSSAL   = registerTier("abyssal",   7, ABYSSAL_TAG,   VOID);
    public static final Tier ETHER     = registerTier("ether",     8, ETHER_TAG,     ABYSSAL);

    private SoaTiers() {}

    /** Forces class initialization so the tiers above register during mod construction. */
    public static void bootstrap() {}

    private static Tier registerTier(String name, int level, TagKey<Block> tag, Tier after) {
        return TierSortingRegistry.registerTier(
                new ForgeTier(level, 0, 0, 0, 0, tag, () -> Ingredient.EMPTY),
                modLoc(name),
                List.of(after),
                List.of());
    }

    private static TagKey<Block> blockTag(String path) {
        return BlockTags.create(modLoc(path));
    }

    private static ResourceLocation modLoc(String path) {
        return new ResourceLocation(SoaAdditions.MODID, path);
    }
}
