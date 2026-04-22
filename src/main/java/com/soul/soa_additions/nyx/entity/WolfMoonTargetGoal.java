package com.soul.soa_additions.nyx.entity;

import com.soul.soa_additions.nyx.NyxWorldData;
import com.soul.soa_additions.nyx.event.BloodMoonEvent;
import com.soul.soa_additions.nyx.event.FullMoonEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;

/** Custom target goal that lets wolves go hostile against players/animals/skeletons during
 *  a Blood Moon or Full Moon. */
public class WolfMoonTargetGoal extends NonTameRandomTargetGoal<LivingEntity> {

    public WolfMoonTargetGoal(Wolf wolf) {
        super(wolf, LivingEntity.class, false,
                e -> !(e instanceof Wolf) && (e instanceof Player || e instanceof Animal || e instanceof Skeleton));
    }

    @Override public boolean canUse() { return super.canUse() && shouldHappen(); }
    @Override public boolean canContinueToUse() { return super.canContinueToUse() && shouldHappen(); }

    private boolean shouldHappen() {
        if (!(mob.level() instanceof ServerLevel sl)) return false;
        NyxWorldData data = NyxWorldData.get(sl);
        return data != null && (data.currentEvent instanceof FullMoonEvent || data.currentEvent instanceof BloodMoonEvent);
    }
}
