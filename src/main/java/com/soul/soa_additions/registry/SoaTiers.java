package com.soul.soa_additions.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import se.mickelus.tetra.TetraRegistries;

import java.util.List;

public class SoaTiers {

    public static final TagKey<Block> INFERNIUM_TAG = BlockTags.create(new ResourceLocation("soa_additions:needs_infernium_tool"));
    public static final TagKey<Block> VOID_TAG = BlockTags.create(new ResourceLocation("soa_additions:needs_void_tool"));
    public static final TagKey<Block> ABYSSAL_TAG = BlockTags.create(new ResourceLocation("soa_additions:needs_abyssal_tool"));
    public static final TagKey<Block> ETHER_TAG = BlockTags.create(new ResourceLocation("soa_additions:needs_ether_tool"));

    public static void init() {
        Tier inferniumTier = TierSortingRegistry.registerTier(new ForgeTier(5, 0, 0, 0, 0,
                INFERNIUM_TAG, () -> Ingredient.EMPTY), new ResourceLocation("soa_additions:infernium"), List.of(Tiers.NETHERITE), List.of());


        Tier voidTier = TierSortingRegistry.registerTier(new ForgeTier(6, 0, 0, 0, 0,
                VOID_TAG, () -> Ingredient.EMPTY), new ResourceLocation("soa_additions:void"), List.of(inferniumTier), List.of());


        Tier abyssalTier = TierSortingRegistry.registerTier(new ForgeTier(7, 0, 0, 0, 0,
                ABYSSAL_TAG, () -> Ingredient.EMPTY), new ResourceLocation("soa_additions:abyssal"), List.of(voidTier), List.of());


        Tier etherTier = TierSortingRegistry.registerTier(new ForgeTier(8, 0, 0, 0, 0,
                ETHER_TAG, () -> Ingredient.EMPTY), new ResourceLocation("soa_additions:ether"), List.of(abyssalTier), List.of());
    }
}
