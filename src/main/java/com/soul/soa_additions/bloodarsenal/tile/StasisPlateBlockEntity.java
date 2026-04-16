package com.soul.soa_additions.bloodarsenal.tile;

import com.soul.soa_additions.bloodarsenal.BABlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity for the Stasis Plate. Holds a single item and supports
 * an {@code inStasis} lock that disables capability access during rituals.
 *
 * <p>Ported from: arcaratus.bloodarsenal.tile.TileStasisPlate</p>
 */
public class StasisPlateBlockEntity extends BlockEntity {

    private static final String TAG_IN_STASIS = "inStasis";

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    };

    private final LazyOptional<IItemHandler> inventoryOptional = LazyOptional.of(() -> inventory);
    private boolean inStasis = false;

    public StasisPlateBlockEntity(BlockPos pos, BlockState state) {
        super(BABlockEntities.STASIS_PLATE.get(), pos, state);
    }

    // ── Public API ───────────────────────────────────────────────────────

    public ItemStack getStoredItem() {
        return inventory.getStackInSlot(0);
    }

    public void setStoredItem(ItemStack stack) {
        inventory.setStackInSlot(0, stack);
        setChanged();
    }

    public boolean isInStasis() {
        return inStasis;
    }

    public void setInStasis(boolean stasis) {
        this.inStasis = stasis;
        setChanged();
    }

    // ── Capabilities ─────────────────────────────────────────────────────

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (!inStasis && cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryOptional.invalidate();
    }

    // ── NBT ──────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putBoolean(TAG_IN_STASIS, inStasis);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Items"));
        inStasis = tag.getBoolean(TAG_IN_STASIS);
    }
}
