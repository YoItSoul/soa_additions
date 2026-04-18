package com.soul.soa_additions.tconstructevo.modifier.core;

import com.soul.soa_additions.tconstructevo.TConEvoPotions;
import net.minecraft.world.effect.MobEffect;
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
 * Battle Furor — each successful melee hit stacks TConEvo's Damage Boost
 * effect on the attacker, up to a per-level cap.
 */
public class BattleFurorModifier extends Modifier implements MeleeHitModifierHook {

    private static final int DURATION_TICKS = 100;
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
        LivingEntity attacker = context.getAttacker();
        if (attacker == null || attacker.level().isClientSide) {
            return;
        }
        MobEffect boost = TConEvoPotions.DAMAGE_BOOST.get();
        int cap = (int) (MAX_AMPLIFIER_PER_LEVEL * modifier.getEffectiveLevel());
        MobEffectInstance existing = attacker.getEffect(boost);
        int nextAmp = existing != null ? Math.min(existing.getAmplifier() + 1, cap) : 0;
        attacker.addEffect(new MobEffectInstance(boost, DURATION_TICKS, nextAmp));
    }
}
