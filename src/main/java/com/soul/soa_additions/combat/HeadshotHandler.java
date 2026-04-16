package com.soul.soa_additions.combat;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.HeadshotConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Adds headshot mechanics: hits landing near the target's eye height get damage multipliers,
 * apply blindness / slowness / nausea, spawn crit particles, and consume helmets.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class HeadshotHandler {

    private static final double HEAD_HITBOX_HEIGHT = 0.3D;
    private static final float BASE_HEADSHOT_MULTIPLIER = 2.0F;
    private static final float CRITICAL_HEADSHOT_MULTIPLIER = 3.0F;
    private static final float VELOCITY_DAMAGE_MULTIPLIER = 0.2F;
    private static final int SLOWNESS_DURATION = 40;
    private static final int HELMET_BREAK_NAUSEA_DURATION = 80;

    private HeadshotHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!isValidHeadshotScenario(event)) return;

        LivingEntity target = event.getEntity();
        LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
        Vec3 attackPos = event.getSource().getSourcePosition();
        if (attackPos == null || !isHeadshot(target, attackPos)) return;

        boolean playerHadHelmet = target instanceof Player p
                && !p.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                && p.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArmorItem;

        // event.getAmount() is already post-armor/toughness/protection — vanilla
        // runs those reductions before LivingHurtEvent fires. processHeadshot
        // therefore works on already-reduced damage and the helmet profile is a
        // direct multiplier on that. No further armor math here; re-applying it
        // was a double-reduction bug.
        float damage = processHeadshot(event, target, attacker);
        event.setAmount(Math.max(0.0F, damage));
        applyHeadshotEffects(target, playerHadHelmet);
        playHeadshotSounds(target, attacker, playerHadHelmet);
        spawnHeadshotParticles(target);
    }

    private static boolean isValidHeadshotScenario(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity)) return false;
        return !source.is(DamageTypes.FALL)
                && !source.is(DamageTypes.DROWN)
                && !source.is(DamageTypes.IN_FIRE)
                && !source.is(DamageTypes.ON_FIRE)
                && !source.is(DamageTypes.LAVA)
                && !source.is(DamageTypes.MAGIC)
                && !source.is(DamageTypes.STARVE);
    }

    private static boolean isHeadshot(LivingEntity target, Vec3 attackPos) {
        double headY = target.position().y + target.getEyeHeight();
        return Math.abs(attackPos.y - headY) <= HEAD_HITBOX_HEIGHT;
    }

    private static float processHeadshot(LivingHurtEvent event, LivingEntity target, LivingEntity attacker) {
        float baseDamage = event.getAmount();
        float multiplier = calculateHeadshotMultiplier(attacker);

        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
            multiplier += (float) (arrow.getDeltaMovement().length() * VELOCITY_DAMAGE_MULTIPLIER);
        }

        float rawHeadshotDamage = baseDamage * multiplier;

        // Player targets route the hit through their configured helmet profile:
        // the helmet soaks most of the blow (durability loss scaled per-profile)
        // and only the remainder reaches the player. The result is floored at
        // baseDamage so a headshot can never hurt *less* than a body shot, no
        // matter how absorbent the helmet profile is.
        if (target instanceof Player player) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty() && helmet.getItem() instanceof ArmorItem armor) {
                HeadshotConfig.Profile prof = HeadshotConfig.profileFor(armor);
                int durabilityLoss = Math.max(1, Math.round(rawHeadshotDamage * prof.durabilityMult()));
                damageHelmet(player, durabilityLoss);
                return Math.max(baseDamage, rawHeadshotDamage * prof.damageTakenMult());
            }
            // No helmet — player eats the full headshot damage.
            return rawHeadshotDamage;
        }

        ItemStack helmet = target.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.isEmpty() ? rawHeadshotDamage : baseDamage;
    }

    private static float calculateHeadshotMultiplier(LivingEntity attacker) {
        return attacker instanceof Player && attacker.isCrouching()
                ? CRITICAL_HEADSHOT_MULTIPLIER
                : BASE_HEADSHOT_MULTIPLIER;
    }

    private static void damageHelmet(Player player, int durabilityLoss) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || !(helmet.getItem() instanceof ArmorItem)) return;
        helmet.hurtAndBreak(durabilityLoss, player, p -> {
            p.broadcastBreakEvent(EquipmentSlot.HEAD);
            p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, HELMET_BREAK_NAUSEA_DURATION));
            playHelmetBreakSound(p);
        });
    }

    private static void applyHeadshotEffects(LivingEntity target, boolean playerHadHelmet) {
        if (target.isDeadOrDying()) return;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION));
        // A helmeted player is protected from the visual disorientation — the
        // helmet is meant to mitigate the hit, not just the raw damage. Mobs
        // and unhelmeted players still get blinded so the hit has teeth.
        if (target instanceof Player && playerHadHelmet) return;
        int seconds = HeadshotConfig.NO_HELMET_BLINDNESS_SECONDS.get();
        int amplifier = HeadshotConfig.NO_HELMET_BLINDNESS_AMPLIFIER.get();
        if (seconds > 0) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, seconds * 20, amplifier));
        }
    }

    /** Play the three headshot sounds:
     *  - a quiet "ding" delivered only to the attacker as successful-hit feedback,
     *  - the long-standing hurt sound broadcast at the target,
     *  - a low thump broadcast at the target when they're an unhelmeted player. */
    private static void playHeadshotSounds(LivingEntity target, LivingEntity attacker, boolean playerHadHelmet) {
        playHeadshotSound(target);
        if (attacker instanceof ServerPlayer sp) {
            sp.playNotifySound(
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    HeadshotConfig.HEADSHOT_DING_VOLUME.get().floatValue(),
                    HeadshotConfig.HEADSHOT_DING_PITCH.get().floatValue()
            );
        }
        if (target instanceof Player && !playerHadHelmet) {
            playThumpSound(target);
        }
    }

    private static void playHeadshotSound(LivingEntity target) {
        if (target.level() instanceof ServerLevel level) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private static void playThumpSound(LivingEntity target) {
        if (target.level() instanceof ServerLevel level) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.PLAYERS,
                    HeadshotConfig.THUMP_VOLUME.get().floatValue(),
                    HeadshotConfig.THUMP_PITCH.get().floatValue());
        }
    }

    private static void playHelmetBreakSound(Player player) {
        Level level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void spawnHeadshotParticles(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        double x = target.getX();
        double y = target.getY() + target.getEyeHeight();
        double z = target.getZ();
        level.sendParticles(ParticleTypes.CRIT, x, y, z, 20, 0.2, 0.2, 0.2, 0.1);
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 15, 0.1, 0.1, 0.1, 0.05);
    }
}
