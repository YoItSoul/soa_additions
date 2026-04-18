package com.soul.soa_additions.tconstructevo.modifier.core;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

/**
 * Crystalline — bonus damage that scales with how much durability the tool
 * has remaining. A freshly repaired tool hits the hardest; a nearly broken
 * tool loses almost the entire bonus. Encourages upkeep.
 */
public class CrystallineModifier extends Modifier implements MeleeDamageModifierHook {

    private static final float MAX_BONUS_PER_LEVEL = 0.50F;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        float max = tool.getStats().get(ToolStats.DURABILITY);
        if (max <= 0.0F) {
            return damage;
        }
        float fraction = tool.getCurrentDurability() / max;
        float bonus = fraction * MAX_BONUS_PER_LEVEL * modifier.getEffectiveLevel();
        return damage + baseDamage * bonus;
    }
}
