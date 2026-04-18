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
 * Sundering — applies Weakness to the target on hit. Duration scales with
 * the modifier's effective level.
 */
public class SunderingModifier extends Modifier implements MeleeHitModifierHook {

    private static final int DURATION_PER_LEVEL_TICKS = 60;

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
        int duration = (int) (DURATION_PER_LEVEL_TICKS * modifier.getEffectiveLevel());
        living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
    }
}
