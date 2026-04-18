package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

/**
 * Tiered Draconic modifier that grants +25%/level to attack damage via the
 * {@code percent} slot of TConstruct's stat accumulator. In 1.12.2 the modifier
 * added {@code tier × baseAttack / 4} directly; on 1.20.1 the same ratio is
 * expressed against base stats through the percent multiplier — final damage
 * becomes {@code base × (1 + 0.25 × level)}.
 */
public class DraconicAttackDamageModifier extends Modifier implements ToolStatsModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        ToolStats.ATTACK_DAMAGE.percent(builder, 0.25D * modifier.getLevel());
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
