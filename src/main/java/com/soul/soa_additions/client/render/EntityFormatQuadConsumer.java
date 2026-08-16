package com.soul.soa_additions.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Adapts a writer that emits {@code POSITION_COLOR_TEX_LIGHTMAP} vertices onto a
 * consumer that wants the entity format ({@code ..._OVERLAY_LIGHT_NORMAL}).
 *
 * <p>Quads are buffered a corner at a time and only handed to the delegate once all
 * four are in, because the missing normal is derived from the face itself: the cross
 * product of the quad's diagonals. Positions arrive already through the pose stack,
 * i.e. camera-relative with the camera at the origin, so the winding ambiguity is
 * resolved by pointing the normal back at the viewer — correct for every outward face
 * of a convex shape, which is all a cube ever shows.</p>
 *
 * <p>Overlay is filled with {@link OverlayTexture#NO_OVERLAY}; any overlay or normal
 * the caller supplies itself is ignored, since neither can survive the format change.</p>
 */
public final class EntityFormatQuadConsumer implements VertexConsumer {

    private final VertexConsumer delegate;

    private final double[] posX = new double[4];
    private final double[] posY = new double[4];
    private final double[] posZ = new double[4];
    private final int[] red = new int[4];
    private final int[] green = new int[4];
    private final int[] blue = new int[4];
    private final int[] alpha = new int[4];
    private final float[] texU = new float[4];
    private final float[] texV = new float[4];
    private final int[] lightU = new int[4];
    private final int[] lightV = new int[4];
    private int held;

    private double curX;
    private double curY;
    private double curZ;
    private int curRed = 255;
    private int curGreen = 255;
    private int curBlue = 255;
    private int curAlpha = 255;
    private float curU;
    private float curV;
    private int curLightU;
    private int curLightV;

    public EntityFormatQuadConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        this.curX = x;
        this.curY = y;
        this.curZ = z;
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        this.curRed = r;
        this.curGreen = g;
        this.curBlue = b;
        this.curAlpha = a;
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        this.curU = u;
        this.curV = v;
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        this.curLightU = u;
        this.curLightV = v;
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this;
    }

    @Override
    public void endVertex() {
        this.posX[this.held] = this.curX;
        this.posY[this.held] = this.curY;
        this.posZ[this.held] = this.curZ;
        this.red[this.held] = this.curRed;
        this.green[this.held] = this.curGreen;
        this.blue[this.held] = this.curBlue;
        this.alpha[this.held] = this.curAlpha;
        this.texU[this.held] = this.curU;
        this.texV[this.held] = this.curV;
        this.lightU[this.held] = this.curLightU;
        this.lightV[this.held] = this.curLightV;

        if (++this.held == 4) {
            this.held = 0;
            flushQuad();
        }
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        this.delegate.defaultColor(r, g, b, a);
    }

    @Override
    public void unsetDefaultColor() {
        this.delegate.unsetDefaultColor();
    }

    private void flushQuad() {
        double d1x = this.posX[2] - this.posX[0];
        double d1y = this.posY[2] - this.posY[0];
        double d1z = this.posZ[2] - this.posZ[0];
        double d2x = this.posX[3] - this.posX[1];
        double d2y = this.posY[3] - this.posY[1];
        double d2z = this.posZ[3] - this.posZ[1];

        double nx = d1y * d2z - d1z * d2y;
        double ny = d1z * d2x - d1x * d2z;
        double nz = d1x * d2y - d1y * d2x;
        double len = Math.sqrt(nx * nx + ny * ny + nz * nz);

        if (len < 1.0E-6D) {
            nx = 0.0D;
            ny = 1.0D;
            nz = 0.0D;
        } else {
            nx /= len;
            ny /= len;
            nz /= len;

            // Camera sits at the origin: a normal agreeing with the face's own
            // position vector points away from it, so the winding was the other way.
            double cx = (this.posX[0] + this.posX[1] + this.posX[2] + this.posX[3]) * 0.25D;
            double cy = (this.posY[0] + this.posY[1] + this.posY[2] + this.posY[3]) * 0.25D;
            double cz = (this.posZ[0] + this.posZ[1] + this.posZ[2] + this.posZ[3]) * 0.25D;
            if (nx * cx + ny * cy + nz * cz > 0.0D) {
                nx = -nx;
                ny = -ny;
                nz = -nz;
            }
        }

        float normX = (float) nx;
        float normY = (float) ny;
        float normZ = (float) nz;

        for (int i = 0; i < 4; i++) {
            this.delegate.vertex(this.posX[i], this.posY[i], this.posZ[i])
                    .color(this.red[i], this.green[i], this.blue[i], this.alpha[i])
                    .uv(this.texU[i], this.texV[i])
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(this.lightU[i], this.lightV[i])
                    .normal(normX, normY, normZ)
                    .endVertex();
        }
    }
}
