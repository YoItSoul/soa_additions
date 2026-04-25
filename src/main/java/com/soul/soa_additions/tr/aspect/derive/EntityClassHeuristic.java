package com.soul.soa_additions.tr.aspect.derive;

import com.soul.soa_additions.tr.core.Aspect;
import com.soul.soa_additions.tr.core.AspectStack;
import com.soul.soa_additions.tr.core.Aspects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.npc.AbstractVillager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Property-driven entity aspect inference. Far richer than the original
 * "Enemy → telum, Animal → bestia" two-line heuristic — each mob gets
 * aspects derived from:
 * <ul>
 *   <li>Mob-type / family classification (undead, arthropod, illager,
 *       aquatic, villager, boss, animal, etc.)</li>
 *   <li>Physics traits (fire-immune → ignis, no-gravity / FlyingMob → aer
 *       + volatus, can swim → aqua)</li>
 *   <li>Attributes (high attack → +telum, high max health → +potentia,
 *       high armor → +tutamen)</li>
 *   <li>Size (large bounding box → +potentia)</li>
 *   <li>Special class membership (Creeper → +ignis +perditio, Slime →
 *       +limus, Shulker → +alienis +tutamen, etc.)</li>
 * </ul>
 *
 * <p>Combines with the JSON layer rather than overriding — JSON values are
 * "designer-intentional" extra flavor; this heuristic is the property-derived
 * baseline. Both sides see identical results because everything queried
 * lives on the synced entity (no loot-table or server-only data needed).
 */
public final class EntityClassHeuristic {

    private EntityClassHeuristic() {}

    public static List<AspectStack> infer(Entity entity) {
        if (entity == null) return List.of();
        Map<Aspect, Integer> sum = new HashMap<>();

        // -------- Family-level classification --------
        // (Tag-derivable in 1.20.1 if we wanted, but instanceof gives us
        //  tighter coupling to behaviour we actually care about.)

        if (isUndead(entity)) {
            add(sum, Aspects.MORTUUS, 3);
            add(sum, Aspects.CORPUS, 1);
            add(sum, Aspects.EXANIMIS, 1);
        }
        if (isArthropod(entity)) {
            add(sum, Aspects.VENENUM, 1);
            add(sum, Aspects.BESTIA, 1);
            add(sum, Aspects.MOTUS, 1);
        }
        if (isIllager(entity)) {
            add(sum, Aspects.HUMANUS, 2);
            add(sum, Aspects.TELUM, 2);
            add(sum, Aspects.PERDITIO, 1);
        }
        if (entity instanceof AbstractVillager) {
            add(sum, Aspects.HUMANUS, 2);
            add(sum, Aspects.LUCRUM, 2);
            add(sum, Aspects.COGNITIO, 1);
        }
        if (entity instanceof EnderDragon || entity instanceof WitherBoss) {
            add(sum, Aspects.PRAECANTATIO, 3);
            add(sum, Aspects.MORTUUS, 2);
            add(sum, Aspects.POTENTIA, 3);
            add(sum, Aspects.TENEBRAE, 1);
        }
        if (entity instanceof WaterAnimal) {
            add(sum, Aspects.AQUA, 3);
            add(sum, Aspects.BESTIA, 1);
        }
        if (entity instanceof Animal) {
            // Most "peaceful animal" mobs.
            add(sum, Aspects.BESTIA, 2);
            add(sum, Aspects.VICTUS, 2);
        }
        if (entity instanceof Enemy && !isUndead(entity) && !isIllager(entity)) {
            // Generic hostile fallback — only if no more specific family hit.
            add(sum, Aspects.TELUM, 1);
            add(sum, Aspects.PERDITIO, 1);
            add(sum, Aspects.MORTUUS, 1);
        }

        // -------- Special creature types --------
        if (entity instanceof Creeper) {
            add(sum, Aspects.IGNIS, 2);
            add(sum, Aspects.PERDITIO, 3);
            add(sum, Aspects.HERBA, 1);
        }
        if (entity instanceof Slime) {
            add(sum, Aspects.LIMUS, 3);
            add(sum, Aspects.AQUA, 1);
            add(sum, Aspects.PERDITIO, 1);
        }
        if (entity instanceof Ghast || entity instanceof Phantom) {
            add(sum, Aspects.TENEBRAE, 2);
            add(sum, Aspects.AURAM, 1);
            add(sum, Aspects.SPIRITUS, 2);
        }
        if (entity instanceof Vex) {
            add(sum, Aspects.SPIRITUS, 2);
            add(sum, Aspects.PRAECANTATIO, 1);
            add(sum, Aspects.TELUM, 1);
        }
        if (entity instanceof Shulker) {
            add(sum, Aspects.ALIENIS, 2);
            add(sum, Aspects.TUTAMEN, 2);
            add(sum, Aspects.METALLUM, 1);
        }
        if (entity instanceof Spider) {
            add(sum, Aspects.VENENUM, 2);
            add(sum, Aspects.MOTUS, 1);
            add(sum, Aspects.BESTIA, 1);
            add(sum, Aspects.PANNUS, 1);
        }

        // -------- Physics traits --------
        if (entity.fireImmune()) {
            add(sum, Aspects.IGNIS, 2);
        }
        if (entity.isNoGravity() || isFlyingMob(entity)) {
            add(sum, Aspects.AER, 2);
            add(sum, Aspects.VOLATUS, 2);
        }
        if (entity instanceof LivingEntity le && le.canBreatheUnderwater()) {
            add(sum, Aspects.AQUA, 1);
        }

        // -------- Attribute-driven --------
        if (entity instanceof LivingEntity le) {
            var attack = le.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attack != null && attack.getValue() >= 5.0) {
                add(sum, Aspects.TELUM, attack.getValue() >= 10 ? 2 : 1);
            }
            var health = le.getAttribute(Attributes.MAX_HEALTH);
            if (health != null && health.getValue() >= 30.0) {
                add(sum, Aspects.POTENTIA, health.getValue() >= 100 ? 3 : 1);
            }
            var armor = le.getAttribute(Attributes.ARMOR);
            if (armor != null && armor.getValue() >= 5.0) {
                add(sum, Aspects.TUTAMEN, 1);
            }
            var speed = le.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && speed.getValue() >= 0.30) {
                add(sum, Aspects.MOTUS, 1);
            }
            // Tamed by a player → vinculum + humanus
            if (le instanceof net.minecraft.world.entity.TamableAnimal ta && ta.isTame()) {
                add(sum, Aspects.VINCULUM, 1);
                add(sum, Aspects.HUMANUS, 1);
            }
        }

        // -------- Size-driven potentia --------
        var bb = entity.getBoundingBox();
        double maxDim = Math.max(bb.getXsize(), Math.max(bb.getYsize(), bb.getZsize()));
        if (maxDim >= 4.0) {
            add(sum, Aspects.POTENTIA, 2);
        } else if (maxDim >= 2.0) {
            add(sum, Aspects.POTENTIA, 1);
        }

        // -------- MobCategory fallback --------
        // Catches modded mobs that don't extend any of the vanilla classes
        // we checked above — guarantees at least *something* via the spawn
        // category Forge gave them.
        if (entity instanceof Mob mob && sum.isEmpty()) {
            MobCategory cat = mob.getType().getCategory();
            switch (cat) {
                case MONSTER -> { add(sum, Aspects.TELUM, 1); add(sum, Aspects.MORTUUS, 1); }
                case CREATURE -> { add(sum, Aspects.BESTIA, 1); add(sum, Aspects.VICTUS, 1); }
                case AMBIENT -> { add(sum, Aspects.AER, 1); add(sum, Aspects.SENSUS, 1); }
                case AXOLOTLS, UNDERGROUND_WATER_CREATURE, WATER_CREATURE, WATER_AMBIENT
                        -> { add(sum, Aspects.AQUA, 2); add(sum, Aspects.BESTIA, 1); }
                case MISC -> { add(sum, Aspects.MOTUS, 1); }
            }
        }

        // -------- Last-resort minimum --------
        if (sum.isEmpty() && entity instanceof LivingEntity) {
            add(sum, Aspects.VICTUS, 1);
            add(sum, Aspects.MOTUS, 1);
        }

        List<AspectStack> out = new ArrayList<>(sum.size());
        sum.forEach((a, amt) -> out.add(new AspectStack(a, amt)));
        return out;
    }

    private static void add(Map<Aspect, Integer> sum, Aspect a, int n) {
        sum.merge(a, n, Integer::sum);
    }

    // ---- Family checks ----

    private static boolean isUndead(Entity e) {
        if (!(e instanceof LivingEntity le)) return false;
        return le.getMobType() == net.minecraft.world.entity.MobType.UNDEAD;
    }

    private static boolean isArthropod(Entity e) {
        if (!(e instanceof LivingEntity le)) return false;
        return le.getMobType() == net.minecraft.world.entity.MobType.ARTHROPOD;
    }

    private static boolean isIllager(Entity e) {
        if (!(e instanceof LivingEntity le)) return false;
        return le.getMobType() == net.minecraft.world.entity.MobType.ILLAGER;
    }

    private static boolean isFlyingMob(Entity e) {
        // Vanilla FlyingMob covers Ghast, Phantom; many modded fliers extend it.
        return e instanceof net.minecraft.world.entity.FlyingMob
                || e instanceof Phantom
                || e instanceof Ghast
                || e instanceof net.minecraft.world.entity.ambient.Bat
                || e instanceof Vex;
    }
}
