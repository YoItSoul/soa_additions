package com.soul.soa_additions.tinkersaether.modifier.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * tinkersaether "Antigrav": shift-right-click an effective block to push it
 * upward by one (gravity-defying lift). Costs 4 durability per use. Original
 * 1.12 implementation moved the block as if it floated for ~5 seconds; we
 * port the simpler 1-block teleport-up since that's what end users actually
 * see in normal play.
 */
public class AntigravModifier extends Modifier implements BlockInteractionModifierHook {

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.BLOCK_INTERACT);
    }

    @Override
    public InteractionResult afterBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext ctx, InteractionSource source) {
        Player player = ctx.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockPos above = pos.above();
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return InteractionResult.PASS;
        if (state.getDestroySpeed(level, pos) < 0) return InteractionResult.PASS; // unbreakable
        if (!level.getBlockState(above).canBeReplaced()) return InteractionResult.PASS;
        level.setBlockAndUpdate(above, state);
        level.removeBlock(pos, false);
        ToolDamageUtil.damage(tool, 4, player, ctx.getItemInHand());
        return InteractionResult.SUCCESS;
    }
}
