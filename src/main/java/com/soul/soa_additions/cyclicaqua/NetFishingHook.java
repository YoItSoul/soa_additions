package com.soul.soa_additions.cyclicaqua;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A bobber that exists only for the length of one loot roll.
 *
 * <p>Both of the interesting fishing loot entries — vanilla's treasure pool and
 * Aquaculture's Neptune's Bounty pool — are guarded by an
 * {@code entity_properties} condition that asks the loot context's
 * {@code THIS_ENTITY} whether it is a {@link FishingHook} that
 * {@linkplain FishingHook#isOpenWaterFishing() is in open water}. Cyclic's
 * Fishing Net supplies no entity at all, which is why those two entries are
 * unreachable from the block: the condition sees {@code null} and fails.</p>
 *
 * <p>This stands in for the missing bobber. It is never added to the level,
 * never ticked and never networked — it is constructed at the sampled water
 * block, handed to the loot table as {@code THIS_ENTITY}, and dropped. The
 * open-water answer is computed up front by {@link NetFishing} using vanilla's
 * own algorithm, so the net has to be built somewhere a player could genuinely
 * cast into open water to see treasure.</p>
 */
public final class NetFishingHook extends FishingHook {

    private final boolean openWater;

    public NetFishingHook(Level level, Vec3 pos, boolean openWater) {
        super(EntityType.FISHING_BOBBER, level);
        this.openWater = openWater;
        setPos(pos.x, pos.y, pos.z);
    }

    @Override
    public boolean isOpenWaterFishing() {
        return openWater;
    }

    /** Never added to a level, but belt and braces: this bobber does nothing on its own. */
    @Override
    public void tick() {
    }
}
