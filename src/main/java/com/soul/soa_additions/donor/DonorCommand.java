package com.soul.soa_additions.donor;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
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

import java.time.Instant;

/**
 * Donor management commands:
 * <ul>
 *   <li>{@code /soa donor add <player> [tier] [message]} — add/update a donor</li>
 *   <li>{@code /soa donor remove <player>} — remove a donor</li>
 *   <li>{@code /soa donor list} — list all donors in chat</li>
 *   <li>{@code /soa donors} — open the donor wall GUI (any player)</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class DonorCommand {

    private DonorCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .then(Commands.literal("donor")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> addDonor(ctx, "SUPPORTER", ""))
                                                .then(Commands.argument("tier", StringArgumentType.word())
                                                        .executes(ctx -> addDonor(ctx,
                                                                StringArgumentType.getString(ctx, "tier"), ""))
                                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                                .executes(ctx -> addDonor(ctx,
                                                                        StringArgumentType.getString(ctx, "tier"),
                                                                        StringArgumentType.getString(ctx, "message")))))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(DonorCommand::removeDonor)))
                                .then(Commands.literal("list")
                                        .executes(DonorCommand::listDonors)))
                        .then(Commands.literal("donors")
                                .executes(DonorCommand::openWall))
        );
    }

    private static int addDonor(CommandContext<CommandSourceStack> ctx, String tierName, String message) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            DonorData.Tier tier = DonorData.Tier.fromName(tierName);
            DonorData existing = DonorRegistry.get(player.getUUID()).orElse(null);
            Instant donated = existing != null ? existing.donatedAt() : Instant.now();

            DonorData donor = new DonorData(
                    player.getUUID(),
                    player.getGameProfile().getName(),
                    tier,
                    donated,
                    message.isEmpty() && existing != null ? existing.message() : message);

            DonorRegistry.add(donor);
            DonorLifecycleHandler.syncToAll();

            // Re-sync orb state (will spawn if token is equipped)
            DonorOrbManager.syncOrb(player);

            ctx.getSource().sendSuccess(
                    () -> Component.literal("[SOA] ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(player.getGameProfile().getName())
                                    .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" is now a " + tier.display + "!")
                                    .withStyle(ChatFormatting.GREEN)),
                    true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("[SOA] " + e.getMessage()));
            return 0;
        }
    }

    private static int removeDonor(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            if (DonorRegistry.remove(player.getUUID())) {
                DonorLifecycleHandler.syncToAll();
                DonorOrbManager.removeOrb(player);
                ctx.getSource().sendSuccess(
                        () -> Component.literal("[SOA] Removed donor: " + player.getGameProfile().getName())
                                .withStyle(ChatFormatting.YELLOW),
                        true);
            } else {
                ctx.getSource().sendFailure(
                        Component.literal("[SOA] " + player.getGameProfile().getName() + " is not a donor"));
            }
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("[SOA] " + e.getMessage()));
            return 0;
        }
    }

    private static int listDonors(CommandContext<CommandSourceStack> ctx) {
        var donors = DonorRegistry.all();
        if (donors.isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("[SOA] No donors yet.").withStyle(ChatFormatting.GRAY), false);
            return Command.SINGLE_SUCCESS;
        }
        ctx.getSource().sendSuccess(
                () -> Component.literal("[SOA] Donors (" + donors.size() + "):")
                        .withStyle(ChatFormatting.GOLD), false);
        for (DonorData d : donors) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("  " + d.tier().symbol + " ")
                            .append(Component.literal(d.name()).withStyle(s -> s.withColor(d.tier().color)))
                            .append(Component.literal(" — " + d.tier().display)
                                    .withStyle(ChatFormatting.GRAY)),
                    false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int openWall(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            // Sync latest data then tell client to open the wall
            DonorLifecycleHandler.syncTo(player);
            com.soul.soa_additions.network.ModNetworking.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new DonorWallOpenPacket());
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("[SOA] " + e.getMessage()));
            return 0;
        }
    }
}
