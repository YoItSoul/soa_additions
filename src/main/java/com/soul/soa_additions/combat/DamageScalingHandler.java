package com.soul.soa_additions.combat;

import com.mojang.logging.LogUtils;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Port of GreedyCraft's {@code events/onEntityLivingHurt.zs}.
 *
 * <p>This previously lived in {@code kubejs/server_scripts/soa_entity_hurt.js}
 * on {@code EntityEvents.hurt}, where it never worked: KubeJS 1.20.1's
 * {@code LivingEntityHurtEventJS} exposes {@code getDamage()} and <em>no</em>
 * setter, so every {@code event.damage = ...} threw "has no public instance
 * field or method named damage" and aborted the handler. Forge's
 * {@link LivingHurtEvent} has {@code setAmount}, which is what GC's 1.12
 * handler was bound to in the first place.</p>
 *
 * <p>Rules, in GC's original order:</p>
 * <ol>
 *   <li>Burning undead in daylight take +5% max HP extra fire damage.</li>
 *   <li>Player thorns retaliation capped at 50 damage AND 5% of target max HP,
 *       and zeroed entirely against blacklisted bosses.</li>
 *   <li>Spider/cave spider hits stack Slowness on the player; at the cap they
 *       also web the player's feet.</li>
 *   <li>Explosion damage scales 2.5x on stage {@code nether} and 3.0x on stage
 *       {@code hardmode} — both branches multiply, so a hardmode player (who
 *       also holds {@code nether}) takes 7.5x. Preserved verbatim.</li>
 *   <li>Projectile damage scales with difficulty; skeleton arrows hit 2x.</li>
 *   <li>Boss damage (max HP >= 100) scales with difficulty.</li>
 * </ol>
 *
 * <p>GC rule 7 (Eldritch Guardian damage cap) is intentionally absent —
 * Thaumcraft is not in this pack.</p>
 *
 * <p>Runs at {@link EventPriority#LOW} so the thorns cap in rule 2 is a real
 * cap: anything that inflates damage at NORMAL — {@link HeadshotHandler}, for
 * one — has already run by the time it applies. The rule 4/5/6 coefficients are
 * plain multipliers, so running late costs them nothing.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class DamageScalingHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Written by {@code soa_world_events.js} whenever a stage is gained (and as
     * a login repair pass) — the same 0..2400 value it pushes into
     * ScalingHealth via {@code /sh_difficulty}. GC read {@code player.difficulty}
     * off ScalingHealth directly; this mirrors it without an API bridge.
     */
    private static final String DIFFICULTY_TAG = "soa_sh_difficulty";

    /** Bosses excluded from per-difficulty scaling — fights become unwinnable. */
    private static final Set<String> DAMAGE_SCALING_BLACKLIST = Set.of(
            "twilightforest:naga",
            "twilightforest:lich",
            "twilightforest:ur_ghast",
            "twilightforest:hydra",
            "twilightforest:final_boss",
            "twilightforest:knight_phantom",
            "twilightforest:minoshroom",
            "twilightforest:alpha_yeti",
            "twilightforest:snow_queen",
            "twilightforest:goblin_knight_upper",
            "mowziesmobs:frostmaw",
            "mowziesmobs:umvuthi",
            "mowziesmobs:ferrous_wroughtnaut",
            "mowziesmobs:naga",
            "minecraft:ender_dragon",
            "minecraft:wither",
            "minecraft:warden",
            "aether:slider",
            "aether:valkyrie_queen",
            "aether:fire_minion",
            "aether:sun_spirit",
            "deep_aether:nightmare_walker");

    private static final Set<String> SKELETON_TYPES = Set.of(
            "minecraft:skeleton",
            "minecraft:wither_skeleton",
            "minecraft:stray",
            "minecraft:bogged");

    private static Method hasStageMethod;
    private static boolean gameStagesResolved;

    private DamageScalingHandler() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) return;

        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        Level level = victim.level();
        float amount = event.getAmount();

        // (1) Burning undead in sunlight take bonus fire damage.
        if (victim.getMobType() == MobType.UNDEAD && victim.isOnFire()
                && victim.getMaxHealth() > 0.0F
                && level.canSeeSky(victim.blockPosition()) && level.isDay()
                && source.is(DamageTypeTags.IS_FIRE)) {
            amount += victim.getMaxHealth() / 20.0F;
        }

        // (2) Thorns cap, player attribution only.
        Entity attacker = source.getEntity();
        if (attacker instanceof Player && !source.is(DamageTypeTags.IS_PROJECTILE)
                && "thorns".equals(source.getMsgId())) {
            if (DAMAGE_SCALING_BLACKLIST.contains(typeId(victim))) {
                event.setCanceled(true);
                return;
            }
            if (amount > 50.0F) amount = 50.0F;
            float fivePercent = victim.getMaxHealth() * 0.05F;
            if (victim.getMaxHealth() > 0.0F && amount > fivePercent) amount = fivePercent;
        }

        // Everything below applies only to players taking the hit.
        if (!(victim instanceof Player player)) {
            event.setAmount(amount);
            return;
        }

        String attackerType = attacker != null ? typeId(attacker) : null;
        int difficulty = player.getPersistentData().getInt(DIFFICULTY_TAG);

        // (3) Spiders stack slowness, and web the player's feet at the cap.
        if ("minecraft:spider".equals(attackerType) || "minecraft:cave_spider".equals(attackerType)) {
            applySpiderSlowness(player, level);
        }

        // (4) Stage-scaled explosion damage.
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            if (hasStage(player, "nether")) amount *= 2.5F;
            if (hasStage(player, "hardmode")) amount *= 3.0F;
        }

        boolean livingAttacker = attacker instanceof LivingEntity;
        boolean scalable = livingAttacker && !DAMAGE_SCALING_BLACKLIST.contains(attackerType);

        // (5) Projectile damage scales with difficulty; skeleton arrows 2x.
        if (scalable && source.is(DamageTypeTags.IS_PROJECTILE)) {
            amount *= (1.0F + 0.003F * difficulty);
            if (SKELETON_TYPES.contains(attackerType)) amount *= 2.0F;
        }

        // (6) Boss damage scales with difficulty.
        if (scalable && ((LivingEntity) attacker).getMaxHealth() >= 100.0F) {
            amount *= (1.0F + 0.0032F * difficulty);
        }

        event.setAmount(amount);
    }

    private static void applySpiderSlowness(Player player, Level level) {
        MobEffectInstance existing = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (existing == null) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0, false, false));
            return;
        }
        if (player.getRandom().nextFloat() >= 0.66F) return;

        int level_ = existing.getAmplifier();
        if (level_ < 3) {
            int inc = player.getRandom().nextFloat() < 0.33F ? 2 : 1;
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 200, level_ + inc, false, false));
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4, false, false));
        BlockPos here = player.blockPosition();
        if (level.getBlockState(here).isAir()) {
            level.setBlock(here, Blocks.COBWEB.defaultBlockState(), 3);
        }
    }

    private static String typeId(Entity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null ? key.toString() : "";
    }

    /**
     * GameStages is {@code compileOnly}, so this goes through reflection like
     * the rest of the codebase. The lookup is cached — this sits on the hot
     * path of every explosion hit.
     */
    private static boolean hasStage(Player player, String stage) {
        if (!gameStagesResolved) {
            gameStagesResolved = true;
            try {
                hasStageMethod = Class.forName("net.darkhax.gamestages.GameStageHelper")
                        .getMethod("hasStage", Player.class, String.class);
            } catch (ReflectiveOperationException e) {
                LOGGER.warn("[soa] GameStages absent; explosion stage scaling disabled");
            }
        }
        if (hasStageMethod == null) return false;
        try {
            return (boolean) hasStageMethod.invoke(null, player, stage);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
