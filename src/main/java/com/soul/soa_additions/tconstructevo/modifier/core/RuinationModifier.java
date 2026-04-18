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
 * Ruination — adds flat bonus damage equal to a fraction of the target's
 * current HP. Hits the hardest against full-health bosses but falls off as
 * the target bleeds out.
 */
public class RuinationModifier extends Modifier implements MeleeDamageModifierHook {

    private static final float HEALTH_RATIO_PER_LEVEL = 0.05F;
    private static final float MAX_BONUS = 40.0F;

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
        float bonus = living.getHealth() * HEALTH_RATIO_PER_LEVEL * modifier.getEffectiveLevel();
        if (bonus <= 0.0F) {
            return damage;
        }
        return damage + Math.min(bonus, MAX_BONUS);
    }
}
