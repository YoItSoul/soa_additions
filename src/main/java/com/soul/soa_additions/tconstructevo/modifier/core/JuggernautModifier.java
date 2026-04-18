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
 * Juggernaut — bonus melee damage that grows with the attacker's missing HP.
 * In 1.12.2 this trait stacked a temporary damage buff each time the wielder
 * was hurt; 1.20.1's combat timing made that approach ugly (hook ordering +
 * attribute churn). Using missing-HP as the driver produces a very similar
 * "angrier as you get hurt" feel without extra persistent state.
 */
public class JuggernautModifier extends Modifier implements MeleeDamageModifierHook {

    private static final float PERCENT_PER_LEVEL = 0.50F;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity attacker = context.getAttacker();
        if (attacker == null) {
            return damage;
        }
        float max = attacker.getMaxHealth();
        if (max <= 0.0F) {
            return damage;
        }
        float missingFraction = 1.0F - (attacker.getHealth() / max);
        float bonus = missingFraction * PERCENT_PER_LEVEL * modifier.getEffectiveLevel();
        return damage * (1.0F + bonus);
    }
}
