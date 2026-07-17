package com.soul.soa_additions.bloodarsenal;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Blood Arsenal effects — the 1.12 Bleeding potion ({@code PotionBleeding} +
 * {@code DamageSourceBleeding}): periodic armor-bypassing damage.
 */
public final class BAEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "bloodarsenal");

    public static final RegistryObject<MobEffect> BLEEDING = EFFECTS.register("bleeding",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x8A0303) {
                @Override
                public void applyEffectTick(LivingEntity entity, int amplifier) {
                    entity.hurt(entity.damageSources().magic(), 1.0f + amplifier);
                }

                @Override
                public boolean isDurationEffectTick(int duration, int amplifier) {
                    int interval = Math.max(1, 40 >> amplifier);
                    return duration % interval == 0;
                }
            });

    private BAEffects() {}

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}
