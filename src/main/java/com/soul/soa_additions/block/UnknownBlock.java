package com.soul.soa_additions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The stand-in every stage-hidden block is disguised as.
 *
 * <p>{@code scripts/soa_block_replacement.zs} points every staged block-entity
 * block at this one, so a player without the stage sees a featureless block
 * instead of a Draconium Chest or a Forge Controller. sdm_orestages implements
 * that by {@code @Redirect}ing <em>every</em> {@code ServerLevel.getBlockState}
 * call inside {@link net.minecraft.server.level.ServerPlayerGameMode#destroyBlock}
 * through its replacement table, which means vanilla's local {@code blockstate}
 * — the one it hands to {@code canHarvestBlock} and {@code playerDestroy} — is
 * this block rather than the real one. The real block is still what gets removed
 * from the world (that path reads the state separately), but the loot rolled is
 * this block's, so mining a hidden chest used to yield a literal
 * {@code soa_additions:unknown_block} and destroy the chest.</p>
 *
 * <p>Both overrides below recover the real block and defer to it, so breaking a
 * hidden block behaves exactly as breaking the real one would: the real tool
 * requirement applies, and the real loot table rolls with the player's fortune
 * and silk touch. Only the appearance stays hidden.</p>
 *
 * <p>Ore hiding is untouched: {@code scripts/soa_ore_stages.zs} disguises ores
 * as stone/deepslate/netherrack, and dropping the filler is the point there.</p>
 */
public class UnknownBlock extends Block {

    public UnknownBlock(Properties properties) {
        super(properties);
    }

    /**
     * Rolls the real block's loot table instead of this one's.
     *
     * <p>By the time drops are rolled the block is already gone from the world,
     * so the real state has to come from the block entity vanilla captured
     * before removal and passed along as a loot parameter. That is always
     * present for the blocks this stands in for — the replacement list is
     * block-entity blocks only.</p>
     */
    @Override
    // Deprecated only to steer callers to the BlockState overload; overriding it is the intended use.
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        final BlockState real = realState(params);
        return real == null ? super.getDrops(state, params) : real.getDrops(params);
    }

    /**
     * Applies the real block's tool requirement rather than this block's.
     *
     * <p>This runs before the block is removed, so the level still holds the
     * real state — and reading it here is safe because the redirect only covers
     * {@code ServerPlayerGameMode}'s own lookups. On the client the level really
     * does hold this block, so nothing about the real one leaks; the fallback
     * below handles that.</p>
     */
    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        final BlockState real = level.getBlockState(pos);
        if (!real.is(this)) {
            return real.canHarvestBlock(level, pos, player);
        }
        return super.canHarvestBlock(state, level, pos, player);
    }

    /** The hidden block's real state, or null when this really is an unknown block. */
    @Nullable
    private BlockState realState(LootParams.Builder params) {
        final BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity == null) {
            return null;
        }
        final BlockState real = blockEntity.getBlockState();
        return real.is(this) ? null : real;
    }
}
