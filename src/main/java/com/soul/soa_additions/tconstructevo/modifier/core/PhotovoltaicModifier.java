package com.soul.soa_additions.tconstructevo.modifier.core;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.capability.ToolEnergyCapability;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Photovoltaic — once per second while in the holder's inventory, generate RF
 * into the tool's shared energy pool ({@link ToolEnergyCapability}) scaled by
 * effective sky light at the holder's position. The original 1.12.2 trait let
 * each solar-panel material contribute a different gen rate; on 1.20.1 we
 * collapse that into a flat {@code level × perLevelRfPerSec} config value —
 * the capacity side is already handled by Energized / Fluxed.
 */
public class PhotovoltaicModifier extends Modifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder,
                                int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (world.isClientSide || isSelected) return;
        if (holder.tickCount % 20 != 0) return;
        int maxEnergy = ToolEnergyCapability.getMaxEnergy(tool);
        if (maxEnergy <= 0) return;
        int current = ToolEnergyCapability.getEnergy(tool);
        if (current >= maxEnergy) return;

        BlockPos pos = holder.blockPosition();
        int skylight = world.getBrightness(LightLayer.SKY, pos) - world.getSkyDarken();
        if (skylight <= 0) return;

        int baseRate = TConEvoConfig.PHOTOVOLTAIC_RF_PER_LEVEL_SEC.get() * modifier.getLevel();
        int generated = Math.round(baseRate * (skylight / 15.0F));
        if (generated <= 0) return;

        ToolEnergyCapability.setEnergy(tool, Math.min(current + generated, maxEnergy));
    }
}
