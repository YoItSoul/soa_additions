package com.soul.soa_additions.tconstructevo.modifier.core;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Corrupting — applies Wither that stacks in amplifier on repeated hits, up
 * to a per-level cap.
 */
public class CorruptingModifier extends Modifier implements MeleeHitModifierHook {

    private static final int DURATION_TICKS = 80;
    private static final int MAX_AMPLIFIER_PER_LEVEL = 2;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0.0F) {
            return;
        }
        LivingEntity living = context.getLivingTarget();
        if (living == null || living.level().isClientSide || !living.isAlive()) {
            return;
        }
        int cap = (int) (MAX_AMPLIFIER_PER_LEVEL * modifier.getEffectiveLevel());
        MobEffectInstance existing = living.getEffect(MobEffects.WITHER);
        int nextAmp = existing != null ? Math.min(existing.getAmplifier() + 1, cap) : 0;
        living.addEffect(new MobEffectInstance(MobEffects.WITHER, DURATION_TICKS, nextAmp));
    }
}
