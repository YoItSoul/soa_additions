package com.soul.soa_additions.quest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.progress.ProgressService;
import com.soul.soa_additions.quest.task.CustomTriggerTask;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * {@code /soa quests trigger <player> <triggerId>} — fires a custom trigger
 * for the target player. This is the only way to progress
 * {@link CustomTriggerTask} tasks — they cannot be completed by normal
 * gameplay. Intended for server operators to gate quests behind events
 * that only the server can verify (boss kills, storyline progression, etc.).
 *
 * <p>The trigger id is a namespaced resource location (e.g.
 * {@code soa_additions:killed_boss}). If no namespace is given, it defaults
 * to {@code soa_additions}.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class QuestTriggerCommand {

    private QuestTriggerCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .then(Commands.literal("quests")
                                .then(Commands.literal("trigger")
                                        .requires(src -> src.hasPermission(2))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("triggerId", StringArgumentType.string())
                                                        .executes(QuestTriggerCommand::run)))))
        );
    }

    private static int run(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            String raw = StringArgumentType.getString(ctx, "triggerId");
            ResourceLocation triggerId = raw.contains(":")
                    ? new ResourceLocation(raw)
                    : new ResourceLocation(SoaAdditions.MODID, raw);

            ProgressService.apply(player, 1, CustomTriggerTask.TYPE, task -> {
                CustomTriggerTask ct = (CustomTriggerTask) task;
                return ct.triggerId().equals(triggerId);
            });

            ctx.getSource().sendSuccess(
                    () -> Component.literal("[SOA] Fired trigger " + triggerId + " for " + player.getGameProfile().getName())
                            .withStyle(ChatFormatting.GREEN),
                    true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("[SOA] " + e.getMessage()));
            return 0;
        }
    }
}
