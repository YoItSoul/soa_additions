package com.soul.soa_additions.taiga.modifier.core;

import java.util.List;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "pulverizing": mining speed ramps up as durability drops, 60% clear drops. */
public class PulverizingModifier extends Modifier implements BreakSpeedModifierHook, ProcessLootModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.BREAK_SPEED, ModifierHooks.PROCESS_LOOT);
    }

    @Override
    public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier,
                             net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event,
                             net.minecraft.core.Direction sideHit, boolean isEffective, float miningSpeedModifier) {
        if (!isEffective) return;
        int max = tool.getStats().getInt(slimeknights.tconstruct.library.tools.stat.ToolStats.DURABILITY);
        int cur = tool.getCurrentDurability();
        if (max <= 0) return;
        float bonus = 1.0F + 0.9F * (max - cur) / (float) max;
        event.setNewSpeed(event.getNewSpeed() * bonus);
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> drops, LootContext ctx) {
        if (rng.nextFloat() < 0.6F) drops.clear();
    }
}
