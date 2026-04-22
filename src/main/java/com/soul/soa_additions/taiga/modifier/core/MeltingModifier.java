package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "melting": 2.5% chance to turn stone/cobble/netherrack/obsidian into lava after break. */
public class MeltingModifier extends Modifier implements BlockBreakModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.BLOCK_BREAK); }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE()) return;
        if (rng.nextFloat() > 0.025F) return;
        ServerLevel world = context.getWorld();
        Block b = context.getState().getBlock();
        if (b == Blocks.STONE || b == Blocks.COBBLESTONE || b == Blocks.NETHERRACK || b == Blocks.OBSIDIAN) {
            world.setBlockAndUpdate(context.getPos(), Blocks.LAVA.defaultBlockState());
        }
    }
}
