package com.soul.soa_additions.donor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Renders the donor orb as a glowing billboard quad with a soft pulsing
 * effect. Uses {@link RenderType#endGateway()} for the shimmer, plus an
 * additive translucent quad for the glow halo.
 */
@OnlyIn(Dist.CLIENT)
public class DonorOrbRenderer extends EntityRenderer<DonorOrbEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/environment/end_gateway_beam.png");

    public DonorOrbRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0f;
        this.shadowStrength = 0f;
    }

    @Override
    public ResourceLocation getTextureLocation(DonorOrbEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(DonorOrbEntity entity, float yaw, float pt,
                       PoseStack pose, MultiBufferSource buffers, int light) {
        int color = entity.getOrbColor();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Pulsing alpha
        float pulse = 0.7f + 0.3f * (float) Math.sin(entity.tickCount * 0.1 + entity.getId());
        float alpha = 0.9f * pulse;

        pose.pushPose();

        // Billboard — always face the camera
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());

        // Slow rotation for visual interest
        float spin = (entity.tickCount + pt) * 2f;
        pose.mulPose(Axis.ZP.rotationDegrees(spin));

        float size = 0.35f * pulse;

        // Inner bright core (full brightness)
        renderQuad(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE)),
                size * 0.5f, r, g, b, 1.0f, 0xF000F0);

        // Outer glow halo (larger, more transparent)
        renderQuad(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE)),
                size, r, g, b, alpha * 0.5f, 0xF000F0);

        // Outermost soft glow
        renderQuad(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE)),
                size * 1.8f, r, g, b, alpha * 0.15f, 0xF000F0);

        pose.popPose();

        // No shadow, no fire
    }

    private static void renderQuad(PoseStack pose, VertexConsumer vc,
                                    float halfSize, float r, float g, float b, float a,
                                    int packedLight) {
        PoseStack.Pose entry = pose.last();
        Matrix4f mat = entry.pose();
        Matrix3f normal = entry.normal();

        vc.vertex(mat, -halfSize, -halfSize, 0)
                .color(r, g, b, a)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();
        vc.vertex(mat, halfSize, -halfSize, 0)
                .color(r, g, b, a)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();
        vc.vertex(mat, halfSize, halfSize, 0)
                .color(r, g, b, a)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();
        vc.vertex(mat, -halfSize, halfSize, 0)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();
    }
}
