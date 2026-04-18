package com.soul.soa_additions.tconstructevo.event;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.TConEvoPotions;
import com.soul.soa_additions.tconstructevo.item.artifact.ArtifactManager;
import com.soul.soa_additions.tconstructevo.potion.DamageBoostEffect;
import com.soul.soa_additions.tconstructevo.potion.DamageReductionEffect;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Forge-bus event listener for TConstructEvo runtime behaviour. Currently
 * applies the damage-boost / damage-reduction / heal-reduction effects;
 * additional hooks (artifact loot drops, etc.) will subscribe here as
 * they're ported. The Draconic-armour shield HUD is delegated entirely to
 * Draconic Evolution's own {@code OverlayRenderHandler}/{@code ShieldHudElement},
 * so no client-side rendering lives in this mod.
 */
public final class TConEvoEventHandler {

    private TConEvoEventHandler() {}

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(ArtifactManager.INSTANCE);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        var attacker = event.getSource().getEntity();
        if (attacker instanceof net.minecraft.world.entity.LivingEntity live) {
            var boost = live.getEffect(TConEvoPotions.DAMAGE_BOOST.get());
            if (boost != null) {
                event.setAmount(event.getAmount() * DamageBoostEffect.multiplierFor(boost.getAmplifier()));
            }
            // True Strike: placeholder consumer. The 1.12.2 original tied
            // TRUE_STRIKE to a custom "accuracy" attribute that guaranteed the
            // next hit in TConEvo's accuracy-check code path. 1.20.1 has no
            // such pipeline yet (ModifierAccuracy is deferred with the RF
            // family), so for now we turn the flag into a flat damage bump
            // and consume the effect on use — close enough for balance until
            // the accuracy subsystem lands.
            var trueStrike = live.getEffect(TConEvoPotions.TRUE_STRIKE.get());
            if (trueStrike != null) {
                event.setAmount(event.getAmount() * 1.5F);
                live.removeEffect(TConEvoPotions.TRUE_STRIKE.get());
            }
        }
        var victim = event.getEntity();
        var reduction = victim.getEffect(TConEvoPotions.DAMAGE_REDUCTION.get());
        if (reduction != null) {
            event.setAmount(event.getAmount() * DamageReductionEffect.incomingMultiplierFor(reduction.getAmplifier()));
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        var healed = event.getEntity();
        var healReduction = healed.getEffect(TConEvoPotions.HEAL_REDUCTION.get());
        if (healReduction != null) {
            float mult = (float) (double) TConEvoConfig.MORTAL_WOUNDS_HEAL_REDUCTION.get();
            event.setAmount(event.getAmount() * mult);
        }
    }
}
