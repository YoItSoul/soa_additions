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

    // GC mining-tier ladder. Levels 3-7 use GC's canonical material names
    // (obsidian/cobalt/duranite/valyrium/vibranium per patchouli misc/harvest_levels).
    // Cobalt (level 4) maps to vanilla Tiers.NETHERITE. Levels 8-13 and 127 have no
    // GC material ("Original Name: None") so they use GC's display names.
    public static final TagKey<Block> OBSIDIAN_TAG   = blockTag("needs_obsidian_tool");
    public static final TagKey<Block> DURANITE_TAG   = blockTag("needs_duranite_tool");
    public static final TagKey<Block> VALYRIUM_TAG   = blockTag("needs_valyrium_tool");
    public static final TagKey<Block> VIBRANIUM_TAG  = blockTag("needs_vibranium_tool");
    public static final TagKey<Block> HEAVENLY_TAG   = blockTag("needs_heavenly_tool");
    public static final TagKey<Block> LEGENDARY_TAG  = blockTag("needs_legendary_tool");
    public static final TagKey<Block> MYTHICAL_TAG   = blockTag("needs_mythical_tool");
    public static final TagKey<Block> GODLY_TAG      = blockTag("needs_godly_tool");
    public static final TagKey<Block> SUPREME_TAG    = blockTag("needs_supreme_tool");
    public static final TagKey<Block> ULTIMATE_TAG   = blockTag("needs_ultimate_tool");
    public static final TagKey<Block> INFINITY_TAG   = blockTag("needs_infinity_tool");

    public static final Tier OBSIDIAN  = registerTierBetween("obsidian", 3, OBSIDIAN_TAG, Tiers.DIAMOND, Tiers.NETHERITE);
    public static final Tier DURANITE  = registerTier("duranite",  5,  DURANITE_TAG,  Tiers.NETHERITE);
    public static final Tier VALYRIUM  = registerTier("valyrium",  6,  VALYRIUM_TAG,  DURANITE);
    public static final Tier VIBRANIUM = registerTier("vibranium", 7,  VIBRANIUM_TAG, VALYRIUM);
    public static final Tier HEAVENLY  = registerTier("heavenly",  8,  HEAVENLY_TAG,  VIBRANIUM);
    public static final Tier LEGENDARY = registerTier("legendary", 9,  LEGENDARY_TAG, HEAVENLY);
    public static final Tier MYTHICAL  = registerTier("mythical",  10, MYTHICAL_TAG,  LEGENDARY);
    public static final Tier GODLY     = registerTier("godly",     11, GODLY_TAG,     MYTHICAL);
    public static final Tier SUPREME   = registerTier("supreme",   12, SUPREME_TAG,   GODLY);
    public static final Tier ULTIMATE  = registerTier("ultimate",  13, ULTIMATE_TAG,  SUPREME);
    public static final Tier INFINITY  = registerTier("infinity",  127, INFINITY_TAG, ULTIMATE);

    private SoaTiers() {}

    public static void bootstrap() {}

    private static Tier registerTier(String name, int level, TagKey<Block> tag, Tier after) {
        return TierSortingRegistry.registerTier(
                new ForgeTier(level, 0, 0, 0, 0, tag, () -> Ingredient.EMPTY),
                modLoc(name),
                List.of(after),
                List.of());
    }

    private static Tier registerTierBetween(String name, int level, TagKey<Block> tag, Tier after, Tier before) {
        return TierSortingRegistry.registerTier(
                new ForgeTier(level, 0, 0, 0, 0, tag, () -> Ingredient.EMPTY),
                modLoc(name),
                List.of(after),
                List.of(before));
    }

    private static TagKey<Block> blockTag(String path) {
        return BlockTags.create(modLoc(path));
    }

    private static ResourceLocation modLoc(String path) {
        return new ResourceLocation(SoaAdditions.MODID, path);
    }
}
