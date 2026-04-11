package com.soul.soa_additions.quest;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.net.QuestSyncPacket;
import com.soul.soa_additions.quest.progress.QuestEvaluator;
import com.soul.soa_additions.quest.progress.QuestProgressData;
import com.soul.soa_additions.quest.progress.TeamQuestProgress;
import com.soul.soa_additions.quest.team.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.UUID;

/**
 * {@code /soa quests resetprogress <player>} — wipes the target's team
 * progress bucket so every quest returns to its freshly-loaded state.
 * Accepts a {@link GameProfileArgument} so OPs can reset offline players
 * by name; the reset applies to whatever team the target belongs to at
 * the moment of the command, and any other members (online or offline)
 * share the wipe because progress is team-keyed.
 *
 * <p>Offline members pick up the reset the next time they log in: the
 * lifecycle handler lazily creates a fresh empty bucket and pushes a sync
 * packet. No extra plumbing needed here.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class QuestResetCommand {

    private QuestResetCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .then(Commands.literal("quests")
                                .then(Commands.literal("resetprogress")
                                        .requires(src -> src.hasPermission(2))
                                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                                .executes(QuestResetCommand::run))))
        );
    }

    private static int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
        if (profiles.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("[SOA] No matching player."));
            return 0;
        }

        MinecraftServer server = ctx.getSource().getServer();
        TeamData teams = TeamData.get(server);
        QuestProgressData data = QuestProgressData.get(server);
        int resetCount = 0;

        for (GameProfile profile : profiles) {
            UUID targetUuid = profile.getId();
            UUID teamId = teams.teamIdOf(targetUuid);

            data.dropTeam(teamId);
            TeamQuestProgress fresh = data.forTeam(teamId);
            QuestEvaluator.recomputeAll(fresh);

            // Push the empty snapshot to any team members who happen to
            // be online. Offline members (including the target if they're
            // offline) pick up the reset on their next login.
            for (ServerPlayer member : teams.onlineMembers(server, teamId)) {
                QuestSyncPacket.sendTo(member);
            }
            ServerPlayer directTarget = server.getPlayerList().getPlayer(targetUuid);
            if (directTarget != null) QuestSyncPacket.sendTo(directTarget);

            final String name = profile.getName() != null ? profile.getName() : targetUuid.toString();
            ctx.getSource().sendSuccess(
                    () -> Component.literal("[SOA] Reset quest progress for " + name)
                            .withStyle(ChatFormatting.YELLOW),
                    true);
            resetCount++;
        }

        data.touch();
        return resetCount > 0 ? Command.SINGLE_SUCCESS : 0;
    }
}
