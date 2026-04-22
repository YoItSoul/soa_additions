package com.soul.soa_additions.nyx;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.nyx.entity.CauldronTrackerEntity;
import com.soul.soa_additions.nyx.entity.FallingMeteorEntity;
import com.soul.soa_additions.nyx.entity.FallingStarEntity;
import com.soul.soa_additions.nyx.entity.WolfMoonTargetGoal;
import com.soul.soa_additions.nyx.event.BloodMoonEvent;
import com.soul.soa_additions.nyx.event.FullMoonEvent;
import com.soul.soa_additions.nyx.event.HarvestMoonEvent;
import com.soul.soa_additions.nyx.event.StarShowerEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Central Forge event bus handler for the Nyx port. */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NyxEvents {

    private static final UUID METEOR_ARMOR_UUID = UUID.fromString("c1f96acc-e117-4dc1-a351-e295a5de6071");
    private static final AttributeModifier METEOR_SLOWDOWN =
            new AttributeModifier(METEOR_ARMOR_UUID, "nyx:meteor_slowdown", -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private NyxEvents() {}

    // ---------- World tick: drives lunar events + falling stars + meteors ----------

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.level.isClientSide) return;
        if (!(event.level instanceof ServerLevel sl)) return;
        NyxWorldData data = NyxWorldData.get(sl);
        if (data == null) return;
        data.tick();

        if (NyxConfig.FALLING_STARS.get() && !NyxWorldData.isDaytime(sl) && sl.getGameTime() % 20L == 0L) {
            String dim = sl.dimension().location().toString();
            if (NyxConfig.ALLOWED_DIMENSIONS.get().contains(dim)) {
                for (Player player : sl.players()) {
                    float mult = data.currentEvent instanceof StarShowerEvent ? 15.0f : 1.0f;
                    if (sl.random.nextFloat() <= NyxConfig.FALLING_STAR_RARITY.get() * mult) {
                        BlockPos start = player.blockPosition().offset(
                                (int) (sl.random.nextGaussian() * 20.0), 0,
                                (int) (sl.random.nextGaussian() * 20.0));
                        BlockPos spawnPos = sl.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, start)
                                .above(Mth.nextInt(sl.random, 32, 64));
                        FallingStarEntity star = NyxEntities.FALLING_STAR.get().create(sl);
                        if (star != null) {
                            star.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);
                            sl.addFreshEntity(star);
                        }
                    }
                }
            }
        }

        if (NyxConfig.METEORS.get() && sl.getGameTime() % 20L == 0L && !sl.players().isEmpty()) {
            Player chosen = sl.players().get(sl.random.nextInt(sl.players().size()));
            int radius = NyxConfig.METEOR_SPAWN_RADIUS.get();
            double sx = chosen.getX() + Mth.nextDouble(sl.random, -radius, radius);
            double sz = chosen.getZ() + Mth.nextDouble(sl.random, -radius, radius);
            BlockPos spawnPos = BlockPos.containing(sx, 0, sz);
            double chance = data.getMeteorChance();
            ChunkPos cp = new ChunkPos(spawnPos);
            int[] ticks = data.playersPresentTicks.get(cp);
            int disallowTime = NyxConfig.METEOR_DISALLOW_TIME.get();
            if (ticks != null && ticks[0] >= disallowTime) {
                chance /= Math.pow(2.0, (double) ticks[0] / (double) disallowTime);
            }
            if (chance > 0.0 && sl.random.nextFloat() <= chance) {
                if (!sl.hasChunkAt(spawnPos)) {
                    data.cachedMeteorPositions.add(spawnPos);
                    data.setDirty();
                } else {
                    FallingMeteorEntity.spawn(sl, spawnPos);
                }
            }
        }
    }

    // ---------- Chunk load: spawn cached-meteor entities ----------

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel sl)) return;
        NyxWorldData data = NyxWorldData.get(sl);
        if (data == null) return;
        if (!NyxConfig.METEORS.get()) return;
        ChunkPos cp = event.getChunk().getPos();
        List<BlockPos> toSpawn = new ArrayList<>();
        for (BlockPos p : data.cachedMeteorPositions) {
            if (p.getX() >= cp.getMinBlockX() && p.getX() <= cp.getMaxBlockX()
                    && p.getZ() >= cp.getMinBlockZ() && p.getZ() <= cp.getMaxBlockZ()) {
                toSpawn.add(p);
            }
        }
        if (!toSpawn.isEmpty()) {
            data.cachedMeteorPositions.removeAll(toSpawn);
            data.setDirty();
            // Defer spawn off the chunk-load callback — FallingMeteorEntity.spawn
            // calls getHeightmapPos which can trigger ServerChunkCache.getChunkBlocking
            // on an adjacent chunk. Doing that while we're inside a ChunkEvent.Load
            // callback parks the server thread waiting for itself (reentrant load).
            sl.getServer().execute(() -> {
                for (BlockPos p : toSpawn) FallingMeteorEntity.spawn(sl, p);
            });
        }
    }

    // ---------- Neighbor notify: spawn CauldronTrackerEntity on new cauldrons ----------

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        Level world = (Level) event.getLevel();
        if (world.isClientSide) return;
        if (!NyxConfig.LUNAR_WATER.get()) return;
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof CauldronBlock) && !(state.getBlock() instanceof LayeredCauldronBlock)) return;
        AABB box = new AABB(pos);
        if (!world.getEntitiesOfClass(CauldronTrackerEntity.class, box).isEmpty()) return;
        CauldronTrackerEntity tracker = NyxEntities.CAULDRON_TRACKER.get().create(world);
        if (tracker == null) return;
        tracker.setTrackingPos(pos);
        world.addFreshEntity(tracker);
    }

    // ---------- Player sleeping: block during blood moon ----------

    @SubscribeEvent
    public static void onSleepCheck(SleepingTimeCheckEvent event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel sl)) return;
        NyxWorldData data = NyxWorldData.get(sl);
        if (data != null && data.currentEvent instanceof BloodMoonEvent && !NyxConfig.BLOOD_MOON_SLEEPING.get()) {
            event.setResult(Event.Result.DENY);
        }
    }

    // ---------- Blood-moon-spawned mobs vanish at daytime ----------

    @SubscribeEvent
    public static void onLivingTick(LivingTickEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide) return;
        if (!NyxConfig.BLOOD_MOON_VANISH.get()) return;
        if (NyxWorldData.isDaytime(e.level()) && e.getPersistentData().getBoolean("nyx:blood_moon_spawn")) {
            if (e.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, e.getX(), e.getY(), e.getZ(),
                        10, 0.5, 1.0, 0.5, 0.0);
            }
            e.discard();
        }
    }

    // ---------- Player join: send data + attach wolf AI ----------

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        Entity entity = event.getEntity();
        if (entity instanceof Wolf wolf) {
            wolf.targetSelector.addGoal(3, new WolfMoonTargetGoal(wolf));
        }
    }

    // ---------- Day-enchanting + lunar-water pour onto cauldron + blood-moon bed hint ----------

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        if (NyxConfig.DISALLOW_DAY_ENCHANTING.get()
                && state.getBlock() instanceof EnchantmentTableBlock) {
            long time = world.getDayTime() % 24000L;
            if ((time <= 13000L || time >= 23000L)
                    && !NyxConfig.ENCHANTING_WHITELIST_DIMENSIONS.get()
                            .contains(world.dimension().location().toString())) {
                event.setUseBlock(Event.Result.DENY);
                player.displayClientMessage(Component.translatable("info.soa_additions.nyx.day_enchanting"), true);
            }
        }
        if (!(world instanceof ServerLevel sl)) return;
        NyxWorldData data = NyxWorldData.get(sl);
        if (data != null && data.currentEvent instanceof BloodMoonEvent
                && !NyxConfig.BLOOD_MOON_SLEEPING.get()
                && state.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
            player.displayClientMessage(Component.translatable("info.soa_additions.nyx.blood_moon_sleeping"), true);
        }
    }

    // ---------- Lunar-edge XP bonus ----------

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (!NyxConfig.ENCHANTMENTS.get() || !NyxConfig.LUNAR_EDGE_XP.get()) return;
        Player attacker = event.getAttackingPlayer();
        if (attacker == null) return;
        ItemStack held = attacker.getMainHandItem();
        int lvl = EnchantmentHelper.getItemEnchantmentLevel(NyxEnchantments.LUNAR_EDGE.get(), held);
        if (lvl <= 0) return;
        float xp = event.getDroppedExperience();
        float mult = 2.0f * ((float) lvl / (float) NyxEnchantments.LUNAR_EDGE.get().getMaxLevel());
        event.setDroppedExperience(Mth.floor(xp * mult));
    }

    // ---------- Elder guardian meteor shard drop ----------

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ElderGuardian)) return;
        if (entity.level().isClientSide) return;
        if (entity.level().random.nextDouble() > NyxConfig.METEOR_SHARD_GUARDIAN_CHANCE.get()) return;
        int count = event.getLootingLevel() / 2 + 1;
        ItemStack stack = new ItemStack(ModItems.METEOR_SHARD.get(), count);
        event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
    }

    // ---------- Harvest moon blocks monster spawns ----------

    @SubscribeEvent
    public static void onCheckSpawn(MobSpawnEvent.FinalizeSpawn event) {
        LivingEntity e = event.getEntity();
        if (!(e instanceof Enemy) || e instanceof Slime) return;
        if (event.getSpawner() != null) return;
        if (!(e.level() instanceof ServerLevel sl)) return;
        NyxWorldData data = NyxWorldData.get(sl);
        if (data != null && data.currentEvent instanceof HarvestMoonEvent) {
            event.setSpawnCancelled(true);
        }
    }

    // ---------- Full moon potion buffs + extra spawns on mob spawn ----------

    @SubscribeEvent
    public static void onSpecialSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.isCanceled()) return;
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Enemy)) return;
        if (!(entity.level() instanceof ServerLevel sl)) return;
        NyxWorldData data = NyxWorldData.get(sl);
        if (data == null) return;
        if (entity instanceof Slime slime) {
            int size = slime.getSize();
            if (data.currentEvent instanceof FullMoonEvent) {
                int i = sl.random.nextInt(5);
                if (i <= 1) size += 2;
                if (i <= 2) size += 2;
            } else if (data.currentEvent instanceof HarvestMoonEvent) {
                int i = sl.random.nextInt(15);
                if (i < 8) size += i * 2;
            }
            if (size != slime.getSize()) {
                slime.setSize(size, true);
            }
        }
        if (data.currentEvent instanceof FullMoonEvent) {
            if (NyxConfig.ADD_POTION_EFFECTS.get() && !(entity instanceof Creeper)) {
                MobEffect effect = null;
                int i = sl.random.nextInt(20);
                if (i <= 2) effect = MobEffects.MOVEMENT_SPEED;
                else if (i <= 4) effect = MobEffects.DAMAGE_BOOST;
                else if (i <= 6) effect = MobEffects.REGENERATION;
                else if (i <= 7) effect = MobEffects.INVISIBILITY;
                if (effect != null) entity.addEffect(new MobEffectInstance(effect, Integer.MAX_VALUE));
            }
        }
    }

    // ---------- Meteor sword crit: slowness X for 1.5s ----------

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!event.isVanillaCritical() && event.getDamageModifier() <= 1.0f) return;
        if (!NyxConfig.METEORS.get()) return;
        ItemStack held = event.getEntity().getMainHandItem();
        if (held.getItem() != ModItems.METEOR_SWORD.get()) return;
        Entity target = event.getTarget();
        if (target instanceof LivingEntity le) {
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 10, true, false));
        }
    }

    // ---------- Meteor axe: reduce shield durability on shielded hit ----------

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!NyxConfig.METEORS.get()) return;
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity atk)) return;
        if (atk.getMainHandItem().getItem() != ModItems.METEOR_AXE.get()) return;
        LivingEntity target = event.getEntity();
        if (!(target instanceof Player p)) return;
        if (!p.isUsingItem()) return;
        ItemStack active = p.getUseItem();
        if (active.getItem() instanceof ShieldItem) {
            active.hurtAndBreak(13, p, e -> {});
        }
    }

    // ---------- Meteor armor: fire damage reduced 10% per piece ----------

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!NyxConfig.METEORS.get()) return;
        if (!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return;
        int equipped = 0;
        for (ItemStack s : event.getEntity().getArmorSlots()) {
            if (s.getItem() instanceof ArmorItem ai
                    && ai.getMaterial() == NyxMaterials.METEOR_ARMOR) equipped++;
        }
        event.setAmount(event.getAmount() * (1.0f - 0.1f * equipped));
    }

    // ---------- Hammer leap: resetfall damage + ground slam ----------

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (!NyxConfig.METEORS.get()) return;
        if (player.level().getGameTime() % 20L == 0L) {
            int equipped = 0;
            for (ItemStack s : player.getArmorSlots()) {
                if (s.getItem() instanceof ArmorItem ai
                        && ai.getMaterial() == NyxMaterials.METEOR_ARMOR) equipped++;
            }
            AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                boolean has = speed.getModifier(METEOR_ARMOR_UUID) != null;
                boolean shouldHave = equipped >= 2;
                if (shouldHave && !has) speed.addPermanentModifier(METEOR_SLOWDOWN);
                else if (!shouldHave && has) speed.removeModifier(METEOR_ARMOR_UUID);
            }
        }

        if (player.onGround() && player.fallDistance <= 0.0f
                && player.getPersistentData().contains("nyx:leap_start")) {
            long leapTime = player.level().getGameTime() - player.getPersistentData().getLong("nyx:leap_start");
            if (leapTime >= 5L) {
                float damage = NyxConfig.HAMMER_DAMAGE.get() * Math.min((float) (leapTime - 5L) / 35.0f, 1.0f);
                AABB box = new AABB(player.getX() - 3, player.getY() - 3, player.getZ() - 3,
                        player.getX() + 3, player.getY() + 3, player.getZ() + 3);
                DamageSource src = player.damageSources().playerAttack(player);
                for (LivingEntity victim : player.level().getEntitiesOfClass(LivingEntity.class, box)) {
                    if (victim != player && victim.isAlive()) {
                        victim.hurt(src, damage);
                        victim.setDeltaMovement(victim.getDeltaMovement().x, 1.0, victim.getDeltaMovement().z);
                    }
                }
                player.level().playSound(null, player.blockPosition(),
                        NyxSounds.HAMMER_END.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            player.getPersistentData().remove("nyx:leap_start");
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!NyxConfig.METEORS.get()) return;
        if (event.getEntity().getPersistentData().contains("nyx:leap_start")) {
            event.setDamageMultiplier(0.0f);
        }
    }
}
