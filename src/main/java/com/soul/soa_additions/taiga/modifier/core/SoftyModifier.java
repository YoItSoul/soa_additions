package com.soul.soa_additions.taiga.modifier.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "softy": +30% mining speed on soft blocks (hardness <= 1). */
public class SoftyModifier extends Modifier implements BreakSpeedModifierHook {

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.BREAK_SPEED); }

    @Override
    public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier,
                             net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event,
                             net.minecraft.core.Direction sideHit, boolean isEffective, float miningSpeedModifier) {
        var player = event.getEntity();
        var world = player.level();
        BlockPos pos = event.getPosition().orElse(player.blockPosition());
        BlockState state = world.getBlockState(pos);
        if (state.getDestroySpeed(world, pos) <= 1.0F) {
            event.setNewSpeed(event.getOriginalSpeed() * 1.3F);
        }
    }
}
