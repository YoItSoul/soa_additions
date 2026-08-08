package com.soul.soa_additions.donor;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Reformats donor chat as {@code PlayerName: message}.
 *
 * <p>No badge, no tag — the only mark of rank is the player's own name, which
 * carries the tier ramp and is stamped with that tier's marker font. The client
 * picks that up and drifts a soft highlight across it every few seconds (see
 * {@link DonorStyles}). The separator recedes into grey and the message text is
 * left plain: the decoration is the signature, not the sentence.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class DonorChatFormatter {

    private DonorChatFormatter() {}

    /** Muted grey for the structural characters. */
    private static final int COL_FRAME = 0x4A4A4A;
    /** Message body — slightly off pure white so the name stays the focal point. */
    private static final int COL_MESSAGE = 0xD8D8D8;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        DonorData donor = DonorRegistry.get(player.getUUID()).orElse(null);
        if (donor == null) return;

        DonorStyles.Gradient ramp = DonorStyles.of(donor.tier(), donor.isOwner());

        MutableComponent decorated = Component.empty()
                .append(shining(player.getGameProfile().getName(), ramp))
                .append(frame(": "))
                .append(Component.literal(event.getRawText())
                        .withStyle(s -> s.withColor(COL_MESSAGE)));

        // Cancel the default chat pipeline (which would prepend <PlayerName>
        // via the chat type decoration, causing the name to appear twice) and
        // broadcast the fully-formatted message ourselves.
        event.setCanceled(true);
        player.server.getPlayerList().broadcastSystemMessage(decorated, false);
    }

    private static MutableComponent frame(String text) {
        return Component.literal(text).withStyle(s -> s.withColor(COL_FRAME));
    }

    /**
     * Tier-coloured text carrying the marker font. The flat colour is what a
     * client without the mixin sees, so it has to look right on its own.
     */
    private static MutableComponent shining(String text, DonorStyles.Gradient ramp) {
        return Component.literal(text)
                .withStyle(s -> s.withColor(ramp.staticColor()).withFont(ramp.font()));
    }
}
