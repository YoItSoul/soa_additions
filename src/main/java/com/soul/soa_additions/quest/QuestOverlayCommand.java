package com.soul.soa_additions.quest;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.ModConfigs;
import com.soul.soa_additions.quest.web.QuestWebServer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * {@code /soa quests overlay} — sends the player a clickable link to
 * their personal quest overlay URL. Only works when the web overlay
 * server is enabled and running.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class QuestOverlayCommand {

    private QuestOverlayCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .then(Commands.literal("quests")
                                .then(Commands.literal("overlay")
                                        .executes(ctx -> run(ctx.getSource()))))
        );
    }

    private static int run(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }

        if (!QuestWebServer.isRunning()) {
            source.sendFailure(Component.literal("The quest web overlay is not enabled on this server."));
            return 0;
        }

        String token = QuestWebServer.tokenFor(player);
        int port = ModConfigs.QUEST_WEB_OVERLAY_PORT.get();
        String url = "http://localhost:" + port + "/?token=" + token;

        player.sendSystemMessage(Component.literal("")
                .append(Component.literal("[Quest Overlay] ")
                        .withStyle(s -> s.withColor(ChatFormatting.GOLD)))
                .append(Component.literal("Open your quest book in a browser: ")
                        .withStyle(s -> s.withColor(ChatFormatting.GRAY)))
                .append(Component.literal(url)
                        .withStyle(s -> s
                                .withColor(ChatFormatting.AQUA)
                                .withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("Click to open quest overlay"))))));
        return 1;
    }
}
