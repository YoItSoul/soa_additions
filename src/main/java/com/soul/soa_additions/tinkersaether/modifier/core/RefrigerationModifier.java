package com.soul.soa_additions.tinkersaether.modifier.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * tinkersaether "Refrigeration": after breaking a block, freeze any adjacent
 * water source into ice and any lava source into obsidian. Original 1.12
 * iterated the 6 neighbours; we keep the same behavior.
 */
public class RefrigerationModifier extends Modifier implements BlockBreakModifierHook {

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE()) return;
        ServerLevel world = context.getWorld();
        BlockPos origin = context.getPos();
        for (int i = 0; i < 6; i++) {
            BlockPos pos = neighbour(origin, i);
            FluidState fluid = world.getFluidState(pos);
            if (fluid.getType() == Fluids.WATER && fluid.isSource()) {
                world.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
            } else if (fluid.getType() == Fluids.LAVA && fluid.isSource()) {
                world.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            }
        }
    }

    private static BlockPos neighbour(BlockPos pos, int idx) {
        return switch (idx) {
            case 0 -> pos.above();
            case 1 -> pos.below();
            case 2 -> pos.north();
            case 3 -> pos.south();
            case 4 -> pos.east();
            default -> pos.west();
        };
    }
}
