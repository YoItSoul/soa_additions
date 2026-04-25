package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Pinky — 20% chance to drop a sakura_diamond when mining diamond_ore. */
public class PinkyModifier extends Modifier implements BlockBreakModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK);
    }
    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry mod,
                                slimeknights.tconstruct.library.tools.context.ToolHarvestContext context) {
        if (context.getWorld().isClientSide()) return;
        BlockState state = context.getState();
        if (!state.is(net.minecraft.world.level.block.Blocks.DIAMOND_ORE)
                && !state.is(net.minecraft.world.level.block.Blocks.DEEPSLATE_DIAMOND_ORE)) return;
        if (context.getWorld().getRandom().nextFloat() >= 0.2F) return;
        var sd = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("soa_additions", "sakura_diamond"));
        if (sd == null) return;
        Level w = (Level) context.getWorld();
        BlockPos pos = context.getPos();
        net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                w, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(sd));
        w.addFreshEntity(drop);
    }
}
