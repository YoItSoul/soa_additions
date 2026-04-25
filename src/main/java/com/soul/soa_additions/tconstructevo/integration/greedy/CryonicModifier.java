package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Cryonic — 20% chance to slow the attacker (Slowness II, 10s) on incoming hit. */
public class CryonicModifier extends Modifier implements OnAttackedModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }
    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                           EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        if (!(src.getEntity() instanceof LivingEntity attacker)) return;
        if (ctx.getEntity().getRandom().nextFloat() >= 0.2F) return;
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2, false, false));
    }
}
