package com.soul.soa_additions.potion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Lightning — every 100t (5s), 25% chance to summon a (visual-only)
 * lightning bolt at the entity's position. Higher amplifier = more
 * frequent (cycle = 100 / (amp+1)).
 */
public final class LightningEffect extends MobEffect {
    public LightningEffect() { super(MobEffectCategory.BENEFICIAL, 0xFFEB3B); }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % Math.max(20, 100 / (amplifier + 1)) == 0;
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel sl)) return;
        if (sl.getRandom().nextFloat() >= 0.25F) return;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
        if (bolt == null) return;
        bolt.moveTo(entity.getX(), entity.getY(), entity.getZ());
        bolt.setVisualOnly(true);
        sl.addFreshEntity(bolt);
    }
}
