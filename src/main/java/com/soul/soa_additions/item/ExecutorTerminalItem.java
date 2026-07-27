package com.soul.soa_additions.item;

import com.soul.soa_additions.quest.task.GameStageTask;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * GC's Executor Terminal, 1:1: the sole reward of the final quest. GC wired
 * its right-click to {@code /executor @p} (data/effects/greedycraft-
 * executor_right_click.json), whose entire implementation is the pack's
 * closing joke — the world-termination feature was "never implemented" and
 * the programmer left a rant instead. Players carrying the {@code iswuss}
 * stage (the coward path) are denied outright.
 */
public class ExecutorTerminalItem extends StageItem {

    public ExecutorTerminalItem(Properties props, boolean foil, String... tooltip) {
        super(props, foil, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer sp) {
            String key = GameStageTask.hasStage(sp, "iswuss")
                    ? "greedycraft.command.executorCommand.deny"
                    : "greedycraft.command.executorCommand.message";
            sp.sendSystemMessage(Component.translatable(key));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
