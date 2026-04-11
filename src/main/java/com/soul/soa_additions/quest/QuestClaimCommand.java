package com.soul.soa_additions.quest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestTask;
import com.soul.soa_additions.quest.net.QuestSyncPacket;
import com.soul.soa_additions.quest.progress.ClaimService;
import com.soul.soa_additions.quest.progress.QuestEvaluator;
import com.soul.soa_additions.quest.progress.QuestProgress;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.TaskProgress;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.team.QuestTeam;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/**
 * {@code /soa quests claim <chapter/quest>} — manual claim path. The quest book
 * GUI will invoke {@link ClaimService} directly via packet; this command is
 * for testing, recovery, and GUI-less server consoles.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class QuestClaimCommand {

    private QuestClaimCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .then(Commands.literal("quests")
                                .then(Commands.literal("claim")
                                        .then(Commands.argument("quest", StringArgumentType.greedyString())
                                                .executes(QuestClaimCommand::run)))
                                .then(Commands.literal("task")
                                        .requires(s -> s.hasPermission(2))
                                        .then(Commands.literal("complete")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .then(Commands.argument("quest", StringArgumentType.string())
                                                                .then(Commands.argument("taskIndex", IntegerArgumentType.integer(0))
                                                                        .executes(ctx -> runTask(ctx, true))))))
                                        .then(Commands.literal("uncomplete")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .then(Commands.argument("quest", StringArgumentType.string())
                                                                .then(Commands.argument("taskIndex", IntegerArgumentType.integer(0))
                                                                        .executes(ctx -> runTask(ctx, false))))))))
        );
    }

    /** Force a task's counter to its target (complete=true) or zero (complete=false),
     *  targeting the given player's team. Triggers a full recompute so dependents
     *  unlock immediately, and syncs the team. Op-gated. */
    private static int runTask(CommandContext<CommandSourceStack> ctx, boolean complete) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String fullId = StringArgumentType.getString(ctx, "quest");
        int taskIndex = IntegerArgumentType.getInteger(ctx, "taskIndex");

        Optional<Quest> maybe = QuestRegistry.quest(fullId);
        if (maybe.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Unknown quest: " + fullId));
            return 0;
        }
        Quest quest = maybe.get();
        if (taskIndex >= quest.tasks().size()) {
            ctx.getSource().sendFailure(Component.literal("Task index out of range (quest has " + quest.tasks().size() + " tasks)"));
            return 0;
        }
        QuestTask task = quest.tasks().get(taskIndex);

        TeamData teams = TeamData.get(player.server);
        QuestTeam team = teams.teamOf(player);
        QuestProgressData pdata = QuestProgressData.get(player.server);
        TeamQuestProgress team_p = pdata.forTeam(team.id());
        QuestProgress qp = team_p.get(fullId);

        TaskProgress tp = qp.task(taskIndex);
        tp.setCount(complete ? task.target() : 0);
        qp.touch(player.server.getTickCount());
        pdata.touch();

        QuestEvaluator.recomputeAll(team_p);
        QuestSyncPacket.sendToTeam(player);

        String verb = complete ? "completed" : "cleared";
        ctx.getSource().sendSuccess(() -> Component.literal("[SOA] Task " + taskIndex + " " + verb + " on " + fullId)
                .withStyle(ChatFormatting.GREEN), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String fullId = StringArgumentType.getString(ctx, "quest");
        ClaimService.ClaimResult result = ClaimService.claim(player, fullId);
        if (result == ClaimService.ClaimResult.OK) QuestSyncPacket.sendToTeam(player);

        Component msg = switch (result) {
            case OK -> Component.literal("[SOA] Claimed " + fullId).withStyle(ChatFormatting.GREEN);
            case NOT_READY -> Component.literal("[SOA] Not ready to claim yet.").withStyle(ChatFormatting.RED);
            case ALREADY_CLAIMED -> Component.literal("[SOA] Already claimed.").withStyle(ChatFormatting.GRAY);
            case NOTHING_TO_GRANT -> Component.literal("[SOA] Nothing left to grant for you.").withStyle(ChatFormatting.GRAY);
            case UNKNOWN_QUEST -> Component.literal("[SOA] Unknown quest: " + fullId).withStyle(ChatFormatting.RED);
        };
        ctx.getSource().sendSuccess(() -> msg, false);
        return result == ClaimService.ClaimResult.OK ? Command.SINGLE_SUCCESS : 0;
    }
}
