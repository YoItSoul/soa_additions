package com.soul.soa_additions.tconstructevo.modifier.core;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * Staggering — on a fully charged melee hit, applies Slowness V for a short
 * duration. Substitutes for the 1.12.2 "rooted" effect (which required the
 * NaturalPledge integration); vanilla Slowness V covers the same gameplay
 * intent of locking the target in place.
 */
public class StaggeringModifier extends Modifier implements MeleeHitModifierHook {

    private static final int DURATION_TICKS = 20;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!context.isFullyCharged() || damageDealt <= 0.0F) {
            return;
        }
        LivingEntity living = context.getLivingTarget();
        if (living == null || living.level().isClientSide || !living.isAlive()) {
            return;
        }
        int duration = DURATION_TICKS + 10 * (int) modifier.getEffectiveLevel();
        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 4));
        living.level().playSound(null, living.getX(), living.getY(), living.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
