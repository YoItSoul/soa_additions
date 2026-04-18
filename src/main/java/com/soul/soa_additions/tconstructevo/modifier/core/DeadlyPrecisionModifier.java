package com.soul.soa_additions.tconstructevo.modifier.core;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Deadly Precision — bonus damage only on critical strikes. Rewards the
 * sprint-jump timing that vanilla already rewards, stacking extra damage on
 * top of the vanilla crit multiplier.
 */
public class DeadlyPrecisionModifier extends Modifier implements MeleeDamageModifierHook {

    private static final float BONUS_PER_LEVEL = 0.50F;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        if (!context.isCritical()) {
            return damage;
        }
        float bonus = BONUS_PER_LEVEL * modifier.getEffectiveLevel();
        return damage + baseDamage * bonus;
    }
}
