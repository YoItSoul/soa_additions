package com.soul.soa_additions.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;

/**
 * Library of server-side right-click effects used by {@link UseActionItem}.
 * Each method is a pre-baked {@link UseActionItem.UseAction} equivalent to one
 * of the GreedyCraft {@code data/effects} bundles.
 */
public final class RightClickActions {

    private RightClickActions() {}

    // ---------------- Experience ----------------

    public static UseActionItem.UseAction grantXp(int amount) {
        return (level, player, stack) -> {
            player.giveExperiencePoints(amount);
            spawnHappyParticles(level, player);
            return true;
        };
    }

    // ---------------- Weather & time ----------------

    public static UseActionItem.UseAction clearWeather() {
        return (level, player, stack) -> {
            level.setWeatherParameters(6000, 0, false, false);
            spawnAuraParticles(level, player, ParticleTypes.FALLING_WATER);
            announce(player, "\u00a7bThe skies have cleared.");
            return true;
        };
    }

    public static UseActionItem.UseAction setTimeToDay() {
        return (level, player, stack) -> {
            long day = level.getDayTime() / 24000L;
            level.setDayTime(day * 24000L + 1000L);
            spawnAuraParticles(level, player, ParticleTypes.ENCHANT);
            announce(player, "\u00a7bThe moon has returned to peace.");
            return true;
        };
    }

    public static UseActionItem.UseAction setTimeToNight() {
        return (level, player, stack) -> {
            long day = level.getDayTime() / 24000L;
            level.setDayTime(day * 24000L + 13000L);
            spawnAuraParticles(level, player, ParticleTypes.FALLING_LAVA);
            announce(player, "\u00a7cThe blood moon will rise tonight.");
            return true;
        };
    }

    // ---------------- Spawn ----------------

    public static UseActionItem.UseAction setSpawn() {
        return (level, player, stack) -> {
            player.setRespawnPosition(level.dimension(), player.blockPosition(), player.getYRot(), true, false);
            spawnAuraParticles(level, player, ParticleTypes.TOTEM_OF_UNDYING);
            announce(player, "\u00a76Spawnpoint set!");
            return true;
        };
    }

    // ---------------- Meteor ----------------

    public static UseActionItem.UseAction summonMeteor() {
        return (level, player, stack) -> {
            BlockPos pos = player.blockPosition();
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(Vec3.atBottomCenterOf(pos));
                bolt.setVisualOnly(false);
                level.addFreshEntity(bolt);
            }
            // Kaboom — slightly bigger than creeper. DestroyMode BLOCK respects
            // mobGriefing; EXPLODE ignores it.
            level.explode(null, null, null,
                    player.getX(), player.getY(), player.getZ(),
                    6.0F, true, Level.ExplosionInteraction.TNT);
            return true;
        };
    }

    // ---------------- Area cleanup ----------------

    /** Nukes dropped items within 64 blocks of the player. Matches the
     *  GreedyCraft {@code /kill @e[type=Item]} behaviour but scoped to nearby
     *  chunks so you don't accidentally wipe items on the other side of the
     *  world. */
    public static UseActionItem.UseAction clearGroundItems() {
        return (level, player, stack) -> {
            AABB box = player.getBoundingBox().inflate(64.0);
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box);
            for (ItemEntity e : items) e.discard();
            spawnAuraParticles(level, player, ParticleTypes.SWEEP_ATTACK);
            announce(player, "\u00a7b" + items.size() + " dropped item(s) purged.");
            return true;
        };
    }

    /** Kills non-player mobs within 128 blocks of the player. */
    public static UseActionItem.UseAction clearEntities() {
        return (level, player, stack) -> {
            AABB box = player.getBoundingBox().inflate(128.0);
            List<Mob> mobs = level.getEntitiesOfClass(Mob.class, box);
            int n = 0;
            for (Mob mob : mobs) {
                mob.hurt(level.damageSources().genericKill(), Float.MAX_VALUE);
                if (mob.isRemoved() || mob.isDeadOrDying()) n++;
            }
            spawnAuraParticles(level, player, ParticleTypes.LAVA);
            announce(player, "\u00a7c" + n + " entities cleared.");
            return true;
        };
    }

    // ---------------- Loot tables ----------------

    /** Rolls a loot table and drops the results at the player's feet, matching
     *  GreedyCraft's {@code additions:loot_table_at} effect type. Missing
     *  tables produce an empty roll (the vanilla fallback), so a datapack
     *  author can override by shipping the table. */
    public static UseActionItem.UseAction rollLootTableAt(ResourceLocation lootTableId) {
        Objects.requireNonNull(lootTableId);
        return (level, player, stack) -> {
            MinecraftServer server = level.getServer();
            LootTable table = server.getLootData().getLootTable(lootTableId);
            // LootData returns LootTable.EMPTY rather than null when a table is
            // missing; rolling it is a no-op, so no special handling needed.
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.GIFT);
            List<ItemStack> loot = table.getRandomItems(params);
            for (ItemStack s : loot) {
                if (s.isEmpty()) continue;
                if (!player.getInventory().add(s)) player.drop(s, false);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.4F);
            return true;
        };
    }

    // ---------------- Helpers ----------------

    private static void announce(ServerPlayer player, String msg) {
        player.displayClientMessage(Component.literal(msg), false);
    }

    private static void spawnHappyParticles(ServerLevel level, Player player) {
        spawnAuraParticles(level, player, ParticleTypes.HAPPY_VILLAGER);
    }

    private static void spawnAuraParticles(ServerLevel level, Player player, ParticleOptions type) {
        level.sendParticles(type,
                player.getX(), player.getY() + 1.0, player.getZ(),
                30, 0.8, 1.0, 0.8, 0.1);
    }
}
