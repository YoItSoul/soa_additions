package com.soul.soa_additions.tinkersaether.modifier.core;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * tinkersaether "Cushy": fall damage taken with the tool/armor equipped is
 * reduced to zero. Aerclouds are squishy. Hooks {@link
 * ModifyDamageModifierHook} so it works regardless of which slot the cushy
 * piece is in.
 */
public class CushyModifier extends Modifier implements ModifyDamageModifierHook {

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.MODIFY_HURT);
    }

    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slot,
                                   DamageSource source, float amount, boolean isDirectDamage) {
        if (source.is(DamageTypes.FALL)) return 0.0F;
        return amount;
    }
}
