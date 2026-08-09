package com.soul.soa_additions.smithery;

import com.mojang.brigadier.Command;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.network.ModNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Server-side half of {@code /soa materials}, which opens the Smithery
 * material catalog.
 *
 * <p>The command is also registered on the client dispatcher by
 * {@code MaterialsCatalogCommand}, and Forge tries that one first, so on a
 * healthy client this node never runs. It exists because a client-only command
 * is absent from the command tree the server sends: it gets no tab-completion,
 * and if the client dispatcher is missing it for any reason the command reads
 * as unknown. Registering it here makes {@code /soa materials} discoverable and
 * runnable for every player.</p>
 *
 * <p>No permission gate — the catalog is read-only reference data that grants
 * nothing, and it is on {@code AntiCheatHandler.SAFE_SOA_SUBPATHS} for the same
 * reason.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class MaterialsCatalogServerCommand {

    private MaterialsCatalogServerCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        if (!ModList.get().isLoaded("smithery")) return;
        event.getDispatcher().register(Commands.literal("soa")
                .then(Commands.literal("materials")
                        .executes(MaterialsCatalogServerCommand::open)));
    }

    private static int open(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal(
                    "/soa materials opens a screen, so it has to be run by a player."));
            return 0;
        }
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new MaterialsCatalogOpenPacket());
        return Command.SINGLE_SUCCESS;
    }
}
