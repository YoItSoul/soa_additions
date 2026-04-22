package com.soul.soa_additions.nyx.entity;

import com.soul.soa_additions.nyx.NyxBlocks;
import com.soul.soa_additions.nyx.NyxConfig;
import com.soul.soa_additions.nyx.NyxSounds;
import com.soul.soa_additions.nyx.NyxWorldData;
import com.soul.soa_additions.nyx.event.BloodMoonEvent;
import com.soul.soa_additions.nyx.event.HarvestMoonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

/** Tracks one cauldron. Fills a `lunar_water_cauldron` block after the configured tick count
 *  of night-sky exposure, if a blue dye item gets dropped on it. */
public class CauldronTrackerEntity extends Entity {

    private static final EntityDataAccessor<Boolean> IS_DONE =
            SynchedEntityData.defineId(CauldronTrackerEntity.class, EntityDataSerializers.BOOLEAN);

    private BlockPos trackingPos;
    private int timer;

    public CauldronTrackerEntity(EntityType<? extends CauldronTrackerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setTrackingPos(BlockPos pos) {
        this.trackingPos = pos;
        this.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            if (this.entityData.get(IS_DONE) && level().random.nextBoolean()) {
                double x = level().random.nextFloat() + getX() - 0.5;
                double z = level().random.nextFloat() + getZ() - 0.5;
                level().addParticle(ParticleTypes.WITCH, x, getY() + 0.25, z, 1, 1, 1);
            }
            return;
        }
        if (trackingPos == null) { discard(); return; }
        BlockState state = level().getBlockState(trackingPos);
        Block b = state.getBlock();
        boolean isLunarCauldron = b == NyxBlocks.LUNAR_WATER_CAULDRON.get();
        if (!(b instanceof CauldronBlock) && !(b instanceof LayeredCauldronBlock) && !isLunarCauldron) {
            discard();
            return;
        }
        if (isLunarCauldron) { discard(); return; }
        int level = 0;
        if (b instanceof LayeredCauldronBlock) level = state.getValue(LayeredCauldronBlock.LEVEL);
        else if (b instanceof CauldronBlock) level = 0;
        // Only track water cauldrons with water content; vanilla cauldron is always 0-level base,
        // water cauldron is LayeredCauldronBlock with level >=1.
        if (level <= 0) {
            if (timer > 0) {
                this.entityData.set(IS_DONE, false);
                timer = 0;
            }
            return;
        }
        if (!this.entityData.get(IS_DONE)) {
            ServerLevel sl = (ServerLevel) level();
            NyxWorldData data = NyxWorldData.get(sl);
            if (data == null || !level().canSeeSky(trackingPos) || NyxWorldData.isDaytime(level())) {
                timer = 0;
                return;
            }
            int phase = level().dimensionType().moonPhase(level().getDayTime());
            if (data.currentEvent instanceof HarvestMoonEvent) phase = 8;
            else if (data.currentEvent instanceof BloodMoonEvent) phase = 9;
            List<? extends Integer> ticksList = NyxConfig.LUNAR_WATER_TICKS.get();
            int ticksRequired = phase < ticksList.size() ? ticksList.get(phase) : -1;
            if (ticksRequired >= 0) {
                timer++;
                if (timer >= ticksRequired) this.entityData.set(IS_DONE, true);
            }
        } else {
            AABB box = new AABB(trackingPos);
            for (ItemEntity item : level().getEntitiesOfClass(ItemEntity.class, box)) {
                if (item.isRemoved()) continue;
                ItemStack s = item.getItem();
                if (s.getItem() == Items.BLUE_DYE) {
                    item.discard();
                    BlockState ns = NyxBlocks.LUNAR_WATER_CAULDRON.get().defaultBlockState();
                    if (ns.hasProperty(LayeredCauldronBlock.LEVEL))
                        ns = ns.setValue(LayeredCauldronBlock.LEVEL, level);
                    level().setBlockAndUpdate(trackingPos, ns);
                    level().playSound(null, getX(), getY(), getZ(),
                            NyxSounds.LUNAR_WATER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    discard();
                    return;
                }
            }
        }
    }

    @Override protected void defineSynchedData() { this.entityData.define(IS_DONE, false); }

    @Override
    protected void readAdditionalSaveData(CompoundTag c) {
        if (c.contains("tracking_pos"))
            setTrackingPos(NbtUtils.readBlockPos(c.getCompound("tracking_pos")));
        timer = c.getInt("timer");
        this.entityData.set(IS_DONE, c.getBoolean("done"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag c) {
        if (trackingPos != null) c.put("tracking_pos", NbtUtils.writeBlockPos(trackingPos));
        c.putInt("timer", timer);
        c.putBoolean("done", this.entityData.get(IS_DONE));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
