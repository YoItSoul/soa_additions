package com.soul.soa_additions.quest;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/**
 * Applies gameplay difficulty effects per pack mode, matching GreedyCraft's
 * approach to difficulty scaling:
 *
 * <ul>
 *   <li><b>All modes:</b> Force game difficulty to Hard on player login.</li>
 *   <li><b>Casual:</b> keepInventory=true, faster natural regen, lower hunger drain.</li>
 *   <li><b>Adventure:</b> keepInventory=true, slightly slower regen, moderate hunger drain.</li>
 *   <li><b>Expert:</b> keepInventory=false, much slower regen, fast hunger drain, +50% boss HP.</li>
 * </ul>
 *
 * <p>Performance: all per-tick/per-event values are cached in static fields and
 * only refreshed when the pack mode changes via {@link #applyGamerules}. The
 * tick handler early-exits in a single float comparison when the exhaustion
 * multiplier is 1.0 (default Adventure mode is close but not 1.0, so the fast
 * path only fires for modes that happen to resolve to exactly 1.0).</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class PackModeEffects {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/packmode_fx");

    private static final UUID EXPERT_BOSS_HP_UUID =
            UUID.fromString("eb5101d8-993d-4e26-ab59-5b1bc70024ee");
    private static final String EXPERT_BOSS_HP_NAME = "soa_expert_boss_health";

    // --- Cached values, refreshed on mode change ---
    private static volatile double cachedRegenMultiplier = 1.0;
    private static volatile double cachedExhaustionMultiplier = 1.0;
    private static volatile int cachedStarvationDamage = 3;
    private static volatile double cachedBossHpMultiplier = 0.0;
    private static volatile boolean cachedIsExpert = false;

    /**
     * Per-player previous-tick exhaustion level. Stored as a flat array indexed
     * by a small player slot to avoid HashMap overhead. Falls back gracefully
     * if more players connect than the array size (just skips scaling that tick).
     */
    private static final float[] prevExhaustion = new float[128];
    /** Parallel flag — true once we've stored a valid prev value for this slot. */
    private static final boolean[] prevValid = new boolean[128];

    private PackModeEffects() {}

    // ------------------------------------------------------------------ //
    //  Cache refresh
    // ------------------------------------------------------------------ //

    /** Refreshes cached multipliers from config + current mode. Called once on
     *  login and once on every {@code /soa packmode set|force}. */
    private static void refreshCache(MinecraftServer server) {
        PackMode mode = PackModeData.get(server).mode();
        cachedIsExpert = mode == PackMode.EXPERT;
        cachedRegenMultiplier = switch (mode) {
            case CASUAL -> ModConfigs.CASUAL_REGEN_SPEED_MULTIPLIER.get();
            case ADVENTURE -> ModConfigs.ADVENTURE_REGEN_SPEED_MULTIPLIER.get();
            case EXPERT -> ModConfigs.EXPERT_REGEN_SPEED_MULTIPLIER.get();
        };
        cachedExhaustionMultiplier = switch (mode) {
            case CASUAL -> ModConfigs.CASUAL_EXHAUSTION_MULTIPLIER.get();
            case ADVENTURE -> ModConfigs.ADVENTURE_EXHAUSTION_MULTIPLIER.get();
            case EXPERT -> ModConfigs.EXPERT_EXHAUSTION_MULTIPLIER.get();
        };
        cachedStarvationDamage = switch (mode) {
            case CASUAL -> ModConfigs.CASUAL_STARVATION_DAMAGE.get();
            case ADVENTURE -> ModConfigs.ADVENTURE_STARVATION_DAMAGE.get();
            case EXPERT -> ModConfigs.EXPERT_STARVATION_DAMAGE.get();
        };
        cachedBossHpMultiplier = cachedIsExpert
                ? ModConfigs.EXPERT_BOSS_HEALTH_MULTIPLIER.get() - 1.0
                : 0.0;
        // Invalidate all prev-exhaustion slots so the first tick after a
        // mode change doesn't produce a bogus delta.
        java.util.Arrays.fill(prevValid, false);
    }

    // ------------------------------------------------------------------ //
    //  Player login — set gamerules and difficulty
    // ------------------------------------------------------------------ //

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        applyGamerules(player.server);
    }

    /**
     * (Re-)applies gamerule and difficulty settings for the current pack mode.
     * Called on player login and whenever the pack mode changes at runtime.
     */
    public static void applyGamerules(MinecraftServer server) {
        refreshCache(server);
        PackMode mode = PackModeData.get(server).mode();

        if (ModConfigs.PACKMODE_FORCE_HARD_DIFFICULTY.get()) {
            server.setDifficulty(Difficulty.HARD, true);
        }

        GameRules rules = server.getGameRules();
        boolean keepInv = mode != PackMode.EXPERT;
        rules.getRule(GameRules.RULE_KEEPINVENTORY).set(keepInv, server);

        Component msg = Component.literal(
                "[SOA] Pack mode effects applied: " + mode.lower()
                        + " (keepInventory=" + keepInv + ")")
                .withStyle(ChatFormatting.GOLD);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(msg);
        }

        LOG.info("Applied packmode={} gamerules (keepInventory={})", mode.lower(), keepInv);
    }

    // ------------------------------------------------------------------ //
    //  Boss health scaling — Expert mode gives bosses +50% HP
    // ------------------------------------------------------------------ //

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (!isBoss(living)) return;

        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;

        maxHealth.removeModifier(EXPERT_BOSS_HP_UUID);

        double mult = cachedBossHpMultiplier;
        if (mult > 0.001) {
            maxHealth.addPermanentModifier(new AttributeModifier(
                    EXPERT_BOSS_HP_UUID, EXPERT_BOSS_HP_NAME,
                    mult, AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
            living.setHealth(living.getMaxHealth());
        }
    }

    /**
     * Entity types whose HP is already scaled per-packmode by the summoning
     * ritual recipe (frostmaw_ritual_recipe.js, barako_ritual_recipe.js,
     * the_forbidden_ritual_recipe.js write Health + max_health attributes
     * directly into the spawn NBT). Applying the +50% expert boss modifier
     * on top would double-stack the scaling.
     */
    private static final Set<ResourceLocation> EXPERT_HP_BLACKLIST = Set.of(
            new ResourceLocation("mowziesmobs", "barako"),
            new ResourceLocation("mowziesmobs", "frostmaw")
    );

    private static boolean isBoss(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (EXPERT_HP_BLACKLIST.contains(id)) return false;

        if (entity instanceof WitherBoss || entity instanceof EnderDragon) return true;
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            AttributeInstance hp = mob.getAttribute(Attributes.MAX_HEALTH);
            return hp != null && hp.getBaseValue() >= 100.0;
        }
        return false;
    }

    // ------------------------------------------------------------------ //
    //  Natural regen speed scaling
    // ------------------------------------------------------------------ //

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHeal(LivingHealEvent event) {
        double mult = cachedRegenMultiplier;
        if (mult == 1.0) return;
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        float amount = event.getAmount();
        if (amount != 1.0f && amount != 0.5f) return;
        event.setAmount((float) (amount / mult));
    }

    // ------------------------------------------------------------------ //
    //  Exhaustion (hunger drain) scaling via player tick
    // ------------------------------------------------------------------ //

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        double mult = cachedExhaustionMultiplier;
        if (mult == 1.0) return; // fast exit — no scaling needed
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        int slot = player.getId() & 127; // cheap modulo for array index
        FoodData food = player.getFoodData();
        float current = food.getExhaustionLevel();

        if (!prevValid[slot]) {
            prevExhaustion[slot] = current;
            prevValid[slot] = true;
            return;
        }

        float prev = prevExhaustion[slot];
        float delta = current - prev;

        if (delta <= 0) {
            prevExhaustion[slot] = current;
            return;
        }

        float scaled = (float) (delta / mult);
        float adjusted = prev + scaled;
        food.setExhaustion(adjusted);
        prevExhaustion[slot] = adjusted;
    }

    // ------------------------------------------------------------------ //
    //  Starvation damage scaling
    // ------------------------------------------------------------------ //

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onStarvationDamage(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.STARVE)) return;
        event.setAmount(cachedStarvationDamage);
    }
}
