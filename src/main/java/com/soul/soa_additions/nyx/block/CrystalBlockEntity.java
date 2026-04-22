package com.soul.soa_additions.nyx.block;

import com.soul.soa_additions.nyx.NyxConfig;
import com.soul.soa_additions.nyx.NyxWorldData;
import com.soul.soa_additions.nyx.event.HarvestMoonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrystalBlockEntity extends BlockEntity {

    public int durability;

    public CrystalBlockEntity(BlockPos pos, BlockState state) {
        super(com.soul.soa_additions.block.entity.ModBlockEntities.CRYSTAL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrystalBlockEntity be) {
        if (!(level instanceof ServerLevel sl)) return;
        if (sl.getGameTime() % 600L != 0L) return;
        NyxWorldData data = NyxWorldData.get(sl);
        if (data == null) return;
        if (!(data.currentEvent instanceof HarvestMoonEvent)) return;
        if (!sl.canSeeSky(pos.above())) return;
        int max = NyxConfig.CRYSTAL_DURABILITY.get();
        be.durability = Math.min(max, be.durability + max / 10);
        be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("durability", durability);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        durability = tag.getInt("durability");
    }
}
