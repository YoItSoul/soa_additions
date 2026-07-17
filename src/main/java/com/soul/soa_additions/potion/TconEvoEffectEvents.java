package com.soul.soa_additions.potion;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Runtime behavior of the TConEvo effects (1.12 TconEvo event handlers):
 * mortal wounds blocks 75% of healing, damage reduction shaves 4%/level,
 * immortality cancels a killing blow (leaving 1 HP) and is consumed.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class TconEvoEffectEvents {

    private static final float MORTAL_WOUNDS_HEAL_REDUCTION = 0.75f;
    private static final float DAMAGE_REDUCTION_PER_LEVEL = 0.04f;

    private TconEvoEffectEvents() {}

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (event.getEntity().hasEffect(TconEvoEffects.MORTAL_WOUNDS.get())) {
            event.setAmount(event.getAmount() * (1.0f - MORTAL_WOUNDS_HEAL_REDUCTION));
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        var inst = event.getEntity().getEffect(TconEvoEffects.DAMAGE_REDUCTION.get());
        if (inst != null) {
            float factor = 1.0f - DAMAGE_REDUCTION_PER_LEVEL * (inst.getAmplifier() + 1);
            event.setAmount(event.getAmount() * Math.max(0.0f, factor));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !entity.hasEffect(TconEvoEffects.IMMORTALITY.get())) return;
        event.setCanceled(true);
        entity.removeEffect(TconEvoEffects.IMMORTALITY.get());
        entity.setHealth(Math.max(1.0f, entity.getHealth()));
    }
}
