package com.soul.soa_additions.tconstructevo.modifier.core;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Fertilizing — right-clicking a plantable/growable block with this tool
 * acts as a free bone meal application. Each successful growth consumes
 * one durability so infinite farming still has a repair cost attached.
 */
public class FertilizingModifier extends Modifier implements BlockInteractionModifierHook {

    private static final int DURABILITY_COST_PER_GROWTH = 1;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT);
    }

    @Override
    public InteractionResult afterBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        if (tool.isBroken() || context.getPlayer() == null || context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (BoneMealItem.growCrop(context.getItemInHand().copy(), context.getLevel(), context.getClickedPos())) {
            if (!context.getLevel().isClientSide) {
                context.getLevel().levelEvent(2005, context.getClickedPos(), 0);
                if (!context.getPlayer().getAbilities().instabuild) {
                    ToolDamageUtil.damage(tool, DURABILITY_COST_PER_GROWTH, context.getPlayer(), context.getItemInHand());
                }
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        return InteractionResult.PASS;
    }
}
