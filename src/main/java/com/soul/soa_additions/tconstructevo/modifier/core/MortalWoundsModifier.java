package com.soul.soa_additions.tconstructevo.modifier.core;

import com.soul.soa_additions.tconstructevo.TConEvoPotions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Mortal Wounds — applies TConEvo's Heal Reduction effect to the target so
 * subsequent heals (regen potions, food, etc.) recover less HP.
 */
public class MortalWoundsModifier extends Modifier implements MeleeHitModifierHook {

    private static final int DURATION_PER_LEVEL_TICKS = 140;

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
        living.addEffect(new MobEffectInstance(TConEvoPotions.HEAL_REDUCTION.get(), duration, 0));
    }
}
