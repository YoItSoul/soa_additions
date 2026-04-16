package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * CORE modifier — auto-smelts mined blocks. Incompatible with Silky.
 * Interacts with Fortunate for fortune drops on smelted results.
 */
public class ModifierSmelting extends Modifier {

    public ModifierSmelting() {
        super("smelting", EnumModifierType.CORE, 1);
    }

    @Override
    public void onBlockDestroyed(Level level, Player player, ItemStack stack, BlockPos pos, BlockState state, int modLevel) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

        // Get the block's normal drop
        ItemStack blockDrop = new ItemStack(state.getBlock());
        if (blockDrop.isEmpty()) return;

        // Try to find a smelting recipe
        Optional<SmeltingRecipe> recipe = serverLevel.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(blockDrop), serverLevel);

        if (recipe.isPresent()) {
            ItemStack result = recipe.get().getResultItem(serverLevel.registryAccess()).copy();
            if (!result.isEmpty()) {
                Block.popResource(serverLevel, pos, result);
            }
        }
    }
}
