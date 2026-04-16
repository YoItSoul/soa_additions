package com.soul.soa_additions.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Third-person shooting assist: draws a dashed trajectory arc for the arrow
 * that would be released if the player let go of the bow right now. Only
 * active in third-person camera because the first-person view already lines
 * up the bowstring with the crosshair — the assist exists to replace that
 * feedback when the camera is pulled back.
 *
 * <p>Simulation mirrors vanilla arrow physics (drag 0.99, gravity 0.05,
 * inherited shooter motion) so the rendered line tracks the <i>centerline</i>
 * of the actual shot. Inaccuracy is a small Gaussian on release, which we
 * deliberately ignore — we want a stable aim assist, not jitter.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BowTrajectoryRenderer {

    private static final int MAX_TICKS = 120;         // ~6 seconds of flight
    private static final float AIR_DRAG = 0.99F;
    private static final float ARROW_GRAVITY = 0.05F;
    private static final float MIN_POWER = 0.1F;      // don't render for tap-to-cancel pulls
    private static final float END_MARK_RADIUS = 0.15F;

    private BowTrajectoryRenderer() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        if (!player.isUsingItem()) return;
        ItemStack using = player.getUseItem();
        if (!(using.getItem() instanceof BowItem)) return;

        int ticksUsing = using.getUseDuration() - player.getUseItemRemainingTicks();
        float power = BowItem.getPowerForTime(ticksUsing);
        if (power < MIN_POWER) return;

        List<Vec3> points = simulateTrajectory(player, power, mc.level, event.getPartialTick());
        if (points.size() < 2) return;

        renderTrajectory(event.getPoseStack(), event.getCamera().getPosition(), points);
    }

    /** Simulate arrow physics from the player's current pose. Returns a list
     *  of world-space points at tick granularity — the renderer will join
     *  them into line segments. Terminates early on the first block hit. */
    private static List<Vec3> simulateTrajectory(LocalPlayer player, float power, Level level, float partialTick) {
        List<Vec3> out = new ArrayList<>(MAX_TICKS);

        // Arrow spawn point mirrors net.minecraft.world.entity.projectile.Arrow's
        // constructor: (shooter.x, shooter.eyeY - 0.1, shooter.z). We use the
        // interpolated render position so the line meets the bow tip cleanly
        // instead of lagging one tick behind.
        double px = player.xo + (player.getX() - player.xo) * partialTick;
        double py = player.yo + (player.getY() - player.yo) * partialTick + player.getEyeHeight() - 0.1;
        double pz = player.zo + (player.getZ() - player.zo) * partialTick;
        Vec3 pos = new Vec3(px, py, pz);

        float xRot = player.getXRot();
        float yRot = player.getYRot();
        // shootFromRotation: direction from look angles, normalized by shoot().
        float dx = -sinDeg(yRot) * cosDeg(xRot);
        float dy = -sinDeg(xRot);
        float dz = cosDeg(yRot) * cosDeg(xRot);
        Vec3 dir = new Vec3(dx, dy, dz).normalize();
        float speed = 3.0F * power;
        Vec3 vel = dir.scale(speed);
        // Arrow inherits shooter's horizontal motion (and vertical too, if
        // airborne). Matches AbstractArrow.shootFromRotation tail.
        Vec3 shooterMotion = player.getDeltaMovement();
        vel = vel.add(shooterMotion.x, player.onGround() ? 0.0 : shooterMotion.y, shooterMotion.z);

        out.add(pos);
        for (int i = 0; i < MAX_TICKS; i++) {
            Vec3 next = pos.add(vel);
            BlockHitResult hit = level.clip(new ClipContext(
                    pos, next,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player));
            if (hit.getType() != HitResult.Type.MISS) {
                out.add(hit.getLocation());
                return out;
            }
            // Drag then gravity, matching AbstractArrow.tick() ordering.
            vel = vel.scale(AIR_DRAG);
            vel = new Vec3(vel.x, vel.y - ARROW_GRAVITY, vel.z);
            pos = next;
            out.add(pos);
        }
        return out;
    }

    private static void renderTrajectory(PoseStack pose, Vec3 camPos, List<Vec3> points) {
        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();

        MultiBufferSource.BufferSource bufs = Minecraft.getInstance().renderBuffers().bufferSource();
        // Keep the line visible even when the sim clips into terrain near the
        // impact — the useful information is "where will it land", not hidden
        // surface removal.
        RenderSystem.lineWidth(2.5F);
        VertexConsumer buf = bufs.getBuffer(RenderType.lines());

        // Dashed look: draw every other segment. Alpha fades out along the
        // arc to communicate "further = less certain".
        int n = points.size();
        for (int i = 0; i < n - 1; i += 2) {
            Vec3 a = points.get(i);
            Vec3 b = points.get(Math.min(i + 1, n - 1));
            float t = (float) i / Math.max(1, n - 1);
            float alpha = lerp(0.95F, 0.25F, t);
            // White → pale orange as the shot gets further, so the arc reads
            // as a single object instead of a noisy line.
            float r = 1.0F;
            float g = lerp(1.0F, 0.75F, t);
            float bcol = lerp(1.0F, 0.45F, t);
            vertex(buf, matrix, normal, a, r, g, bcol, alpha);
            vertex(buf, matrix, normal, b, r, g, bcol, alpha);
        }

        // Impact marker: small crosshair at the final point.
        Vec3 end = points.get(n - 1);
        float mr = END_MARK_RADIUS;
        vertex(buf, matrix, normal, end.add(-mr, 0, 0), 1f, 0.6f, 0.3f, 0.9f);
        vertex(buf, matrix, normal, end.add(mr, 0, 0),  1f, 0.6f, 0.3f, 0.9f);
        vertex(buf, matrix, normal, end.add(0, -mr, 0), 1f, 0.6f, 0.3f, 0.9f);
        vertex(buf, matrix, normal, end.add(0, mr, 0),  1f, 0.6f, 0.3f, 0.9f);
        vertex(buf, matrix, normal, end.add(0, 0, -mr), 1f, 0.6f, 0.3f, 0.9f);
        vertex(buf, matrix, normal, end.add(0, 0, mr),  1f, 0.6f, 0.3f, 0.9f);

        bufs.endBatch(RenderType.lines());
        RenderSystem.lineWidth(1.0F);
        pose.popPose();
    }

    private static void vertex(VertexConsumer buf, Matrix4f matrix, Matrix3f normal,
                               Vec3 p, float r, float g, float b, float a) {
        buf.vertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .color(r, g, b, a)
                .normal(normal, 0f, 1f, 0f)
                .endVertex();
    }

    private static float sinDeg(float deg) { return (float) Math.sin(Math.toRadians(deg)); }
    private static float cosDeg(float deg) { return (float) Math.cos(Math.toRadians(deg)); }
    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
}
