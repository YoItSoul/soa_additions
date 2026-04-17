package com.soul.soa_additions.donor;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Intercepts chat messages from donors and reformats them with their
 * tier color, symbol prefix, and obfuscated (glowing) bracket effect.
 *
 * <p>Result looks like: {@code ✨ §d§lPlayerName§r: message}</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class DonorChatFormatter {

    private DonorChatFormatter() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        DonorData donor = DonorRegistry.get(player.getUUID()).orElse(null);
        if (donor == null) return;

        DonorData.Tier tier = donor.tier();

        // Build the decorated message:
        // [symbol] [glowing bracket] PlayerName [glowing bracket]: message
        MutableComponent decorated = Component.empty();

        // Tier symbol
        decorated.append(Component.literal(tier.symbol + " ")
                .withStyle(s -> s.withColor(tier.color)));

        // Glowing brackets around name — obfuscated text creates the "glow" shimmer
        decorated.append(Component.literal("|")
                .withStyle(s -> s.withColor(tier.color).withObfuscated(true)));

        // Player name in tier color, bold
        decorated.append(Component.literal(" " + player.getGameProfile().getName() + " ")
                .withStyle(s -> s.withColor(tier.color).withBold(true)));

        // Closing glow bracket
        decorated.append(Component.literal("|")
                .withStyle(s -> s.withColor(tier.color).withObfuscated(true)));

        // Colon separator
        decorated.append(Component.literal(": "));

        // Message
        decorated.append(Component.literal(event.getRawText())
                .withStyle(ChatFormatting.WHITE));

        // Cancel the default chat pipeline (which would prepend <PlayerName>
        // via the chat type decoration, causing the name to appear twice) and
        // broadcast the fully-formatted message ourselves.
        event.setCanceled(true);
        for (ServerPlayer recipient : player.server.getPlayerList().getPlayers()) {
            recipient.sendSystemMessage(decorated);
        }
    }
}
