package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

/**
 * Draconic Arrow Speed — trait of Wyvern/Draconic bow materials. Adds a
 * flat velocity boost per level. In 1.12.2 this was a multiplier on the
 * entity's motion; on 1.20.1 we express it through the {@code velocity}
 * tool stat, which is the same knob vanilla Tinkers' ranged logic reads
 * when spawning the arrow.
 */
public class DraconicArrowSpeedModifier extends Modifier implements ToolStatsModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        float bonus = (float) (TConEvoConfig.DRACONIC_ARROW_SPEED_BONUS.get() * modifier.getLevel());
        if (bonus > 0) {
            ToolStats.VELOCITY.add(builder, bonus);
        }
    }

    @Override
    public int getPriority() {
        return 21;
    }
}
