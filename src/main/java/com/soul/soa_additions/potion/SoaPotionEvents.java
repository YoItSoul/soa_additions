package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Forge-bus listeners that implement the marker-style SoA potion effects.
 * These effects don't have a per-tick implementation in their MobEffect
 * class because they fire on a triggering event:
 *   - Archery   → bonus arrow damage on hit
 *   - Explode   → explosion on death
 *   - Revival   → cancel a lethal hit, heal up + regen, consume effect
 */
public final class SoaPotionEvents {
    private SoaPotionEvents() {}

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // Archery: arrow shooter has the effect → multiply damage on this hit.
        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
            if (arrow.getOwner() instanceof LivingEntity owner) {
                var inst = owner.getEffect(SoaPotions.ARCHERY.get());
                if (inst != null) {
                    event.setAmount(event.getAmount() * ArcheryEffect.multiplierFor(inst.getAmplifier()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide()) return;

        // Revival: if active, cancel death and heal back up to 50% maxHP.
        var rev = entity.getEffect(SoaPotions.REVIVAL.get());
        if (rev != null) {
            event.setCanceled(true);
            entity.removeEffect(SoaPotions.REVIVAL.get());
            entity.setHealth(entity.getMaxHealth() * 0.5f);
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                    20 * 10, 3, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    20 * 5, 4, false, false));
            return;  // explode shouldn't fire when revival saved you
        }

        // Explode: detonate at death location, non-griefing. Radius scales
        // with amplifier (3.0F + amp).
        var exp = entity.getEffect(SoaPotions.EXPLODE.get());
        if (exp != null) {
            float r = ExplodeEffect.radiusFor(exp.getAmplifier());
            level.explode(null, entity.getX(), entity.getY(), entity.getZ(),
                    r, Level.ExplosionInteraction.NONE);
        }
    }
}
