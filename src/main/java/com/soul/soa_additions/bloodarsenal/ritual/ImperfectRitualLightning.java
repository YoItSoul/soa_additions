package com.soul.soa_additions.bloodarsenal.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.ritual.RitualRegister;
import wayoftime.bloodmagic.ritual.imperfect.IImperfectRitualStone;
import wayoftime.bloodmagic.ritual.imperfect.ImperfectRitual;

/**
 * Imperfect Ritual: Lightning — summons a lightning bolt at the ritual stone.
 * Requires a coal block above.
 */
@RitualRegister.Imperfect("blood_arsenal_imperfect_lightning")
public class ImperfectRitualLightning extends ImperfectRitual {

    public ImperfectRitualLightning() {
        super("blood_arsenal_imperfect_lightning",
                state -> state.is(Blocks.IRON_BLOCK),
                5000, "ritual.soa_additions.imperfect_lightning");
    }

    @Override
    public boolean onActivate(IImperfectRitualStone stone, Player player) {
        Level level = stone.getRitualWorld();
        BlockPos pos = stone.getRitualPos();

        if (level instanceof ServerLevel serverLevel) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(Vec3.atCenterOf(pos.above()));
                bolt.setVisualOnly(false);
                serverLevel.addFreshEntity(bolt);
            }
        }
        return true;
    }
}
