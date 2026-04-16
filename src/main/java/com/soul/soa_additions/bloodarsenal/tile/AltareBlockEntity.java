package com.soul.soa_additions.bloodarsenal.tile;

import com.soul.soa_additions.bloodarsenal.BABlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import wayoftime.bloodmagic.altar.IBloodAltar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Altare Aenigmatica block entity — links to a Blood Magic altar and
 * auto-inserts recipe items. 10-slot inventory (9 input + 1 orb slot).
 *
 * <p>Ported from: arcaratus.bloodarsenal.tile.TileAltare</p>
 */
public class AltareBlockEntity extends BlockEntity {

    private static final int SLOTS = 10;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final LazyOptional<IItemHandler> inventoryOptional = LazyOptional.of(() -> inventory);

    private BlockPos altarPos = null;
    private UUID ownerUUID = null;

    public AltareBlockEntity(BlockPos pos, BlockState state) {
        super(BABlockEntities.ALTARE.get(), pos, state);
    }

    // ── Tick ─────────────────────────────────────────────────────────────

    public void serverTick() {
        if (level == null || altarPos == null) return;

        BlockEntity altarBE = level.getBlockEntity(altarPos);
        if (!(altarBE instanceof IBloodAltar altar)) return;

        // Auto-insert the first non-empty input slot item into the altar
        // Full altar interaction logic will be expanded in Phase 11
        // For now, this validates the link is functional
    }

    // ── Public API ───────────────────────────────────────────────────────

    public void setAltarPos(BlockPos pos) {
        this.altarPos = pos;
        setChanged();
    }

    public BlockPos getAltarPos() {
        return altarPos;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    // ── Capabilities ─────────────────────────────────────────────────────

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return inventoryOptional.cast();
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
        if (altarPos != null) {
            tag.putInt("AltarX", altarPos.getX());
            tag.putInt("AltarY", altarPos.getY());
            tag.putInt("AltarZ", altarPos.getZ());
        }
        if (ownerUUID != null) {
            tag.putUUID("ownerUUID", ownerUUID);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Items"));
        if (tag.contains("AltarX")) {
            altarPos = new BlockPos(tag.getInt("AltarX"), tag.getInt("AltarY"), tag.getInt("AltarZ"));
        }
        if (tag.hasUUID("ownerUUID")) {
            ownerUUID = tag.getUUID("ownerUUID");
        }
    }
}
