package com.soul.soa_additions.tconstructevo.modifier.core;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Overwhelm — bonus damage proportional to the target's armour value. This
 * is the tconevo anti-armour trait: the higher the target's armour attribute,
 * the more extra damage the attack deals, effectively punishing heavily
 * armoured foes.
 */
public class OverwhelmModifier extends Modifier implements MeleeDamageModifierHook {

    private static final float BONUS_PER_ARMOUR_PER_LEVEL = 0.04F;

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
        float armour = (float) living.getAttributeValue(Attributes.ARMOR);
        if (armour <= 0.0F) {
            return damage;
        }
        float bonus = armour * BONUS_PER_ARMOUR_PER_LEVEL * modifier.getEffectiveLevel();
        return damage + baseDamage * bonus;
    }
}
