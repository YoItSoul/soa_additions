package com.soul.soa_additions.tconstructevo.modifier.core;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Photosynthetic — while the tool is in the holder's inventory (and not
 * currently held), a probabilistic tick repairs one durability per second
 * scaled by how much of the holder's position is under open sky. Skips on
 * broken tools and on full-durability tools.
 */
public class PhotosyntheticModifier extends Modifier implements InventoryTickModifierHook {

    private static final float REPAIR_CHANCE_PER_LEVEL = 0.12F;

    private final Random rng = new Random();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder,
                                int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (world.isClientSide || isSelected) {
            return;
        }
        if (holder.tickCount % 20 != 0) {
            return;
        }
        if (tool.isBroken() || tool.getDamage() <= 0) {
            return;
        }
        BlockPos pos = holder.blockPosition();
        if (!world.isDay() || !world.canSeeSky(pos)) {
            return;
        }
        float chance = REPAIR_CHANCE_PER_LEVEL * modifier.getEffectiveLevel();
        if (rng.nextFloat() < chance) {
            ToolDamageUtil.repair(tool, 1);
        }
    }
}
