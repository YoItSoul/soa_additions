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
 * Tiered Draconic modifier that slams bow draw speed. 1.12.2 used a cubic
 * scaler ({@code tier³ × baseDrawSpeed × 0.25}) to make each level absurdly
 * stronger than the last; we preserve that flavour by feeding the same
 * {@code level³ × 0.25} factor into the percent slot of the draw-speed stat.
 * Result: level 1 → +25%, level 2 → +200%, level 3 → +675%, level 4 → +1600%.
 */
public class DraconicDrawSpeedModifier extends Modifier implements ToolStatsModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        int level = modifier.getLevel();
        double factor = (double) level * level * level * 0.25D;
        ToolStats.DRAW_SPEED.percent(builder, factor);
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
