package com.soul.soa_additions.bloodarsenal.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import wayoftime.bloodmagic.ritual.RitualRegister;
import wayoftime.bloodmagic.ritual.imperfect.IImperfectRitualStone;
import wayoftime.bloodmagic.ritual.imperfect.ImperfectRitual;

/**
 * Imperfect Ritual: Snow — covers the area around the ritual stone in snow layers.
 * Requires a snow block above.
 */
@RitualRegister.Imperfect("blood_arsenal_imperfect_snow")
public class ImperfectRitualSnow extends ImperfectRitual {

    private static final int RADIUS = 3;

    public ImperfectRitualSnow() {
        super("blood_arsenal_imperfect_snow",
                state -> state.is(Blocks.SNOW_BLOCK),
                500, "ritual.soa_additions.imperfect_snow");
    }

    @Override
    public boolean onActivate(IImperfectRitualStone stone, Player player) {
        Level level = stone.getRitualWorld();
        BlockPos pos = stone.getRitualPos();
        boolean changed = false;

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                BlockPos surface = pos.offset(x, 0, z);
                // Find the surface
                for (int y = 2; y >= -2; y--) {
                    BlockPos check = pos.offset(x, y, z);
                    BlockPos above = check.above();
                    if (!level.getBlockState(check).isAir() && level.getBlockState(above).isAir()) {
                        if (Blocks.SNOW.defaultBlockState().canSurvive(level, above)) {
                            level.setBlockAndUpdate(above, Blocks.SNOW.defaultBlockState());
                            changed = true;
                        }
                        break;
                    }
                }
            }
        }

        return changed;
    }
}
