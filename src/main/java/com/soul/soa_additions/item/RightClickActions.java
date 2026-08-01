package com.soul.soa_additions.item;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
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

    // ---------------- Potion self-buffs ----------------

    /** Adrenaline right-click: Speed V + Strength IV for 10 seconds, matching
     *  GreedyCraft's adrenaline.json (speed a=4 t=200, strength a=3 t=200). */
    public static UseActionItem.UseAction adrenalineRush() {
        return (level, player, stack) -> {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 4));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 3));
            spawnAuraParticles(level, player, ParticleTypes.FLAME);
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
                com.soul.soa_additions.util.ItemDelivery.give(player, s);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.4F);
            return true;
        };
    }

    // ---------------- GreedyCraft command effects ----------------

    /** Passport right-click — GC {@code greedycraft-unlock_all_stages.json}:
     *  totem particles, then {@code /unlockallstages @p}. */
    public static UseActionItem.UseAction unlockAllStages() {
        return (level, player, stack) -> {
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY(), player.getZ(), 1000, 2, 2, 2, 5);
            for (String stage : GcStages.ALL) {
                com.soul.soa_additions.compat.GameStagesCompat.addStage(player, stage);
            }
            player.displayClientMessage(Component.literal(
                    "§eYou have unlocked §6" + GcStages.ALL.size() + "§e game stages!"), false);
            return true;
        };
    }

    /** Passport left-click — GC {@code greedycraft-clear_stages.json}:
     *  totem particles, then {@code /lockallstages @p}. */
    public static UseActionItem.UseAction lockAllStages() {
        return (level, player, stack) -> {
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY(), player.getZ(), 1000, 2, 2, 2, 5);
            for (String stage : GcStages.ALL) {
                com.soul.soa_additions.compat.GameStagesCompat.removeStage(player, stage);
            }
            player.displayClientMessage(Component.literal(
                    "§cYou have locked §6" + GcStages.ALL.size() + "§c game stages!"), false);
            return true;
        };
    }

    /** Creative Mode Controller — GC {@code event_controller_creative/survival}
     *  ran {@code /gamemode <mode> @p}. */
    public static UseActionItem.UseAction setGameMode(GameType mode) {
        return (level, player, stack) -> {
            player.setGameMode(mode);
            return true;
        };
    }

    /** Death Counter — GC {@code greedycraft-event_death_count.json} ran
     *  {@code /deathcounter leaderboard all} as the holder. iChun's 1.20.1
     *  rewrite renamed that subcommand to {@code broadcast}; try the current
     *  name first and fall back so either version works. */
    public static UseActionItem.UseAction showDeathLeaderboard() {
        return (level, player, stack) ->
                runFirstValidCommand(player, "deathcounter broadcast", "deathcounter leaderboard all");
    }

    /** Dust of Purifying — GC {@code greedycraft-dust_purify.json} consumed the
     *  dust and ran {@code /purifyingdust @p}, which transmuted every Pure Daisy
     *  input inside a radius-7 sphere and threw explosion particles. */
    public static UseActionItem.UseAction pureDaisyBurst(int radius) {
        return (level, player, stack) -> {
            if (!ModList.get().isLoaded("botania")) {
                announce(player, "§cBotania is not installed — nothing to purify.");
                return false;
            }
            int converted = com.soul.soa_additions.compat.BotaniaPureDaisy
                    .convertAround(level, player.blockPosition(), radius);
            level.sendParticles(ParticleTypes.EXPLOSION,
                    player.getX(), player.getY(), player.getZ(), 1500, 8, 8, 8, 0.2);
            if (converted > 0) {
                announce(player, "§2Purified §6" + converted + "§2 blocks.");
            }
            return true;
        };
    }

    /** Bounty Hunter Medal [Emerald] — GC
     *  {@code greedycraft-summon_bounty_merchant.json} rewrote the trades of
     *  every MCA villager within 5 blocks. MCA has no 1.20.1 counterpart in this
     *  pack, so the same offers are stamped onto vanilla villagers instead. */
    public static UseActionItem.UseAction bountyMerchants(double radius) {
        return (level, player, stack) -> {
            List<Villager> nearby = level.getEntitiesOfClass(Villager.class,
                    new AABB(player.blockPosition()).inflate(radius));
            if (nearby.isEmpty()) {
                announce(player, "§cNo villagers nearby.");
                return false;
            }
            for (Villager villager : nearby) {
                villager.setCustomName(Component.literal("§dBounty Merchant§e"));
                villager.setCustomNameVisible(true);
                MerchantOffers offers = villager.getOffers();
                offers.clear();
                offers.addAll(bountyOffers());
            }
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY(), player.getZ(), 30, 1, 1, 1, 1);
            return true;
        };
    }

    /** GC's Bounty Merchant trade list, in source order. {@code rewardExp:0b} and
     *  {@code maxUses:9999999} come straight from the effect JSON. */
    private static List<MerchantOffer> bountyOffers() {
        ItemStack bronze = new ItemStack(ModItems.BOUNTY_HUNTER_MEDAL_BRONZE.get());
        ItemStack silver = new ItemStack(ModItems.BOUNTY_HUNTER_MEDAL_SILVER.get());
        ItemStack gold = new ItemStack(ModItems.BOUNTY_HUNTER_MEDAL.get());
        int uses = 9999999;
        return List.of(
                offer(count(bronze, 1), ItemStack.EMPTY, new ItemStack(Items.EMERALD), uses),
                offer(count(bronze, 3), ItemStack.EMPTY, new ItemStack(ModItems.REWARD_TICKET_COMMON.get()), uses),
                offer(count(silver, 1), count(bronze, 6), new ItemStack(ModItems.REWARD_TICKET_RARE.get()), uses),
                offer(count(silver, 6), count(bronze, 3), new ItemStack(ModItems.REWARD_TICKET_EPIC.get()), uses),
                offer(count(gold, 2), count(silver, 4), new ItemStack(ModItems.REWARD_TICKET_LEGENDARY.get()), uses),
                // GC attached {Potion:"minecraft:harming"} here; the 1.20.1 Pearl
                // of Knowledge is not a potion item, so the tag is dropped.
                offer(count(gold, 1), count(silver, 3), new ItemStack(ModItems.PEARL_OF_KNOWLEDGE.get()), uses),
                offer(count(silver, 3), ItemStack.EMPTY, new ItemStack(ModItems.ANCIENT_TOME_FRAGMENT.get()), uses),
                offer(count(gold, 8), ItemStack.EMPTY, new ItemStack(ModItems.ORDINARY_MEDAL.get()), uses),
                offer(count(gold, 8), ItemStack.EMPTY, new ItemStack(ModItems.CREATIVE_SHARD.get()), uses),
                offer(count(silver, 1), new ItemStack(Items.EMERALD, 16),
                        new ItemStack(ModItems.BOUNTY_HUNTER_MEDAL_EMERALD.get()), uses),
                offer(count(silver, 3), ItemStack.EMPTY, new ItemStack(ModItems.DELIVERY_ORDER.get()), uses));
    }

    private static ItemStack count(ItemStack proto, int count) {
        ItemStack copy = proto.copy();
        copy.setCount(count);
        return copy;
    }

    private static MerchantOffer offer(ItemStack costA, ItemStack costB, ItemStack result, int maxUses) {
        return new MerchantOffer(costA, costB, result, maxUses, 0, 1.0F);
    }

    /** Slime Crown — GC {@code greedycraft-summon_slime_king.json} consumed the
     *  crown and ran {@code /summonslimegod @p}, which spawned
     *  {@code betterslimes:quazar} at {@code Size:16}. That mod has no 1.20.1
     *  port, so {@link com.soul.soa_additions.boss.SlimeKing} builds the fight
     *  out of a vanilla slime instead; GC's broadcast line is kept verbatim.
     *
     *  <p>Spawned clear of the player — a 12-block boss dropped at GC's +3
     *  offset would materialise around whoever used the crown.</p> */
    public static UseActionItem.UseAction summonSlimeKing() {
        return (level, player, stack) -> {
            Vec3 look = player.getLookAngle();
            double distance = com.soul.soa_additions.boss.SlimeKing.SIZE * 0.6;
            com.soul.soa_additions.boss.SlimeKing.summon(level,
                    player.getX() + look.x * distance,
                    player.getY() + 3,
                    player.getZ() + look.z * distance,
                    player.getYRot());

            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("§9The Slime God §5§ohas awaken!"), false);
            return true;
        };
    }

    /** Runs the first candidate the server's dispatcher can actually parse, as
     *  the player but with command-block permission (GC's effect JSONs ran with
     *  {@code command_sender_name} set, i.e. elevated). */
    private static boolean runFirstValidCommand(ServerPlayer player, String... candidates) {
        MinecraftServer server = player.server;
        CommandSourceStack source = player.createCommandSourceStack().withPermission(2);
        for (String command : candidates) {
            ParseResults<CommandSourceStack> parse =
                    server.getCommands().getDispatcher().parse(command, source);
            if (parse.getReader().canRead() || !parse.getExceptions().isEmpty()) continue;
            server.getCommands().performCommand(parse, command);
            return true;
        }
        org.slf4j.LoggerFactory.getLogger("soa_additions/actions")
                .warn("No usable command among {}", String.join(", ", candidates));
        return false;
    }

    /** Vanilla's top-tier overworld chest tables. Four of them are also targets
     *  of {@code gc_dungeon_injection}, so pack loot rides along on the roll. */
    private static final List<ResourceLocation> TREASURE_TABLES = List.of(
            new ResourceLocation("minecraft", "chests/ancient_city"),
            new ResourceLocation("minecraft", "chests/woodland_mansion"),
            new ResourceLocation("minecraft", "chests/stronghold_library"),
            new ResourceLocation("minecraft", "chests/buried_treasure"),
            new ResourceLocation("minecraft", "chests/desert_pyramid"),
            new ResourceLocation("minecraft", "chests/jungle_temple"),
            new ResourceLocation("minecraft", "chests/shipwreck_treasure"));

    /** Hands out {@code min}..{@code max} stacks drawn from
     *  {@link #TREASURE_TABLES}. Each stack is one item picked at random out of
     *  a full roll of a randomly chosen table, so the odds within a table are
     *  vanilla's — you just get a slice of the chest instead of all of it. */
    public static UseActionItem.UseAction rollTreasureChest(int min, int max) {
        return (level, player, stack) -> {
            MinecraftServer server = level.getServer();
            var rand = player.getRandom();
            int count = min + rand.nextInt(max - min + 1);
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.CHEST);

            for (int i = 0; i < count; i++) {
                // A table can legitimately roll nothing; try a couple of others
                // before giving up on this slot.
                for (int attempt = 0; attempt < 3; attempt++) {
                    ResourceLocation id = TREASURE_TABLES.get(rand.nextInt(TREASURE_TABLES.size()));
                    List<ItemStack> loot = server.getLootData().getLootTable(id).getRandomItems(params).stream()
                            .filter(s -> !s.isEmpty()).toList();
                    if (loot.isEmpty()) continue;
                    com.soul.soa_additions.util.ItemDelivery.give(player,
                            loot.get(rand.nextInt(loot.size())).copy());
                    break;
                }
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
