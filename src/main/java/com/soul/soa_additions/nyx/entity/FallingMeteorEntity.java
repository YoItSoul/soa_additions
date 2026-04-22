package com.soul.soa_additions.nyx.entity;

import com.soul.soa_additions.nyx.NyxBlocks;
import com.soul.soa_additions.nyx.NyxSounds;
import com.soul.soa_additions.nyx.NyxWorldData;
import com.soul.soa_additions.nyx.event.HarvestMoonEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class FallingMeteorEntity extends FallingStarEntity {

    private static final EntityDataAccessor<Integer> SIZE =
            SynchedEntityData.defineId(FallingMeteorEntity.class, EntityDataSerializers.INT);
    public boolean homing;
    public boolean disableMessage;
    public float speedModifier = 1.0f;
    public boolean spawnNoBlocks;

    public FallingMeteorEntity(EntityType<? extends FallingMeteorEntity> type, Level level) {
        super((EntityType<? extends FallingStarEntity>)(EntityType<?>) type, level);
        this.entityData.set(SIZE, level.random.nextInt(3) + 1);
        initTrajectory(2.0f * speedModifier);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SIZE, 1);
    }

    public void setSize(int size) { this.entityData.set(SIZE, size); }
    public int getSize() { return this.entityData.get(SIZE); }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (this.entityData.get(SIZE) <= 0) this.entityData.set(SIZE, 2);
        if (this.speedModifier <= 0) this.speedModifier = 1.0f;
        if (trajectoryX == 0 && trajectoryY == 0 && trajectoryZ == 0) {
            initTrajectory(2.0f * speedModifier);
        }
    }

    @Override
    public void moveTo(double x, double y, double z, float yaw, float pitch) {
        if (this.homing) y += 48.0;
        super.moveTo(x, y, z, yaw, pitch);
    }

    @Override
    protected void customUpdate() {
        if (!level().isClientSide) {
            if (getY() <= -64.0) { discard(); return; }
            if (homing) {
                Player p = level().getNearestPlayer(getX(), getY(), getZ(), 128.0, false);
                if (p != null && p.distanceToSqr(this) >= 1024.0) {
                    Vec3 dir = new Vec3(p.getX() - getX(), p.getY() - getY(), p.getZ() - getZ()).normalize();
                    trajectoryX = (float) dir.x * 2.0f * speedModifier;
                    if (dir.y < 0.0) trajectoryY = (float) dir.y * 2.0f * speedModifier;
                    trajectoryZ = (float) dir.z * 2.0f * speedModifier;
                }
            }
            if (this.horizontalCollision || this.verticalCollision) {
                if (removeTrees(blockPosition())) return;
                ServerLevel sl = (ServerLevel) level();
                NyxWorldData data = NyxWorldData.get(sl);
                int size = this.entityData.get(SIZE);
                var exp = level().explode(null, getX() + 0.5, getY() + 0.5, getZ() + 0.5,
                        (float) (size * 4), Level.ExplosionInteraction.TNT);
                if (!spawnNoBlocks) {
                    for (BlockPos affected : exp.getToBlow()) {
                        BlockState cur = level().getBlockState(affected);
                        if (cur.canBeReplaced()
                                && level().getBlockState(affected.below()).isSolidRender(level(), affected.below())
                                && level().random.nextInt(2) == 0) {
                            if (level().random.nextInt(5) == 0) {
                                level().setBlockAndUpdate(affected, net.minecraft.world.level.block.Blocks.MAGMA_BLOCK.defaultBlockState());
                            } else {
                                BlockState rockState;
                                if (data.currentEvent instanceof HarvestMoonEvent && level().random.nextInt(10) == 0) {
                                    rockState = NyxBlocks.GLEANING_METEOR_ROCK.get().defaultBlockState();
                                } else {
                                    rockState = NyxBlocks.METEOR_ROCK.get().defaultBlockState();
                                }
                                level().setBlockAndUpdate(affected, rockState);
                                data.meteorLandingSites.add(affected);
                            }
                        }
                    }
                }
                data.setDirty();
                discard();
                if (!disableMessage) {
                    Component text = Component.translatable("info.soa_additions.meteor")
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true));
                    for (Player p : level().players()) {
                        double dist = p.distanceToSqr(getX(), getY(), getZ());
                        SoundEvent sound;
                        float pitch;
                        if (dist <= 65536.0) {
                            if (dist > 256.0) p.sendSystemMessage(text);
                            sound = SoundEvents.GENERIC_EXPLODE;
                            pitch = 0.15f;
                        } else {
                            sound = NyxSounds.FALLING_METEOR_IMPACT.get();
                            pitch = 1.0f;
                        }
                        if (p instanceof ServerPlayer sp && sp.level() == level()) {
                            sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                                    net.minecraft.core.Holder.direct(sound),
                                    SoundSource.AMBIENT, sp.getX(), sp.getY(), sp.getZ(),
                                    0.5f, pitch, sp.getRandom().nextLong()));
                        }
                    }
                }
            } else if (level().getGameTime() % 35L == 0L) {
                level().playSound(null, getX(), getY(), getZ(),
                        NyxSounds.FALLING_METEOR.get(), SoundSource.AMBIENT, 5.0f, 1.0f);
            }
        } else if (isLoaded()) {
            float size = this.entityData.get(SIZE) / 2.0f + 1.0f;
            for (int i = 0; i < 60; i++) {
                double x = getX() + Mth.randomBetween(level().random, -size, size);
                double y = getY() + Mth.randomBetween(level().random, -size, size);
                double z = getZ() + Mth.randomBetween(level().random, -size, size);
                double mX = -getDeltaMovement().x + level().random.nextGaussian() * 0.02;
                double mY = -getDeltaMovement().y + level().random.nextGaussian() * 0.02;
                double mZ = -getDeltaMovement().z + level().random.nextGaussian() * 0.02;
                float r = level().random.nextFloat();
                var t = r >= 0.65f ? ParticleTypes.FLAME
                        : r >= 0.45f ? ParticleTypes.LAVA
                        : r >= 0.3f ? ParticleTypes.SMOKE
                        : ParticleTypes.LARGE_SMOKE;
                level().addParticle(t, true, x, y, z, mX, mY, mZ);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag c) {
        super.addAdditionalSaveData(c);
        c.putInt("size", entityData.get(SIZE));
        c.putBoolean("homing", homing);
        c.putBoolean("disable_message", disableMessage);
        c.putFloat("speed", speedModifier);
        c.putBoolean("spawn_no_blocks", spawnNoBlocks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag c) {
        super.readAdditionalSaveData(c);
        entityData.set(SIZE, c.getInt("size"));
        homing = c.getBoolean("homing");
        disableMessage = c.getBoolean("disable_message");
        speedModifier = c.getFloat("speed");
        spawnNoBlocks = c.getBoolean("spawn_no_blocks");
    }

    private boolean removeTrees(BlockPos pos) {
        boolean any = false;
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
            BlockPos off = pos.offset(x, y, z);
            if (off.distSqr(blockPosition()) >= 64.0) continue;
            BlockState s = level().getBlockState(off);
            if (s.is(net.minecraft.tags.BlockTags.LEAVES) || s.is(net.minecraft.tags.BlockTags.LOGS)) {
                level().removeBlock(off, false);
                removeTrees(off);
                any = true;
            }
        }
        return any;
    }

    public static FallingMeteorEntity spawn(ServerLevel level, BlockPos pos) {
        pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos)
                .above(Mth.nextInt(level.random, 64, 96));
        FallingMeteorEntity m = com.soul.soa_additions.nyx.NyxEntities.FALLING_METEOR.get().create(level);
        m.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
        level.addFreshEntity(m);
        return m;
    }
}
