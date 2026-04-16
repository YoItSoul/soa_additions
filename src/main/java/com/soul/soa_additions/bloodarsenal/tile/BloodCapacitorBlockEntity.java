package com.soul.soa_additions.bloodarsenal.tile;

import com.soul.soa_additions.bloodarsenal.BABlockEntities;
import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Blood Capacitor block entity — stores Forge Energy (RF) and auto-pushes
 * to adjacent blocks when not powered by redstone.
 *
 * <p>Ported from: arcaratus.bloodarsenal.tile.TileBloodCapacitor</p>
 */
public class BloodCapacitorBlockEntity extends BlockEntity {

    private final EnergyStorage energy;
    private final LazyOptional<IEnergyStorage> energyOptional;

    public BloodCapacitorBlockEntity(BlockPos pos, BlockState state) {
        super(BABlockEntities.BLOOD_CAPACITOR.get(), pos, state);
        int capacity = BAConfig.BLOOD_CAPACITOR_STORAGE.get();
        int transfer = BAConfig.BLOOD_CAPACITOR_TRANSFER.get();
        this.energy = new EnergyStorage(capacity, transfer, transfer);
        this.energyOptional = LazyOptional.of(() -> energy);
    }

    // ── Tick ─────────────────────────────────────────────────────────────

    public void serverTick() {
        if (level == null) return;

        // Auto-push energy to adjacent blocks when not redstone-powered
        if (!level.hasNeighborSignal(worldPosition) && energy.getEnergyStored() > 0) {
            int transfer = BAConfig.BLOOD_CAPACITOR_TRANSFER.get();
            for (Direction dir : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
                if (neighbor != null) {
                    neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(dest -> {
                        int canExtract = energy.extractEnergy(transfer, true);
                        if (canExtract > 0) {
                            int accepted = dest.receiveEnergy(canExtract, false);
                            if (accepted > 0) {
                                energy.extractEnergy(accepted, false);
                                setChanged();
                            }
                        }
                    });
                }
            }
        }
    }

    // ── Public API ───────────────────────────────────────────────────────

    public int getRedstoneSignal() {
        if (energy.getMaxEnergyStored() == 0) return 0;
        return (int) (15.0 * energy.getEnergyStored() / energy.getMaxEnergyStored());
    }

    // ── Capabilities ─────────────────────────────────────────────────────

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyOptional.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }

    // ── NBT ──────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Energy")) {
            // EnergyStorage doesn't have a public setter, so we receive energy to restore
            int stored = tag.getInt("Energy");
            energy.receiveEnergy(stored, false);
        }
    }
}
