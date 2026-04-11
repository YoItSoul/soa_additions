package com.soul.soa_additions.combat;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
    private static final int BLINDNESS_DURATION = 60;
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

        float damage = processHeadshot(event, target, attacker);
        event.setAmount(calculateFinalDamage(event.getSource(), target, damage));
        applyHeadshotEffects(target);
        playHeadshotSound(target);
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

        // Player targets absorb the hit through their helmet (the helmet dies instead)
        if (target instanceof Player player) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty() && helmet.getItem() instanceof ArmorItem) {
                damageHelmet(player, baseDamage);
                return 0.0F;
            }
        }

        ItemStack helmet = target.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.isEmpty() ? baseDamage * multiplier : baseDamage;
    }

    private static float calculateHeadshotMultiplier(LivingEntity attacker) {
        return attacker instanceof Player && attacker.isCrouching()
                ? CRITICAL_HEADSHOT_MULTIPLIER
                : BASE_HEADSHOT_MULTIPLIER;
    }

    private static float calculateFinalDamage(DamageSource source, LivingEntity target, float damage) {
        if (damage <= 0.0F) return 0.0F;

        float armor = (float) target.getAttributeValue(Attributes.ARMOR);
        float armorToughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float enchantmentProtection = EnchantmentHelper.getDamageProtection(target.getArmorSlots(), source);

        float clampedArmor = Math.min(armor, 20.0F);
        float afterArmor = damage * (1.0F - clampedArmor / 25.0F);

        int toughnessFactor = (int) (armorToughness + armor * 2.0F);
        float ratio = Math.max(0.0F, damage / 4.0F);
        afterArmor += Math.max(0.0F, damage - ratio * toughnessFactor * 0.04F);

        afterArmor *= 1.0F - enchantmentProtection / 25.0F;
        return Math.max(afterArmor, 0.0F);
    }

    private static void damageHelmet(Player player, float damage) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || !(helmet.getItem() instanceof ArmorItem)) return;
        helmet.hurtAndBreak((int) Math.ceil(damage), player, p -> {
            p.broadcastBreakEvent(EquipmentSlot.HEAD);
            p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, HELMET_BREAK_NAUSEA_DURATION));
            playHelmetBreakSound(p);
        });
    }

    private static void applyHeadshotEffects(LivingEntity target) {
        if (target.isDeadOrDying()) return;
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_DURATION));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION));
    }

    private static void playHeadshotSound(LivingEntity target) {
        if (target.level() instanceof ServerLevel level) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 1.0F);
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
