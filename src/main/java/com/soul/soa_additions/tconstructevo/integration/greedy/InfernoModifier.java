package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Inferno — 20% chance to set the attacker on fire for 8s on incoming hit. */
public class InfernoModifier extends Modifier implements OnAttackedModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }
    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                           EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        Entity attacker = src.getEntity();
        if (!(attacker instanceof LivingEntity)) return;
        if (ctx.getEntity().getRandom().nextFloat() >= 0.2F) return;
        attacker.setSecondsOnFire(8);
    }
}
