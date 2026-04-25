package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Second Life — 5% chance to negate a lethal hit, granting Absorption IV
 * + Regen IV + Resistance V. Only fires when damage exceeds current HP
 * but stays under maxHealth (matches GC's safety check).
 */
public class SecondLifeModifier extends Modifier implements ModifyDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                                   EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        LivingEntity entity = ctx.getEntity();
        if (entity.level().isClientSide()) return dmg;
        if (dmg < entity.getMaxHealth() && dmg > entity.getHealth()) {
            if (entity.getRandom().nextFloat() < 0.05F) {
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 3, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 3, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 45, 4, false, false));
                return 0.0F;
            }
        }
        return dmg;
    }
}
