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
 * Foot Fleet — grants the attacker a short Speed II burst after every
 * successful hit. Keeps pressure on the target while chaining attacks.
 */
public class FootFleetModifier extends Modifier implements MeleeHitModifierHook {

    private static final int DURATION_TICKS = 60;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0.0F) {
            return;
        }
        LivingEntity attacker = context.getAttacker();
        if (attacker == null || attacker.level().isClientSide) {
            return;
        }
        int amplifier = Math.max(0, (int) modifier.getEffectiveLevel() - 1) + 1;
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_TICKS, amplifier));
    }
}
