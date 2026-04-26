package com.soul.soa_additions.quest;

import insane96mcp.insanelib.event.PlayerExhaustionEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Ports GreedyCraft's HungerTweaker {@code HungerEvents.onExhausted} low-food
 * drain-skip mechanic (Tip 52: "the lower your hunger, the slower it drains").
 *
 * <p>GC computed: {@code chance = (20 - foodLevel) * 400 / 10000} per
 * exhaustion tick, clamped 0–1. On hit, the exhaustion delta is zeroed.
 * At full hunger this gives 0%; at 6 hunger it's 56%; at 0 it caps near
 * 80% — making the last few hunger ticks drag out so the player has time
 * to eat before starving.
 *
 * <p>InsaneLib (already installed) fires {@link PlayerExhaustionEvent} from
 * its {@code ILEventFactory} every time the vanilla food system charges
 * exhaustion. We subscribe and zero the amount on the RNG hit.
 *
 * <p>Class-loading guard: SoaAdditions checks {@code ModList.isLoaded("insanelib")}
 * before calling {@link #init()}. If InsaneLib is absent, this class never
 * gets loaded — its hard import on {@link PlayerExhaustionEvent} stays
 * dormant and Forge doesn't hit a NoClassDefFoundError.
 */
public final class InsaneLibHungerBridge {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/hunger_bridge");

    private InsaneLibHungerBridge() {}

    /** Registers the event handlers. Call only if InsaneLib is loaded. */
    public static void init() {
        MinecraftForge.EVENT_BUS.register(InsaneLibHungerBridge.class);
        LOG.info("InsaneLib PlayerExhaustionEvent bridge initialized "
                + "(Tip 52: low-food drain-skip RNG)");
    }

    /**
     * Per GC formula. Clamped to [0, 1]. At foodLevel=20 → 0; foodLevel=6 →
     * 0.56; foodLevel=0 → 0.80.
     */
    private static double skipChance(int foodLevel) {
        if (foodLevel >= 20) return 0.0;
        double raw = (20 - foodLevel) * 400.0 / 10000.0;
        return Math.min(Math.max(raw, 0.0), 1.0);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onExhaustion(PlayerExhaustionEvent event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) return;
        if (player.isCreative() || player.isSpectator()) return;

        FoodData food = player.getFoodData();
        int level = food.getFoodLevel();
        double chance = skipChance(level);
        if (chance <= 0.0) return;

        if (ThreadLocalRandom.current().nextDouble() < chance) {
            event.setAmount(0.0f);
        }
    }

}
