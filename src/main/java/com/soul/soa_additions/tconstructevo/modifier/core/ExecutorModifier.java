package com.soul.soa_additions.tconstructevo.modifier.core;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Executor — bonus melee damage scaled by how much HP the target is missing.
 * Mirrors the 1.12.2 behaviour: deals up to +100% damage at 1 HP, scaling
 * linearly with missing health, multiplied by 0.2 per level.
 */
public class ExecutorModifier extends Modifier implements MeleeDamageModifierHook {

    private static final float PERCENT_PER_LEVEL = 0.20F;

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
        float max = living.getMaxHealth();
        if (max <= 0.0F) {
            return damage;
        }
        float missingFraction = 1.0F - (living.getHealth() / max);
        float bonus = missingFraction * PERCENT_PER_LEVEL * modifier.getEffectiveLevel();
        return damage * (1.0F + bonus);
    }
}
