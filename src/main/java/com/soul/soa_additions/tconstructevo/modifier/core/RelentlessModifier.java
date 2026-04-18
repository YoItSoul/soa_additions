package com.soul.soa_additions.tconstructevo.modifier.core;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Relentless — reduces the target's post-hit invulnerability window so
 * rapid follow-up hits can land through vanilla's 10-tick invuln. Per-level
 * tick reduction keeps the drop-off tunable.
 */
public class RelentlessModifier extends Modifier implements MeleeHitModifierHook {

    private static final int TICKS_PER_LEVEL = 4;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity living = context.getLivingTarget();
        if (living == null) {
            return;
        }
        int reduction = (int) (TICKS_PER_LEVEL * modifier.getEffectiveLevel());
        living.invulnerableTime = Math.max(0, living.invulnerableTime - reduction);
    }
}
