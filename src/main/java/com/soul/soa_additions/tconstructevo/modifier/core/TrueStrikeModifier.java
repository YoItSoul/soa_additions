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
 * True Strike — applies TConEvo's True Strike effect to the attacker for a
 * handful of ticks. Downstream hooks read the effect as a "bypass armour"
 * flag for the very next hit.
 */
public class TrueStrikeModifier extends Modifier implements MeleeHitModifierHook {

    private static final int DURATION_TICKS = 6;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity attacker = context.getAttacker();
        if (attacker == null || attacker.level().isClientSide) {
            return;
        }
        attacker.addEffect(new MobEffectInstance(TConEvoPotions.TRUE_STRIKE.get(), DURATION_TICKS, 0, false, false));
    }
}
