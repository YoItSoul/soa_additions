package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Lucky (armor) — Luck I while equipped. */
public class LuckyModifier extends Modifier implements InventoryTickModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry mod, Level level, LivingEntity holder,
                                int slot, boolean selected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide()) return;
        holder.addEffect(new MobEffectInstance(MobEffects.LUCK, 25, 0, false, false));
    }
}
