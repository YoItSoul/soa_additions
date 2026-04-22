package com.soul.soa_additions.taiga.modifier.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * TAIGA "superheavy": mining speed drops as more blocks are broken, stored in
 * persistent tool NBT. The counter caps implicitly at (speed / 500) so very
 * high block counts clamp to 0.35× min speed.
 */
public class SuperHeavyModifier extends Modifier implements BlockBreakModifierHook, BreakSpeedModifierHook {
    private static final ResourceLocation KEY = new ResourceLocation("taiga", "superheavy_brokenblocks");

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.BLOCK_BREAK, ModifierHooks.BREAK_SPEED);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE()) return;
        CompoundTag data = ((ToolStack) tool).getPersistentData().getCompound(KEY);
        int broken = data.getInt("count") + 1;
        CompoundTag out = new CompoundTag();
        out.putInt("count", broken);
        ((ToolStack) tool).getPersistentData().put(KEY, out);
    }

    @Override
    public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier,
                             net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event,
                             net.minecraft.core.Direction sideHit, boolean isEffective, float miningSpeedModifier) {
        CompoundTag data = ((ToolStack) tool).getPersistentData().getCompound(KEY);
        int broken = data.getInt("count");
        if (broken <= 0) return;
        float penalty = broken * event.getOriginalSpeed() / 500.0F;
        event.setNewSpeed(Math.max(event.getNewSpeed() - penalty, 0.35F));
    }
}
