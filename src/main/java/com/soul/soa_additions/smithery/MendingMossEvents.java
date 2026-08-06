package com.soul.soa_additions.smithery;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Binding a Ball of Moss into Mending Moss at an enchanting table.
 *
 * <p>Tinkers' Construct 1.12 gave Mending Moss no crafting recipe: you took a Ball of Moss to an
 * enchanting table and spent 10 experience levels, and {@code ToolEvents} swapped the item
 * ({@code TinkerCommons.matMendingMoss}, with a {@code message.mending_moss.not_enough_levels}
 * refusal below the threshold). That is ported here rather than invented as a bench recipe,
 * because the XP price is the whole cost of the modifier — a craftable version would hand out
 * self-repairing tools for nine mossy cobblestone.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class MendingMossEvents {

    private MendingMossEvents() {}

    /** Experience levels the 1.12 conversion charged. */
    private static final int LEVEL_COST = 10;

    @SubscribeEvent
    public static void onBindMoss(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!event.getLevel().getBlockState(event.getPos()).is(Blocks.ENCHANTING_TABLE)) return;

        ItemStack held = event.getItemStack();
        if (!held.is(ModItems.BALL_OF_MOSS.get())) return;

        event.setCanceled(true);   // don't open the enchanting screen on top of this

        if (!player.getAbilities().instabuild && player.experienceLevel < LEVEL_COST) {
            player.displayClientMessage(Component.translatable(
                    "message.soa_additions.mending_moss.not_enough_levels", LEVEL_COST), true);
            return;
        }
        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-LEVEL_COST);
        }

        held.shrink(1);
        ItemStack moss = new ItemStack(ModItems.MENDING_MOSS.get());
        if (!player.getInventory().add(moss)) {
            player.drop(moss, false);
        }
        player.level().playSound(null, event.getPos(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.BLOCKS, 0.8f, 1.2f);
        player.displayClientMessage(
                Component.translatable("message.soa_additions.mending_moss.bound"), true);
    }
}
