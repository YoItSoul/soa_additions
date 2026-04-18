package com.soul.soa_additions.tconstructevo.modifier.core;

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
 * Energized — grants the tool an RF energy buffer (reusing TConstruct's
 * {@link ToolEnergyCapability} infrastructure, which TinkerTools already wires
 * a {@link net.minecraftforge.energy.IEnergyStorage} provider for on every
 * modifiable item). Each level raises the {@code max_energy} stat so the
 * provider actually exposes the capability, and every point of durability
 * damage is optionally paid for out of the RF pool.
 *
 * <p>This is the foundation for the rest of the RF trait family (Piezoelectric,
 * Photovoltaic, Fluxed) — those hang additional behaviour off the same pool.</p>
 */
public class EnergizedModifier extends Modifier implements ToolStatsModifierHook, ToolDamageModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS, ModifierHooks.TOOL_DAMAGE);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        int perLevel = TConEvoConfig.ENERGIZED_CAPACITY_PER_LEVEL.get();
        ToolEnergyCapability.MAX_STAT.add(builder, (double) perLevel * modifier.getLevel());
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
        if (amount <= 0) return amount;
        int unitCost = TConEvoConfig.ENERGIZED_ENERGY_COST_TOOLS.get();
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
        return 25;
    }
}
