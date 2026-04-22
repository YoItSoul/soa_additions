package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * TAIGA "naturebound": standing on grass/leaves occasionally heals the tool,
 * standing elsewhere occasionally damages it. 1/400 chance per tick.
 */
public class NatureBoundModifier extends Modifier implements InventoryTickModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.INVENTORY_TICK); }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder,
                                int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (world.isClientSide) return;
        if (rng.nextInt(400) != 0) return;
        BlockPos below = holder.blockPosition().below();
        BlockState state = world.getBlockState(below);
        String reg = state.getBlock().getDescriptionId();
        boolean isNature = reg.contains("grass") || reg.contains("leaves") || reg.contains("moss");
        if (isNature) {
            ToolDamageUtil.repair((ToolStack) tool, rng.nextInt(2) + 1);
        } else {
            ToolDamageUtil.damageAnimated((ToolStack) tool, 1, holder);
        }
    }
}
