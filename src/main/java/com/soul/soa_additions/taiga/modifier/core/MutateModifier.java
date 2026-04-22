package com.soul.soa_additions.taiga.modifier.core;

import java.util.List;
import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "mutate": 5% chance swap the broken block with another natural block. */
public class MutateModifier extends Modifier implements BlockBreakModifierHook {
    private static final List<Block> BLOCKS = List.of(
            Blocks.STONE, Blocks.COBBLESTONE, Blocks.DIRT, Blocks.SAND, Blocks.GRASS_BLOCK,
            Blocks.CLAY, Blocks.NETHERRACK, Blocks.ICE, Blocks.SNOW_BLOCK, Blocks.NETHER_WART_BLOCK,
            Blocks.LAVA, Blocks.WATER, Blocks.WHEAT
    );
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.BLOCK_BREAK); }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE()) return;
        ServerLevel world = context.getWorld();
        if (rng.nextFloat() > 0.05F) return;
        BlockState newState = BLOCKS.get(rng.nextInt(BLOCKS.size())).defaultBlockState();
        world.setBlockAndUpdate(context.getPos(), newState);
    }
}
