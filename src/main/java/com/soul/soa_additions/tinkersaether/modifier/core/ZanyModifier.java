package com.soul.soa_additions.tinkersaether.modifier.core;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

/**
 * tinkersaether "Zany": entropy-powered speed. Mining speed scales up as the
 * tool wears out. At full durability the bonus is zero, at zero durability
 * the speed is doubled. Mirrors the original 1.12 formula
 * {@code 1 + (1 - cur/max)} with a +1 floor at full HP.
 */
public class ZanyModifier extends Modifier implements BreakSpeedModifierHook {

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.BREAK_SPEED);
    }

    @Override
    public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier,
                             net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event,
                             net.minecraft.core.Direction sideHit, boolean isEffective, float miningSpeedModifier) {
        if (!isEffective) return;
        int max = tool.getStats().getInt(ToolStats.DURABILITY);
        if (max <= 0) return;
        int cur = tool.getCurrentDurability();
        float bonus = 1.0F + (1.0F - cur / (float) max);
        event.setNewSpeed(event.getNewSpeed() * bonus);
    }
}
