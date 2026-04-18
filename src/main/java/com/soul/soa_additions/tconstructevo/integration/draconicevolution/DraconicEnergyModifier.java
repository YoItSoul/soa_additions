package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.capability.ToolEnergyCapability;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

/**
 * Draconic Energy — pure RF capacity expander that stacks on top of an
 * existing Evolved / Energized / Fluxed pool. In 1.12.2 each level doubled
 * the Evolved capacity; we preserve that multiplicative flavour by adding
 * {@code capacity × (2^level)} on top of the current {@code MAX_STAT}.
 * Discharge behaviour remains whatever trait owns the base pool.
 */
public class DraconicEnergyModifier extends Modifier implements ToolStatsModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        int perLevel = TConEvoConfig.DRACONIC_ENERGY_CAPACITY_PER_LEVEL.get();
        int level = Math.max(modifier.getLevel(), 1);
        long capacity = (long) perLevel << Math.min(level - 1, 16);
        ToolEnergyCapability.MAX_STAT.add(builder, (double) Math.min(capacity, Integer.MAX_VALUE));
    }

    @Override
    public int getPriority() {
        return 22;
    }
}
