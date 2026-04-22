package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * TAIGA "decay": slow passive degradation of the tool. 1.12 drained from a
 * randomized stat pool; TC3 tools don't have mutable attack/speed stats the
 * same way, so the port applies a small durability drain every {@value #TICK_PER_STAT}
 * ticks instead. Keeps the "this tool is rotting" flavour.
 */
public class DecayModifier extends Modifier implements InventoryTickModifierHook {
    private static final int TICK_PER_STAT = 24;
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.INVENTORY_TICK); }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder,
                                int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (world.isClientSide) return;
        if (holder.tickCount % TICK_PER_STAT != 0) return;
        if (((ToolStack) tool).isBroken()) return;
        ToolDamageUtil.damageAnimated((ToolStack) tool, rng.nextInt(1) + 1, holder);
    }
}
