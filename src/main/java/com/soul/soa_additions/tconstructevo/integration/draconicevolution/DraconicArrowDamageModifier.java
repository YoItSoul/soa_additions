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
 * Tiered Draconic modifier granting +25%/level to projectile damage. Parallels
 * the attack-damage variant: 1.12.2 added {@code tier × baseProjectile / 4},
 * here expressed as a percent multiplier on the projectile damage stat.
 */
public class DraconicArrowDamageModifier extends Modifier implements ToolStatsModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        ToolStats.PROJECTILE_DAMAGE.percent(builder, 0.25D * modifier.getLevel());
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
