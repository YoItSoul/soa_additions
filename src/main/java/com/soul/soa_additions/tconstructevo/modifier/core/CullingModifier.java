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
 * Culling — flat bonus damage against baby mobs. Historic value was +100%
 * damage; here we scale per level so multi-level traits feel meaningful
 * while single-level traits still line up with the original tconevo feel.
 */
public class CullingModifier extends Modifier implements MeleeDamageModifierHook {

    private static final float PERCENT_PER_LEVEL = 1.00F;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity living = context.getLivingTarget();
        if (living == null || !living.isBaby()) {
            return damage;
        }
        float bonus = PERCENT_PER_LEVEL * modifier.getEffectiveLevel();
        return damage * (1.0F + bonus);
    }
}
