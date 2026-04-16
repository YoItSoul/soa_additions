package com.soul.soa_additions.bloodarsenal.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import wayoftime.bloodmagic.ritual.RitualRegister;
import wayoftime.bloodmagic.ritual.imperfect.IImperfectRitualStone;
import wayoftime.bloodmagic.ritual.imperfect.ImperfectRitual;

/**
 * Imperfect Ritual: Ice — freezes water in area to ice.
 * Requires a packed ice block above.
 */
@RitualRegister.Imperfect("blood_arsenal_imperfect_ice")
public class ImperfectRitualIce extends ImperfectRitual {

    private static final int RADIUS = 3;

    public ImperfectRitualIce() {
        super("blood_arsenal_imperfect_ice",
                state -> state.is(Blocks.ICE),
                500, "ritual.soa_additions.imperfect_ice");
    }

    @Override
    public boolean onActivate(IImperfectRitualStone stone, Player player) {
        Level level = stone.getRitualWorld();
        BlockPos pos = stone.getRitualPos();
        boolean changed = false;

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                for (int y = -1; y <= 1; y++) {
                    BlockPos check = pos.offset(x, y, z);
                    if (level.getFluidState(check).is(Fluids.WATER)
                            || level.getFluidState(check).is(Fluids.FLOWING_WATER)) {
                        level.setBlockAndUpdate(check, Blocks.ICE.defaultBlockState());
                        changed = true;
                    }
                }
            }
        }

        return changed;
    }
}
