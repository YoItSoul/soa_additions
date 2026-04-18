package com.soul.soa_additions.tconstructevo.modifier.core;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Cascading — when the broken block is a {@link FallingBlock} (gravel, sand,
 * concrete powder), mines the entire contiguous vertical column of the same
 * state. Re-entry guard prevents the extra breaks from recursively triggering
 * the trait again on each sub-break.
 */
public class CascadingModifier extends Modifier implements BlockBreakModifierHook {

    private static final Set<UUID> PROCESSING = ConcurrentHashMap.newKeySet();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (!context.isEffective() || context.isAOE()) return;
        ServerPlayer player = context.getPlayer();
        if (player == null) return;
        BlockState state = context.getState();
        if (!(state.getBlock() instanceof FallingBlock)) return;

        UUID key = player.getUUID();
        if (!PROCESSING.add(key)) return;
        try {
            ServerLevel world = context.getWorld();
            BlockPos origin = context.getPos();
            walkColumn(tool, context, world, state, origin, true);
            walkColumn(tool, context, world, state, origin, false);
        } finally {
            PROCESSING.remove(key);
        }
    }

    private static void walkColumn(IToolStackView tool, ToolHarvestContext base, ServerLevel world,
                                   BlockState matchState, BlockPos origin, boolean up) {
        int max = up ? world.getMaxBuildHeight() : world.getMinBuildHeight();
        BlockPos cursor = up ? origin.above() : origin.below();
        while (up ? cursor.getY() < max : cursor.getY() >= max) {
            if (world.getBlockState(cursor) != matchState) return;
            ToolHarvestContext forBlock = base.forPosition(cursor, matchState);
            ToolHarvestLogic.breakExtraBlock(tool, base.getPlayer().getMainHandItem(), forBlock);
            cursor = up ? cursor.above() : cursor.below();
        }
    }
}
