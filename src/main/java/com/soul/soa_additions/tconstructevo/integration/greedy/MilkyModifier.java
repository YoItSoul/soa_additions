package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Milky — periodically clear all status effects.
 *   - tool slot (mainhand selected): every 12000 ticks (one MC day)
 *   - armor slot:                    every 18000 ticks (matches GC armor cadence)
 */
public class MilkyModifier extends Modifier implements InventoryTickModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry mod, Level level, LivingEntity holder,
                                int slot, boolean selected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide()) return;
        long t = level.getGameTime();
        boolean isArmor = slot >= 36 && slot <= 39;
        if (selected && t % 12000L == 0L) { holder.removeAllEffects(); return; }
        if (isArmor && t % 18000L == 0L) { holder.removeAllEffects(); }
    }
}
