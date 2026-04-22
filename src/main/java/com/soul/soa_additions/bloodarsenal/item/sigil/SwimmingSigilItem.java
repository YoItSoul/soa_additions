package com.soul.soa_additions.bloodarsenal.item.sigil;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.common.item.sigil.ItemSigilToggleableBase;

/**
 * Swimming Sigil — when active and the player is in water or lava:
 * restores air, boosts movement speed (capped at 0.2), and triples mining speed.
 *
 * <p>Mining speed boost is handled via BreakSpeed event in BAEventHandler.</p>
 */
public class SwimmingSigilItem extends ItemSigilToggleableBase {

    private static final double MOTION_MULTIPLIER = 1.2;
    private static final double MAX_SPEED = 0.2;

    public SwimmingSigilItem() {
        super("swimming", BAConfig.SIGIL_SWIMMING_COST.get());
    }

    @Override
    public void onSigilUpdate(ItemStack stack, Level level, Player player, int slot, boolean isSelected) {
        if (level.isClientSide()) return;
        if (!player.isInWater() && !player.isInLava()) return;

        // Restore air supply
        player.setAirSupply(player.getMaxAirSupply());

        // Movement boost
        Vec3 motion = player.getDeltaMovement();
        double mx = motion.x * MOTION_MULTIPLIER;
        double my = motion.y;
        double mz = motion.z * MOTION_MULTIPLIER;

        // Only multiply Y when moving upward
        if (my > 0.0) {
            my *= MOTION_MULTIPLIER;
        }

        // Cap all speeds
        if (mx > MAX_SPEED) mx = MAX_SPEED;
        else if (mx < -MAX_SPEED) mx = -MAX_SPEED;

        if (my > MAX_SPEED) my = MAX_SPEED;

        if (mz > MAX_SPEED) mz = MAX_SPEED;
        else if (mz < -MAX_SPEED) mz = -MAX_SPEED;

        player.setDeltaMovement(mx, my, mz);
    }
}
