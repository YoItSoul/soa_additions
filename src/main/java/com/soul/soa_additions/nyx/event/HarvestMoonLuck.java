package com.soul.soa_additions.nyx.event;

import com.soul.soa_additions.nyx.NyxConfig;
import com.soul.soa_additions.nyx.NyxWorldData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

/**
 * The Harvest Moon's luck boon.
 *
 * <p>The Harvest Moon is the one announced in blue italics with a blue-green
 * sky; Nyx has no separate "blue moon". It already accelerates crops, and this
 * adds the other half of a harvest night: while it is up, luck runs high.</p>
 *
 * <p>Players get it as the vanilla Luck attribute, which is exactly where
 * fishing reads a player's luck from ({@code AquaFishingBobberEntity} adds
 * {@code angler.getLuck()} to its roll), so hand-casting picks it up with no
 * further wiring. The modifier is transient — it is never written to the player
 * file, so a crash or a restart mid-moon cannot leave it stuck on. It is
 * reconciled on a slow tick rather than on start/stop hooks so that logging in
 * mid-moon, changing dimension, or the event ending all resolve on their own.</p>
 *
 * <p>The Cyclic Fishing Net has no player to carry an attribute, so it reads
 * {@link #bonus} directly — same number, same config, applied in
 * {@code NetFishing}.</p>
 */
public final class HarvestMoonLuck {

    /** Stable id so the modifier can be found and removed again. */
    private static final UUID MODIFIER_ID = UUID.fromString("8f3d6f1e-0d3a-4a2e-9a5f-2c7b1e9d4a61");
    private static final String MODIFIER_NAME = "SoA Harvest Moon";
    private static final int INTERVAL_TICKS = 40;

    private HarvestMoonLuck() {}

    /** True while a Harvest Moon is running in this level. */
    public static boolean isHarvestMoon(ServerLevel level) {
        try {
            return NyxWorldData.get(level).currentEvent instanceof HarvestMoonEvent;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Configured luck for a harvest night, or 0 when the boon is off. */
    public static double bonus() {
        try {
            return NyxConfig.HARVEST_MOON_LUCK.get();
        } catch (IllegalStateException e) {
            return 0.0D;
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        double bonus = bonus();
        boolean enabled = bonus > 0.0D && NyxConfig.HARVEST_MOON_PLAYER_LUCK.get();

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            AttributeInstance luck = player.getAttribute(Attributes.LUCK);
            if (luck == null) {
                continue;
            }
            AttributeModifier current = luck.getModifier(MODIFIER_ID);
            boolean wanted = enabled && isHarvestMoon(player.serverLevel());

            if (!wanted) {
                if (current != null) {
                    luck.removeModifier(MODIFIER_ID);
                }
            } else if (current == null || current.getAmount() != bonus) {
                // Re-add when the configured amount changed under a running moon.
                luck.removeModifier(MODIFIER_ID);
                luck.addTransientModifier(new AttributeModifier(
                        MODIFIER_ID, MODIFIER_NAME, bonus, AttributeModifier.Operation.ADDITION));
            }
        }
    }
}
