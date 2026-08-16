package com.soul.soa_additions.optimizer.client;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.optimizer.OptimizerCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side {@code /soa tune} and {@code /soa tune send}.
 *
 * <h2>Why a client registration and not just the server one</h2>
 *
 * <p>The whole value of these reports is the <em>player's</em> JVM — their heap, their reclaim
 * rate, their frame times. A command registered through {@code RegisterCommandsEvent} executes
 * on the server, so a player on a multiplayer server would have been reporting the server's
 * heap and GC behaviour under their own name: not merely useless, but actively misleading data
 * in the table.</p>
 *
 * <p>Forge checks the client dispatcher before sending anything to the server, so this shadows
 * the server-side registration for players and the command never leaves their machine. The
 * server-side one remains for console and operator use, where reporting the server's JVM is
 * the correct behaviour. Same split as {@code /soa materials}.</p>
 *
 * <h2>Permissions</h2>
 *
 * <p>No {@code requires} clause, matching {@code /soa materials}. Client commands do not
 * consult server permissions at all, so this works for any player on any server regardless of
 * op status or whether cheats are enabled — and it never reaches the anti-cheat handler, which
 * only inspects commands from sources already at permission level 2.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class TuneClientCommand {

    private TuneClientCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.<CommandSourceStack>literal("soa")
                .then(Commands.literal("tune")
                        .executes(ctx -> OptimizerCommand.tune(ctx.getSource()))
                        .then(Commands.literal("send")
                                .executes(ctx -> OptimizerCommand.tuneSend(ctx.getSource())))));
    }
}
