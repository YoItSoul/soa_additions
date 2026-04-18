package com.soul.soa_additions.tconstructevo.modifier.core;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Impact Force — damage scales with the attacker's current velocity. The
 * 1.12.2 version used a custom per-player velocity tracker; in 1.20.1 we can
 * just read {@code getDeltaMovement()} directly. A sprint-jump crit will land
 * meaningfully harder than a standing swing.
 */
public class ImpactForceModifier extends Modifier implements MeleeDamageModifierHook {

    private static final float BONUS_PER_SPEED_PER_LEVEL = 1.0F;

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
        Vec3 move = attacker.getDeltaMovement();
        float speed = (float) move.length();
        if (speed <= 0.0F) {
            return damage;
        }
        float bonus = speed * BONUS_PER_SPEED_PER_LEVEL * modifier.getEffectiveLevel();
        return damage + baseDamage * bonus;
    }
}
