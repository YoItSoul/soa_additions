package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Superknockback — on critical hit, push the target much harder.
 * GC computed: newKnockBack * 10 + 20. We approximate by adding a
 * substantial extra impulse along the attacker→target vector. The 25%
 * always-crit branch from GC isn't reproducible without TC3 calcCrit;
 * this hook only fires when vanilla determines it's a crit.
 */
public class SuperknockbackModifier extends Modifier implements MeleeHitModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float dmg) {
        if (!ctx.isCritical()) return;
        LivingEntity target = ctx.getLivingTarget();
        if (target == null) return;
        Vec3 dir = target.position().subtract(ctx.getAttacker().position()).normalize();
        target.knockback(2.0F, -dir.x, -dir.z);
    }
}
