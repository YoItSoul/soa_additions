package com.soul.soa_additions.block;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.ModConfigs;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * When {@link ModConfigs#ENABLE_TOOL_REQUIREMENTS} is on: breaking a block without the proper tool
 * either cancels the break (empty hand) or heavily damages the tool.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ToolRequirementHandler {

    private ToolRequirementHandler() {}

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!ModConfigs.ENABLE_TOOL_REQUIREMENTS.get()) return;

        Player player = event.getPlayer();
        Level level = player.level();
        BlockState state = event.getState();
        ItemStack tool = player.getMainHandItem();

        if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            if (!(tool.getItem() instanceof AxeItem)) {
                event.setCanceled(true);
                notifyPlayer(player, level);
            }
            return;
        }

        if (isCorrectToolForBlock(tool, state)) return;

        if (tool.isEmpty()) {
            event.setCanceled(true);
            notifyPlayer(player, level);
            return;
        }

        int punishment = (int) (tool.getMaxDamage() / ModConfigs.TOOL_DAMAGE_MULTIPLIER.get());
        tool.hurtAndBreak(punishment, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static boolean isCorrectToolForBlock(ItemStack tool, BlockState state) {
        if (!(tool.getItem() instanceof TieredItem)) return false;
        if (state.is(BlockTags.NEEDS_STONE_TOOL)
                || state.is(BlockTags.NEEDS_IRON_TOOL)
                || state.is(BlockTags.NEEDS_DIAMOND_TOOL)
                || state.is(Tags.Blocks.NEEDS_NETHERITE_TOOL)) {
            return tool.isCorrectToolForDrops(state);
        }
        return true;
    }

    private static void notifyPlayer(Player player, Level level) {
        player.displayClientMessage(Component.literal("You need the proper tool to break this block!"), true);
        level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.3F, 2.0F);
    }
}
