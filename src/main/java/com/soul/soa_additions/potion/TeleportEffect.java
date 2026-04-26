package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Teleport — every 100t, 50% chance to short-range random teleport.
 * Amplifier scales radius (8 + amp*4 blocks).
 */
public final class TeleportEffect extends MobEffect {
    public TeleportEffect() { super(MobEffectCategory.NEUTRAL, 0x673AB7); }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 100 == 0; }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getRandom().nextFloat() >= 0.5F) return;
        double radius = 8.0 + amplifier * 4.0;
        for (int tries = 0; tries < 16; tries++) {
            double dx = (entity.getRandom().nextDouble() - 0.5) * 2 * radius;
            double dy = (entity.getRandom().nextInt(7) - 3);
            double dz = (entity.getRandom().nextDouble() - 0.5) * 2 * radius;
            if (entity.randomTeleport(entity.getX() + dx, entity.getY() + dy, entity.getZ() + dz, true)) break;
        }
    }
}
