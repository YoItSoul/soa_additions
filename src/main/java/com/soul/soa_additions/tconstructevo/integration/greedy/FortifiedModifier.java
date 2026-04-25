package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Fortified — tool: Resistance II 5s after every hit. armor: -15% projectile damage. */
public class FortifiedModifier extends Modifier implements MeleeHitModifierHook, ModifyDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float dmg) {
        LivingEntity a = ctx.getAttacker();
        if (a.level().isClientSide() || !(a instanceof Player)) return;
        a.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99, 1, false, false));
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                                   EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        return src.is(DamageTypeTags.IS_PROJECTILE) ? dmg * 0.85F : dmg;
    }
}
