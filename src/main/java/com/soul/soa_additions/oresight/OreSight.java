package com.soul.soa_additions.oresight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Static helpers for the ore-sight system.
 *
 * <p>An "ore-sight" potion is brewed with a {@link BlockItem} whose block is
 * an ore (per {@link #isOreBlock}). The potion stores that block's id, and
 * later highlights <i>that exact block</i> when the player drinks it. Each
 * variant — {@code gold_ore}, {@code deepslate_gold_ore}, {@code tiberium_ore}
 * — is its own potion. No loot-table reverse-lookup, no tag expansion, no
 * smelting-recipe inference; the input is the answer.</p>
 */
public final class OreSight {

    /** Forge's standard ores block tag. */
    public static final TagKey<Block> FORGE_ORES = BlockTags.create(new ResourceLocation("forge", "ores"));

    private OreSight() {}

    /**
     * True iff this block qualifies as an ore for the ore-sight system —
     * either tagged {@code forge:ores} or its registry path contains
     * {@code _ore}.
     */
    public static boolean isOreBlock(Block block) {
        if (block == null || block == Blocks.AIR) return false;
        if (block.defaultBlockState().is(FORGE_ORES)) return true;
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        return id != null && id.getPath().contains("_ore");
    }

    /**
     * Resolve a brewing ingredient to the ore block it represents.
     * Accepts a {@link BlockItem} whose block is an ore; everything else
     * returns null.
     */
    public static Block oreFromIngredient(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (!(stack.getItem() instanceof BlockItem bi)) return null;
        Block block = bi.getBlock();
        return isOreBlock(block) ? block : null;
    }

    /** Player position center for the scan-radius helpers. */
    public static Vec3 playerPos(Player player) {
        return new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
    }
}
