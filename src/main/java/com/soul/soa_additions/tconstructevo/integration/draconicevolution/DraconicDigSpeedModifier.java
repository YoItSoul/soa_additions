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
 * Tiered Draconic modifier that multiplies mining speed. The 1.12.2 version
 * added {@code tier × baseSpeed} outright; the 1.20.1 equivalent here is a
 * flat +100%/level on the percent slot, giving final speed =
 * {@code base × (1 + level)}.
 */
public class DraconicDigSpeedModifier extends Modifier implements ToolStatsModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        ToolStats.MINING_SPEED.percent(builder, 1.0D * modifier.getLevel());
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
