package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "fracture": chain-break line of same-mineable blocks along facing axis, durability-weighted. */
public class FractureModifier extends Modifier implements BlockBreakModifierHook {
    private static final ThreadLocal<Boolean> REENTRY = ThreadLocal.withInitial(() -> false);
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.BLOCK_BREAK); }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE() || REENTRY.get()) return;
        if (!context.isEffective()) return;
        ServerPlayer player = context.getPlayer();
        if (player == null) return;

        int max = tool.getStats().getInt(slimeknights.tconstruct.library.tools.stat.ToolStats.DURABILITY);
        int cur = tool.getCurrentDurability();
        if (max <= 50) return;
        float bonus = 0.99F * (0.4F / (max - 50) * cur + 0.55F);
        if (rng.nextFloat() > bonus) return;

        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult bhit)) return;
        Direction dir = bhit.getDirection();

        ServerLevel world = context.getWorld();
        BlockPos origin = context.getPos();
        int length = rng.nextInt(9) + 1;

        REENTRY.set(true);
        try {
            for (int i = 1; i <= length; i++) {
                BlockPos next = origin.relative(dir.getOpposite(), i);
                BlockState ns = world.getBlockState(next);
                if (ns.is(Blocks.BEDROCK) || ns.isAir()) continue;
                if (!player.getMainHandItem().isCorrectToolForDrops(ns)) continue;
                ToolHarvestContext forBlock = context.forPosition(next, ns);
                ToolHarvestLogic.breakExtraBlock(tool, player.getMainHandItem(), forBlock);
            }
        } finally {
            REENTRY.set(false);
        }
    }
}
