package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "cascade": 10% chain-break adjacent same-type blocks, up to 50 blocks. */
public class CascadeModifier extends Modifier implements BlockBreakModifierHook {
    private static final ThreadLocal<Boolean> REENTRY = ThreadLocal.withInitial(() -> false);
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.BLOCK_BREAK); }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE() || REENTRY.get()) return;
        ServerPlayer player = context.getPlayer();
        if (player == null) return;
        if (rng.nextFloat() > 0.1F) return;
        BlockState matchState = context.getState();
        ServerLevel world = context.getWorld();
        BlockPos origin = context.getPos();

        int max = tool.getStats().getInt(slimeknights.tconstruct.library.tools.stat.ToolStats.DURABILITY);
        int cur = tool.getCurrentDurability();
        int cap = Math.min((int) (300.0F * (float) cur / Math.max(max, 1)), 50);
        if (cap <= 0) return;
        int count = rng.nextInt(cap);
        REENTRY.set(true);
        try {
            double[] cursor = {origin.getX(), origin.getY(), origin.getZ()};
            double[] saved = {origin.getX(), origin.getY(), origin.getZ()};
            for (int i = 0; i < count; i++) {
                int axis = rng.nextInt(3);
                int delta = rng.nextBoolean() ? 1 : -1;
                double[] next = cursor.clone();
                next[axis] += delta;
                BlockPos nPos = new BlockPos((int) next[0], (int) next[1], (int) next[2]);
                if (world.getBlockState(nPos) == matchState) {
                    ToolHarvestContext forBlock = context.forPosition(nPos, matchState);
                    ToolHarvestLogic.breakExtraBlock(tool, player.getMainHandItem(), forBlock);
                    cursor = next;
                    saved = cursor.clone();
                } else {
                    cursor = saved.clone();
                }
            }
        } finally {
            REENTRY.set(false);
        }
    }
}
