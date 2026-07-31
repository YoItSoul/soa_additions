package com.soul.soa_additions.combat;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Per-species head hitboxes, ported from GreedyCraft's iblis-headshots so the
 * "did that hit the head" answer matches the 1.12 pack shot for shot.
 *
 * <p>Boxes are stored in normalised entity-space: {@code 0.0} is the min corner
 * of the entity's bounding box and {@code 1.0} the max corner on each axis.
 * {@link #shrinkTo} rescales them onto the live bounding box, so the same table
 * works for a baby zombie, an adult, and anything a mod scales oddly. Values
 * above {@code 1.0} are deliberate — a cow's muzzle and a spider's head stick
 * out past the collision box.</p>
 */
public final class HeadBoxes {

    private static final AABB SLIME_CORE   = new AABB(0.4D, 0.4D, 0.4D, 0.6D, 0.6D, 0.6D);
    private static final AABB SHULKER_CORE = new AABB(0.4D, 0.1D, 0.4D, 0.6D, 0.4D, 0.6D);
    private static final AABB HUMANOID     = new AABB(0.1D, 0.8D, 0.1D, 0.9D, 1.0D, 0.9D);
    private static final AABB HUSK         = new AABB(0.1D, 0.8D, 0.1D, 0.9D, 1.1D, 0.9D);
    private static final AABB SPIDER       = new AABB(0.6D, 0.5D, 0.3D, 1.2D, 1.2D, 0.7D);
    private static final AABB CHICKEN      = new AABB(0.9D, 0.8D, 0.3D, 1.4D, 1.4D, 0.7D);
    private static final AABB COW          = new AABB(0.9D, 0.7D, 0.2D, 1.4D, 1.2D, 0.8D);
    private static final AABB HORSE        = new AABB(0.7D, 0.7D, 0.2D, 1.0D, 1.0D, 0.8D);
    private static final AABB GUARDIAN_EYE = new AABB(0.8D, 0.4D, 0.4D, 1.0D, 0.6D, 0.6D);
    private static final AABB GHAST_EYES   = new AABB(0.8D, 0.6D, 0.2D, 1.0D, 0.7D, 0.8D);
    private static final AABB POLAR_BEAR   = new AABB(1.0D, 0.6D, 0.2D, 1.5D, 1.0D, 0.8D);

    private HeadBoxes() {}

    /** True when the segment {@code from -> to} passes through the target's head box. */
    public static boolean intersectsHead(LivingEntity target, Vec3 from, Vec3 to) {
        AABB head = headBox(target);
        return head != null && head.clip(from, to).isPresent();
    }

    /** The target's head box in world space, or {@code null} for entities that have no head. */
    @Nullable
    public static AABB headBox(LivingEntity entity) {
        AABB bounds = entity.getBoundingBox();

        if (entity instanceof Slime) return shrinkTo(bounds, SLIME_CORE);
        if (entity instanceof Shulker) return shrinkTo(bounds, SHULKER_CORE);

        // Headless by design — these can never be headshot, in GC or here.
        if (entity instanceof Bat || entity instanceof Endermite || entity instanceof Ocelot
                || entity instanceof Parrot || entity instanceof Silverfish || entity instanceof Squid) {
            return null;
        }

        float yaw = entity.yBodyRot;
        if (entity instanceof Spider) return shrinkTo(bounds, rotateAroundY(SPIDER, yaw));
        if (entity instanceof Chicken || entity instanceof Rabbit) return shrinkTo(bounds, rotateAroundY(CHICKEN, yaw));
        if (entity instanceof Cow || entity instanceof Pig || entity instanceof Sheep) return shrinkTo(bounds, rotateAroundY(COW, yaw));
        if (entity instanceof AbstractHorse) return shrinkTo(bounds, rotateAroundY(HORSE, yaw));
        if (entity instanceof Guardian) return shrinkTo(bounds, rotateAroundY(GUARDIAN_EYE, yaw));
        if (entity instanceof Ghast) return shrinkTo(bounds, rotateAroundY(GHAST_EYES, yaw));

        if (entity instanceof PolarBear bear) {
            AABB box = shrinkTo(bounds, rotateAroundY(POLAR_BEAR, yaw));
            // Rearing up lifts the head out of the resting box.
            return bear.attackAnim > 0.2F ? box.move(0.0D, bear.attackAnim, 0.0D) : box;
        }
        if (entity instanceof Wolf) return shrinkTo(bounds, rotateAroundY(POLAR_BEAR, yaw));

        return shrinkTo(bounds, entity instanceof Husk ? HUSK : HUMANOID);
    }

    /**
     * Spin a normalised head box about the entity's centre so forward-projecting
     * heads follow the body. Reproduces GC's transform verbatim, quirks included:
     * it mixes sin/cos in a way a textbook rotation wouldn't, and every shipped
     * head box is symmetric enough that the difference never showed. Changing it
     * to a "correct" rotation would shift where snouts sit, so it stays as-is.
     */
    private static AABB rotateAroundY(AABB headBox, float yaw) {
        float cos = Mth.cos(-yaw * ((float) Math.PI / 180F));
        float sin = Mth.sin(-yaw * ((float) Math.PI / 180F));

        float dMinX = (float) (headBox.minX - 0.5D);
        float dMaxX = (float) (headBox.maxX - 0.5D);
        float dMinZ = (float) (headBox.minZ - 0.5D);
        float dMaxZ = (float) (headBox.maxZ - 0.5D);

        float x00 = 0.5F + sin * dMinX + cos * dMinZ;
        float z00 = 0.5F + cos * dMinX + sin * dMinZ;
        float x11 = 0.5F + sin * dMaxX + cos * dMaxZ;
        float z11 = 0.5F + cos * dMaxX + sin * dMaxZ;
        float x10 = 0.5F + sin * dMaxX + cos * dMinZ;
        float z10 = 0.5F + cos * dMaxX + sin * dMinZ;
        float x01 = 0.5F + sin * dMinX + cos * dMaxZ;
        float z01 = 0.5F + cos * dMinX + sin * dMaxZ;

        return new AABB(
                min(x00, x10, x01, x11), headBox.minY, min(z00, z10, z01, z11),
                max(x00, x10, x01, x11), headBox.maxY, max(z00, z10, z01, z11)
        );
    }

    /** Map a normalised box onto the entity's real bounding box. */
    private static AABB shrinkTo(AABB bounds, AABB normalised) {
        double sizeX = bounds.maxX - bounds.minX;
        double sizeY = bounds.maxY - bounds.minY;
        double sizeZ = bounds.maxZ - bounds.minZ;
        return new AABB(
                bounds.minX + normalised.minX * sizeX,
                bounds.minY + normalised.minY * sizeY,
                bounds.minZ + normalised.minZ * sizeZ,
                bounds.minX + normalised.maxX * sizeX,
                bounds.minY + normalised.maxY * sizeY,
                bounds.minZ + normalised.maxZ * sizeZ
        );
    }

    private static float min(float a, float b, float c, float d) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    private static float max(float a, float b, float c, float d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }
}
