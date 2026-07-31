package com.soul.soa_additions.combat;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.HeadshotConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Headshot mechanics, ported for gameplay parity with GreedyCraft's
 * iblis-headshots 1.2.6 running under GC's shipped config.
 *
 * <p>How a hit is resolved:</p>
 * <ol>
 *   <li>Only damage with a direct entity counts — that alone excludes fall,
 *       fire, drowning and starvation without needing a damage-type blocklist.</li>
 *   <li>The direct entity's one-tick travel segment is ray-traced against the
 *       target's species-specific head box ({@link HeadBoxes}).</li>
 *   <li>A miss is scaled by the body-shot multiplier; a hit is scaled by the
 *       headshot multiplier, itself pulled back toward 1.0 by the helmet's
 *       protection ({@link HeadgearProtection}).</li>
 * </ol>
 *
 * <p>This runs on {@link LivingHurtEvent}, which fires <em>before</em> vanilla's
 * armour reduction ({@code LivingEntity.actuallyHurt} calls
 * {@code ForgeHooks.onLivingHurt} and only then {@code getDamageAfterArmorAbsorb}).
 * So the amount we see is raw incoming damage and the full armour set still gets
 * its cut afterwards — the helmet multiplier here is the headshot's own
 * mitigation, layered on top of normal armour exactly as it is in GC.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class HeadshotHandler {

    /**
     * Some mods fire {@link LivingDamageEvent} without a preceding
     * {@link LivingHurtEvent}. GC covers that by handling both and remembering
     * which entity it just processed so a normal hit isn't scaled twice.
     */
    private static int lastHandledEntityId = -1;

    private HeadshotHandler() {}

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) return;
        LivingEntity victim = event.getEntity();
        lastHandledEntityId = victim.getId();
        event.setAmount(recalculateDamage(event.getAmount(), victim, event.getSource()));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (lastHandledEntityId == victim.getId()) {
            lastHandledEntityId = -1;
            return;
        }
        event.setAmount(recalculateDamage(event.getAmount(), victim, event.getSource()));
    }

    private static float recalculateDamage(float damage, LivingEntity victim, DamageSource source) {
        Level level = victim.level();
        if (damage < 0.1F || !(level instanceof ServerLevel serverLevel)) return damage;

        Entity direct = source.getDirectEntity();
        if (direct == null) return damage;

        // A projectile's impact point sits somewhere inside the tick it just
        // travelled, so trace the whole segment rather than a single point.
        Vec3 delta = direct.getDeltaMovement();
        Vec3 start = direct.position().subtract(delta);
        Vec3 end = direct.position().add(delta);

        if (direct instanceof Player attacker) {
            // Direct-damage attacks (melee, and hitscan weapons that skip spawning
            // a projectile) have no travel segment, so aim down the look vector
            // instead — but only past the melee gate, which keeps ordinary swings
            // out of the headshot system entirely.
            double distSq = attacker.distanceToSqr(victim);
            if (distSq < HeadshotConfig.meleeMinDistanceSq()) return damage;

            Vec3 eye = new Vec3(attacker.getX(), attacker.getY() + attacker.getEyeHeight(), attacker.getZ());
            Vec3 aim = attacker.getLookAngle();
            start = eye;
            end = eye.add(aim.scale(distSq));
        }

        boolean headless = HeadshotConfig.PLAYERS_HAVE_NO_HEADS.get() && victim instanceof Player;
        if (headless || !HeadBoxes.intersectsHead(victim, start, end)) {
            return damage * HeadshotConfig.BODYSHOT_DAMAGE_MULTIPLIER.get().floatValue();
        }

        float multiplier = HeadshotConfig.HEADSHOT_DAMAGE_MULTIPLIER.get().floatValue();

        ItemStack headgear = victim.getItemBySlot(EquipmentSlot.HEAD);
        if (!headgear.isEmpty()) {
            // Interpolate from the full multiplier down toward 1.0 as protection
            // rises, so a helmet can neutralise the bonus but never make a
            // headshot weaker than a body hit.
            float absorbed = HeadgearProtection.getProtection(headgear);
            multiplier = 1.0F + Math.max(multiplier - 1.0F, 0.0F) * absorbed;

            int durabilityLoss = (int) ((victim.getRandom().nextFloat() * 0.5D + 1.0D)
                    * damage * HeadshotConfig.HEADGEAR_DAMAGE_MULTIPLIER.get());
            headgear.hurtAndBreak(durabilityLoss, victim, ent -> ent.broadcastBreakEvent(EquipmentSlot.HEAD));
        }

        damage *= multiplier;

        // A lethal headshot on a big slime collapses it to the smallest size so it
        // dies outright instead of splitting into a fresh wave of children.
        if (victim.getHealth() < damage && victim instanceof Slime slime && slime.getSize() > 1) {
            slime.setSize(1, false);
        }

        spawnFeedback(serverLevel, victim, source);
        return damage;
    }

    /** Optional cosmetics. Off by default — GC's headshots are silent and invisible. */
    private static void spawnFeedback(ServerLevel level, LivingEntity victim, DamageSource source) {
        if (HeadshotConfig.SHOW_PARTICLES.get()) {
            level.sendParticles(ParticleTypes.CRIT,
                    victim.getX(), victim.getY() + victim.getEyeHeight(), victim.getZ(),
                    10, 0.2D, 0.2D, 0.2D, 0.1D);
        }
        if (HeadshotConfig.PLAY_HIT_SOUND.get() && source.getEntity() instanceof ServerPlayer shooter) {
            shooter.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                    HeadshotConfig.HIT_SOUND_VOLUME.get().floatValue(),
                    HeadshotConfig.HIT_SOUND_PITCH.get().floatValue());
        }
    }
}
