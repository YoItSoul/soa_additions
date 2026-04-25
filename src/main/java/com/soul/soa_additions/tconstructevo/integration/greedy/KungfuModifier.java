package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.DamageTypeTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Kung Fu — 4% chance to dodge a non-absolute hit, gaining Speed IV (5s).
 * Original also did +12.5% damage from baby attackers; that branch is
 * dropped (negligible practical impact).
 */
public class KungfuModifier extends Modifier implements ModifyDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                                   EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        LivingEntity entity = ctx.getEntity();
        if (entity.level().isClientSide() || src.is(DamageTypeTags.BYPASSES_ARMOR)) return dmg;
        if (entity.getRandom().nextFloat() < 0.04F) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 3, false, false));
            return 0.0F;
        }
        return dmg;
    }
}
