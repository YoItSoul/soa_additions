package com.soul.soa_additions.tconstructevo.integration.greedy;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Leveling Damage — scales with the TinkersLevellingAddon "leveling" stat.
 * GC's formula read NBT directly; in TC3 1.20.1 the leveling addon stores
 * tool levels as its own modifier, but exposing that to vanilla MELEE_DAMAGE
 * needs the addon's API (not pulled in here). Falls back to a flat +5%
 * per modifier level, capped at +50%.
 */
public class LevelingdamageModifier extends Modifier implements MeleeDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float base, float dmg) {
        float level = mod.getEffectiveLevel();
        float mult = Math.min(1.5F, 1.0F + 0.05F * level);
        return dmg * mult;
    }
}
