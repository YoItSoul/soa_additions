package com.soul.soa_additions.command;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code /unstuck} — kills the player who ran it, after a confirmation.
 *
 * <p>Escape hatch for players wedged inside terrain, suffocating in a
 * structure, or otherwise unable to move. Vanilla's {@code /kill} is an
 * OP-level command, and the SOA anticheat cancels OP commands outright unless
 * they are whitelisted, so neither was usable for this.</p>
 *
 * <p><b>Two-step:</b> the first {@code /unstuck} only warns. A second one
 * within {@link #CONFIRM_WINDOW_TICKS} actually kills. A death in this pack
 * costs a full inventory drop, which is far too expensive to hand to a typo.
 * Past the window the confirmation lapses and the next call warns again.</p>
 *
 * <p><b>Permission:</b> no {@code requires()} predicate, so this is available
 * to every player at permission level 0 — an unstuck command that only admins
 * can run is useless.</p>
 *
 * <p><b>Anticheat:</b> {@code "unstuck"} is listed in
 * {@link com.soul.soa_additions.anticheat.AntiCheatHandler}'s {@code SAFE_COMMANDS}.
 * Without that entry an OP running this would have it cancelled (or be flagged
 * as a cheater if they had opted into cheater mode). It is safe to whitelist
 * because the command's only effect is killing the caller — it costs the player
 * their inventory and progress toward whatever they were doing, so it can't be
 * used to bypass progression the way {@code /give} or {@code /tp} could.</p>
 *
 * <p>The kill goes through {@link net.minecraft.world.entity.Entity#kill()},
 * which deals {@code Float.MAX_VALUE} of {@code FELL_OUT_OF_WORLD} damage. That
 * damage type carries {@code bypasses_invulnerability}, so the death is
 * unconditional: totems of undying do not fire, armour and resistance do not
 * apply, and it works even if the player is somehow invulnerable — which
 * matters, since being stuck in an invulnerable state is one of the situations
 * this command exists for.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class UnstuckCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_Unstuck");

    /** How long a pending confirmation stays valid. 15 seconds at 20 tps. */
    private static final int CONFIRM_WINDOW_TICKS = 15 * 20;

    /**
     * Players who have run {@code /unstuck} once, mapped to the server tick at
     * which they did. Keyed on UUID rather than holding the player object so a
     * disconnect can't leak an entity reference.
     *
     * <p>Server ticks, not wall-clock: a singleplayer world pauses its server
     * while the escape menu is open, and a wall-clock window would quietly
     * expire behind the pause screen.</p>
     */
    private static final Map<UUID, Integer> PENDING = new ConcurrentHashMap<>();

    private UnstuckCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("unstuck")
                        .executes(ctx -> run(ctx.getSource())));
    }

    /** Don't carry a pending confirmation across a reconnect. */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
    }

    private static int run(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("/unstuck can only be run by a player."));
            return 0;
        }

        int now = player.server.getTickCount();
        Integer armedAt = PENDING.get(player.getUUID());

        if (armedAt == null || now - armedAt > CONFIRM_WINDOW_TICKS) {
            PENDING.put(player.getUUID(), now);
            player.sendSystemMessage(prefix()
                    .append(Component.literal("This will KILL you and drop your inventory. ")
                            .withStyle(s -> s.withColor(ChatFormatting.RED)))
                    .append(Component.literal("Run ")
                            .withStyle(s -> s.withColor(ChatFormatting.GRAY)))
                    .append(Component.literal("/unstuck")
                            .withStyle(s -> s.withColor(ChatFormatting.YELLOW)))
                    .append(Component.literal(" again within 15 seconds to confirm.")
                            .withStyle(s -> s.withColor(ChatFormatting.GRAY))));
            return 1;
        }

        PENDING.remove(player.getUUID());

        // Tell them before the kill so the message isn't buried under the
        // death screen's respawn transition.
        player.sendSystemMessage(prefix()
                .append(Component.literal("Freeing you the only way that always works. You have died.")
                        .withStyle(s -> s.withColor(ChatFormatting.GRAY))));

        LOGGER.info("{} ({}) confirmed /unstuck at {} in {}",
                player.getGameProfile().getName(),
                player.getUUID(),
                player.blockPosition().toShortString(),
                player.level().dimension().location());

        player.kill();
        return 1;
    }

    private static net.minecraft.network.chat.MutableComponent prefix() {
        return Component.literal("")
                .append(Component.literal("[Unstuck] ")
                        .withStyle(s -> s.withColor(ChatFormatting.GOLD)));
    }
}
