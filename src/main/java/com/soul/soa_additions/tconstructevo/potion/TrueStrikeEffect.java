package com.soul.soa_additions.tconstructevo.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Ports {@code xyz.phanta.tconevo.potion.PotionTrueStrike}. Flag-only effect:
 * the True Strike trait applies this to itself so armor-bypass code in the
 * attack handler can detect it. It has no passive tick logic of its own.
 */
public final class TrueStrikeEffect extends MobEffect {
    public TrueStrikeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFFF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
