package com.soul.soa_additions.tr.block;

import com.soul.soa_additions.tr.TrBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Marker BE for the Astral Ward block. Registers its position into
 * {@link WardRegistry} on load so the meteor system can do O(N wards)
 * area-warding checks without touching the chunk system — see WardRegistry
 * for the deadlock that motivated this design.
 */
public class AstralWardBlockEntity extends BlockEntity {

    public AstralWardBlockEntity(BlockPos pos, BlockState state) {
        super(TrBlockEntities.ASTRAL_WARD.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            WardRegistry.add(level.dimension(), worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        // Called on both block-break AND chunk unload. Either way, this BE
        // is no longer "active" — drop it from the registry. If the chunk
        // re-loads, onLoad will re-add.
        if (level != null && !level.isClientSide) {
            WardRegistry.remove(level.dimension(), worldPosition);
        }
        super.setRemoved();
    }
}
