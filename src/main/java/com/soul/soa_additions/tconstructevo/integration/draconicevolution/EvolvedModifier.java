package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.capability.ToolEnergyCapability;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

/**
 * Evolved — signature trait of Awakened Draconium. 1.12.2 tconevo used this
 * as a complex "tool is supernaturally resilient while you have RF, garbage
 * without" mechanic spread across six combat/mining hooks. On 1.20.1 we ship
 * the simplified-but-equivalent read: the tool gains a large RF pool whose
 * capacity doubles per level (tier), and every point of durability damage is
 * paid out of that pool. When the pool is empty, normal durability loss
 * resumes. Mining-speed / projectile-velocity degradation are intentionally
 * dropped — the RF drain already gates sustained use.
 */
public class EvolvedModifier extends Modifier implements ToolStatsModifierHook, ToolDamageModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS, ModifierHooks.TOOL_DAMAGE);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        int baseCap = TConEvoConfig.EVOLVED_BASE_RF_CAPACITY.get();
        int level = Math.max(modifier.getLevel(), 1);
        long capacity = (long) baseCap << Math.min(level - 1, 16);
        ToolEnergyCapability.MAX_STAT.add(builder, (double) Math.min(capacity, Integer.MAX_VALUE));
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
        if (amount <= 0) return amount;
        int unitCost = TConEvoConfig.EVOLVED_ENERGY_COST_TOOLS.get();
        if (unitCost <= 0) return amount;
        int energy = ToolEnergyCapability.getEnergy(tool);
        if (energy <= 0) return amount;

        int cost = amount * unitCost;
        int spent = Math.min(cost, energy);
        ToolEnergyCapability.setEnergy(tool, energy - spent);
        if (spent >= cost) return 0;
        return Math.max(amount - (int) Math.ceil(amount * ((float) spent / cost)), 0);
    }

    @Override
    public int getPriority() {
        return 23;
    }
}
