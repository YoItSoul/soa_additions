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

/**
 * Vision — Night Vision while a tool is held (level 0, 330 ticks)
 * OR while armor is worn (level 2 / amplified, 330 ticks per GC).
 */
public class VisionModifier extends Modifier implements InventoryTickModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry mod, Level level, LivingEntity holder,
                                int slot, boolean selected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide()) return;
        boolean isArmor = slot >= 36 && slot <= 39;
        if (selected) {
            holder.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 330, 0, false, false));
        } else if (isArmor) {
            holder.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 330, 2, false, false));
        }
    }
}
