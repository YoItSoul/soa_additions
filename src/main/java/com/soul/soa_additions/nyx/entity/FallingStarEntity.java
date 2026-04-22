package com.soul.soa_additions.nyx.entity;

import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.nyx.NyxSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class FallingStarEntity extends Entity {

    protected float trajectoryX;
    protected float trajectoryY;
    protected float trajectoryZ;

    public FallingStarEntity(EntityType<? extends FallingStarEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        initTrajectory(1.0f);
    }

    @Override
    public void tick() {
        customUpdate();
        if (!level().isClientSide) {
            this.move(MoverType.SELF, new net.minecraft.world.phys.Vec3(trajectoryX, trajectoryY, trajectoryZ));
        }
        super.tick();
    }

    protected void customUpdate() {
        if (!isLoaded()) { discard(); return; }
        if (!level().isClientSide) {
            if (this.horizontalCollision || this.verticalCollision) {
                level().playSound(null, getX(), getY(), getZ(),
                        NyxSounds.FALLING_STAR_IMPACT.get(), SoundSource.AMBIENT, 10.0f, 1.0f);
                ItemStack stack = new ItemStack(ModItems.FALLEN_STAR.get());
                ItemEntity ie = new ItemEntity(level(), getX(), getY(), getZ(), stack);
                ie.getPersistentData().putBoolean("nyx:fallen_star", true);
                level().addFreshEntity(ie);
                discard();
            } else if (this.tickCount % 40 == 0) {
                level().playSound(null, getX(), getY(), getZ(),
                        NyxSounds.FALLING_STAR.get(), SoundSource.AMBIENT, 5.0f, 1.0f);
            }
        } else {
            for (int i = 0; i < 2; i++) {
                double mX = -getDeltaMovement().x + level().random.nextGaussian() * 0.05;
                double mY = -getDeltaMovement().y + level().random.nextGaussian() * 0.05;
                double mZ = -getDeltaMovement().z + level().random.nextGaussian() * 0.05;
                level().addParticle(ParticleTypes.FIREWORK, getX(), getY(), getZ(), mX, mY, mZ);
            }
        }
    }

    protected boolean isLoaded() {
        return level().hasChunkAt(blockPosition());
    }

    protected void initTrajectory(float mul) {
        trajectoryX = Mth.randomBetween(level().random, 0.5f, 1.25f) * mul;
        if (level().random.nextBoolean()) trajectoryX *= -1.0f;
        trajectoryY = Mth.randomBetween(level().random, -0.85f, -0.5f) * mul;
        trajectoryZ = Mth.randomBetween(level().random, 0.5f, 1.25f) * mul;
        if (level().random.nextBoolean()) trajectoryZ *= -1.0f;
    }

    @Override protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        trajectoryX = compound.getFloat("trajectory_x");
        trajectoryY = compound.getFloat("trajectory_y");
        trajectoryZ = compound.getFloat("trajectory_z");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("trajectory_x", trajectoryX);
        compound.putFloat("trajectory_y", trajectoryY);
        compound.putFloat("trajectory_z", trajectoryZ);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
