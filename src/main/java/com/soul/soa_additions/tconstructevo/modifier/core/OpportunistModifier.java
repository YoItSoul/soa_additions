package com.soul.soa_additions.tconstructevo.modifier.core;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Opportunist — extra damage against targets afflicted with any harmful
 * potion effect. Matches tconevo's behaviour of kicking a foe while they're
 * already debuffed.
 */
public class OpportunistModifier extends Modifier implements MeleeDamageModifierHook {

    private static final float BONUS_PER_LEVEL = 0.30F;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity living = context.getLivingTarget();
        if (living == null) {
            return damage;
        }
        for (MobEffectInstance effect : living.getActiveEffects()) {
            if (!effect.getEffect().isBeneficial()) {
                float bonus = BONUS_PER_LEVEL * modifier.getEffectiveLevel();
                return damage + baseDamage * bonus;
            }
        }
        return damage;
    }
}
