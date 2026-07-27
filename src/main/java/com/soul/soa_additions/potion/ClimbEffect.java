package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Climb — climb walls like a spider while pressed against them. PotionCore port. */
public final class ClimbEffect extends MobEffect {
    public ClimbEffect() { super(MobEffectCategory.BENEFICIAL, 0x556B2F); }

    @Override public boolean isDurationEffectTick(int duration, int amplifier) { return true; }

    @Override public void applyEffectTick(LivingEntity e, int amplifier) {
        if (e.horizontalCollision) {
            Vec3 m = e.getDeltaMovement();
            e.setDeltaMovement(m.x, e.isShiftKeyDown() ? 0.0 : 0.2, m.z);
            e.fallDistance = 0.0f;
        }
    }
}
