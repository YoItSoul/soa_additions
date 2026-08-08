package com.soul.soa_additions.oresight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

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

    /** {@code forge:ores/netherite_scrap} — checked directly because it is the
     *  only vanilla ore whose id carries no {@code _ore} marker, so it has no
     *  fallback if a pack datapack overrides {@code forge:ores}. */
    public static final TagKey<Block> FORGE_ORES_NETHERITE_SCRAP =
            BlockTags.create(new ResourceLocation("forge", "ores/netherite_scrap"));

    /**
     * Ore blocks that neither carry {@code _ore} in their registry path nor can
     * be relied on to survive a pack's {@code forge:ores} override. Ancient
     * debris is the vanilla case; add modded strays here as they turn up.
     */
    private static final Set<ResourceLocation> EXTRA_ORES = Set.of(
            new ResourceLocation("minecraft", "ancient_debris"));

    private OreSight() {}

    /**
     * True iff this block qualifies as an ore for the ore-sight system —
     * tagged {@code forge:ores} (or {@code forge:ores/netherite_scrap}), listed
     * in {@link #EXTRA_ORES}, or its registry path contains {@code _ore}.
     */
    public static boolean isOreBlock(Block block) {
        if (block == null || block == Blocks.AIR) return false;
        BlockState state = block.defaultBlockState();
        if (state.is(FORGE_ORES) || state.is(FORGE_ORES_NETHERITE_SCRAP)) return true;
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        if (id == null) return false;
        return EXTRA_ORES.contains(id) || id.getPath().contains("_ore");
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
