package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Throny — tool: reflect 10% maxHP per hit. armor: 2.5% damage thorns (cap 10) per incoming hit. */
public class ThronyModifier extends Modifier implements MeleeHitModifierHook, OnAttackedModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.ON_ATTACKED);
    }
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float dmg) {
        LivingEntity target = ctx.getLivingTarget();
        if (target == null) return;
        LivingEntity attacker = ctx.getAttacker();
        target.hurt(attacker.damageSources().thorns(attacker), attacker.getMaxHealth() * 0.10F);
    }
    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                           EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        if (!(src.getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity entity = ctx.getEntity();
        float refl = Math.min(10.0F, dmg * 0.025F);
        attacker.hurt(entity.damageSources().thorns(entity), refl);
    }
}
