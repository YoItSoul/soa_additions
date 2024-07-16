package com.soul.soa_additions.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import se.mickelus.tetra.TetraRegistries;

import java.util.List;

public class SoaTiers {

    public static void init() {
        TagKey<Block> inferniumTag = BlockTags.create(new ResourceLocation("soa_additions:needs_infernium_tool"));
        Tier inferniumTier = TierSortingRegistry.registerTier(new ForgeTier(5, 0, 0, 0, 0,
                inferniumTag, () -> Ingredient.EMPTY), new ResourceLocation("soa_additions:infernium"), List.of(TetraRegistries.forgeHammerTier), List.of());

        TagKey<Block> voidTag = BlockTags.create(new ResourceLocation("soa_additions:needs_void_tool"));
        Tier voidTier = TierSortingRegistry.registerTier(new ForgeTier(6, 0, 0, 0, 0,
                voidTag, () -> Ingredient.EMPTY), new ResourceLocation("soa_additions:void"), List.of(inferniumTag), List.of());

        TagKey<Block> abyssalTag = BlockTags.create(new ResourceLocation("soa_additions:needs_abyssal_tool"));
        Tier abyssalTier = TierSortingRegistry.registerTier(new ForgeTier(7, 0, 0, 0, 0,
                abyssalTag, () -> Ingredient.EMPTY), new ResourceLocation("soa_additions:abyssal"), List.of(voidTier), List.of());

        TagKey<Block> etherTag = BlockTags.create(new ResourceLocation("soa_additions:needs_ether_tool"));
        Tier etherTier = TierSortingRegistry.registerTier(new ForgeTier(8, 0, 0, 0, 0,
                etherTag, () -> Ingredient.EMPTY), new ResourceLocation("tetranomicon:ether"), List.of(abyssalTier), List.of());
    }
}
