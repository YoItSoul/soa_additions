package com.soul.soa_additions.quest.team;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * {@code /soa team <create|invite|join|leave|list>}. Kept minimal and
 * synchronous — team state is small, the SavedData handles persistence,
 * and there are no network races to worry about on a single server tick.
 *
 * <p>Invite flow is intentionally simple: the inviter runs {@code invite
 * <player>}, the invitee runs {@code join <teamId>}. No pending-invite
 * bookkeeping in this pass — a GUI button on the quest book will replace
 * the raw command flow once the GUI lands.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class TeamCommand {

    private TeamCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .then(Commands.literal("team")
                                .then(Commands.literal("create")
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .executes(TeamCommand::create)))
                                .then(Commands.literal("invite")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(TeamCommand::invite)))
                                .then(Commands.literal("join")
                                        .then(Commands.argument("teamId", StringArgumentType.word())
                                                .executes(TeamCommand::join)))
                                .then(Commands.literal("leave").executes(TeamCommand::leave))
                                .then(Commands.literal("list").executes(TeamCommand::list)))
        );
    }

    private static int create(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(ctx, "name");
        QuestTeam team = TeamData.get(player.server).createTeam(player, name);
        ctx.getSource().sendSuccess(() -> Component.literal("[SOA] Created team '" + team.name() + "' (" + team.id() + ")")
                .withStyle(ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int invite(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer inviter = ctx.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        QuestTeam team = TeamData.get(inviter.server).teamOf(inviter);
        if (team.solo()) {
            ctx.getSource().sendFailure(Component.literal("You're not on a team — use /soa team create first."));
            return 0;
        }
        target.sendSystemMessage(Component.literal("[SOA] " + inviter.getGameProfile().getName()
                + " invited you to team '" + team.name() + "'. Accept: /soa team join " + team.id())
                .withStyle(ChatFormatting.AQUA));
        ctx.getSource().sendSuccess(() -> Component.literal("[SOA] Invite sent."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int join(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        UUID teamId;
        try { teamId = UUID.fromString(StringArgumentType.getString(ctx, "teamId")); }
        catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("Invalid team id."));
            return 0;
        }
        try {
            QuestTeam team = TeamData.get(player.server).joinTeam(teamId, player);
            ctx.getSource().sendSuccess(() -> Component.literal("[SOA] Joined team '" + team.name() + "'")
                    .withStyle(ChatFormatting.GREEN), false);
            // Catch the joiner up on everything the team has already completed.
            com.soul.soa_additions.quest.progress.QuestProgressData pdata =
                    com.soul.soa_additions.quest.progress.QuestProgressData.get(player.server);
            com.soul.soa_additions.quest.progress.TeamQuestProgress tp = pdata.forTeam(team.id());
            com.soul.soa_additions.quest.progress.QuestEvaluator.recomputeAll(tp);
            com.soul.soa_additions.quest.net.QuestSyncPacket.sendTo(player);
            com.soul.soa_additions.quest.progress.QuestNotifier.replayCompleted(player, tp);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int leave(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TeamData.get(player.server).leaveTeam(player);
        ctx.getSource().sendSuccess(() -> Component.literal("[SOA] Left your team.").withStyle(ChatFormatting.GRAY), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        QuestTeam team = TeamData.get(player.server).teamOf(player);
        ctx.getSource().sendSuccess(() -> Component.literal("[SOA] Team: ")
                .append(Component.literal(team.name() + " (" + team.size() + " member" + (team.size() == 1 ? "" : "s") + ")")
                        .withStyle(ChatFormatting.AQUA))
                .append(team.solo() ? Component.literal(" [solo]").withStyle(ChatFormatting.GRAY) : Component.empty()), false);
        return Command.SINGLE_SUCCESS;
    }
}
