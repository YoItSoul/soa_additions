package com.soul.soa_additions.boss;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Slime King — what the Slime Crown summons.
 *
 * <p>GreedyCraft's {@code /summonslimegod} spawned {@code betterslimes:quazar}
 * at {@code Size:16}, a purpose-built boss with a boss bar, a telegraphed leap
 * and a death explosion. BetterSlimes has no 1.20.1 port, so instead of porting
 * a whole mob this dresses a vanilla slime up as a boss: oversized, heavily
 * armoured, permanently buffed, named, and given a real boss bar.</p>
 *
 * <p>{@link #SIZE} is the one number worth tuning. Vanilla clamps slime size to
 * 127, but size scales the hitbox by {@code 0.51 × size} — 127 is a ~65-block
 * cube that spawns embedded in terrain and cannot path anywhere. 24 gives a
 * ~12-block boss, comfortably larger than GC's effective 17.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SlimeKing {

    /** Entity tag that marks a slime as the boss, so it survives save/load. */
    public static final String TAG = "soa_slime_king";

    public static final int SIZE = 24;
    /** Deliberately low: the fight is an endurance race against {@link
     *  MobEffects#REGENERATION}, not a health-bar slog. Regeneration II heals
     *  1 HP every 25 ticks, so a party that cannot sustain damage through the
     *  armour never makes progress no matter how long they swing. */
    private static final double MAX_HEALTH = 500.0;
    /** Death pays out this many rolls of {@code minecraft:entities/slime}. */
    private static final int LOOT_MULTIPLIER = 10;
    /** Boss-bar visibility radius, in blocks. */
    private static final double BAR_RANGE = 96.0;

    private static final Map<UUID, ServerBossEvent> BARS = new HashMap<>();

    private SlimeKing() {}

    /** Spawns a Slime King centred on the given position. */
    public static Slime summon(ServerLevel level, double x, double y, double z, float yRot) {
        Slime slime = new Slime(EntityType.SLIME, level);
        // setSize derives max health (size²), speed, damage and xp from the size,
        // so it has to run before the boss numbers are stamped on top.
        slime.setSize(SIZE, true);

        setAttribute(slime, Attributes.MAX_HEALTH, MAX_HEALTH);
        setAttribute(slime, Attributes.ARMOR, 20.0);
        setAttribute(slime, Attributes.ARMOR_TOUGHNESS, 12.0);
        setAttribute(slime, Attributes.KNOCKBACK_RESISTANCE, 1.0);
        setAttribute(slime, Attributes.FOLLOW_RANGE, 64.0);
        setAttribute(slime, Attributes.ATTACK_DAMAGE, 20.0);
        // setSize would leave this at 0.2 + 0.1 × size (2.6 at size 24), which
        // makes it uncatchable rather than menacing.
        setAttribute(slime, Attributes.MOVEMENT_SPEED, 0.8);
        slime.setHealth(slime.getMaxHealth());

        slime.addEffect(infinite(MobEffects.REGENERATION, 1));
        slime.addEffect(infinite(MobEffects.DAMAGE_RESISTANCE, 1));
        slime.addEffect(infinite(MobEffects.FIRE_RESISTANCE, 0));
        slime.addEffect(infinite(MobEffects.DAMAGE_BOOST, 0));

        slime.setCustomName(Component.literal("§9§lSlime King"));
        slime.setCustomNameVisible(true);
        slime.setPersistenceRequired();
        slime.addTag(TAG);

        slime.moveTo(x, y, z, yRot, 0.0F);
        level.addFreshEntity(slime);
        return slime;
    }

    private static void setAttribute(Slime slime, Attribute attribute, double value) {
        AttributeInstance instance = slime.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    private static MobEffectInstance infinite(net.minecraft.world.effect.MobEffect effect, int amplifier) {
        return new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, amplifier, false, false);
    }

    // ---------------- Boss bar ----------------

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Slime slime)) return;
        if (slime.level().isClientSide() || !slime.getTags().contains(TAG)) return;
        if (slime.tickCount % 10 != 0) return;

        ServerBossEvent bar = BARS.computeIfAbsent(slime.getUUID(), id -> new ServerBossEvent(
                slime.getDisplayName(), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS));
        bar.setProgress(Math.max(0.0F, slime.getHealth() / slime.getMaxHealth()));

        if (!(slime.level() instanceof ServerLevel level)) return;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(slime) <= BAR_RANGE * BAR_RANGE) {
                bar.addPlayer(player);
            } else {
                bar.removePlayer(player);
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        clearBar(entity);
        if (!(entity instanceof Slime slime) || !slime.getTags().contains(TAG)) return;
        if (slime.level() instanceof ServerLevel level) {
            dropBossLoot(level, slime, event.getSource());
        }
    }

    /**
     * Pays out {@link #LOOT_MULTIPLIER} rolls of the ordinary slime table.
     *
     * <p>A slime this large would otherwise drop nothing at all: vanilla's
     * {@code Slime#getDefaultLootTable} hands back the empty table unless the
     * size is exactly 1, so the boss dies bare. Rolling the real table here
     * keeps looting and the killer's context intact.</p>
     */
    private static void dropBossLoot(ServerLevel level, Slime slime, DamageSource source) {
        LootTable table = level.getServer().getLootData()
                .getLootTable(EntityType.SLIME.getDefaultLootTable());

        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, slime)
                .withParameter(LootContextParams.ORIGIN, slime.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, source.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, source.getDirectEntity());
        if (slime.getKillCredit() instanceof Player killer) {
            builder.withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer)
                    .withLuck(killer.getLuck());
        }
        LootParams params = builder.create(LootContextParamSets.ENTITY);

        for (int roll = 0; roll < LOOT_MULTIPLIER; roll++) {
            for (ItemStack drop : table.getRandomItems(params)) {
                if (!drop.isEmpty()) slime.spawnAtLocation(drop);
            }
        }
    }

    /** Unload is not death — drop the bar so it cannot leak, it rebuilds on reload. */
    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) clearBar(living);
    }

    private static void clearBar(LivingEntity entity) {
        ServerBossEvent bar = BARS.remove(entity.getUUID());
        if (bar != null) bar.removeAllPlayers();
    }
}
