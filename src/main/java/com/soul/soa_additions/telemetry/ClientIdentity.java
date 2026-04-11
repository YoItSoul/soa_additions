package com.soul.soa_additions.telemetry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Side-safe accessors for client-only data. We cannot import Minecraft client classes
 * directly from {@link Telemetry} because that class runs on both sides; touching
 * {@code net.minecraft.client.Minecraft} on a dedicated server would NoClassDefFoundError.
 *
 * <p>All methods return {@code null} on the dedicated server and swallow any errors.
 */
public final class ClientIdentity {

    private ClientIdentity() {}

    public static String getUsernameOrNull() {
        if (FMLEnvironment.dist != Dist.CLIENT) return null;
        try {
            return ClientAccess.username();
        } catch (Throwable t) {
            return null;
        }
    }

    public static String getUuidOrNull() {
        if (FMLEnvironment.dist != Dist.CLIENT) return null;
        try {
            return ClientAccess.uuid();
        } catch (Throwable t) {
            return null;
        }
    }

    private static volatile Telemetry.GpuInfo cachedGpu;

    public static Telemetry.GpuInfo getGpuInfoOrNull() {
        return cachedGpu;
    }

    /** Must be called on the render thread (GL context required). */
    public static void captureGpuOnRenderThread() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        try {
            cachedGpu = ClientAccess.gpu();
        } catch (Throwable ignored) {}
    }

    /** Returns the resource location of the client player's current dimension, or null if not in a world. */
    public static String getCurrentDimensionOrNull() {
        if (FMLEnvironment.dist != Dist.CLIENT) return null;
        try {
            return ClientAccess.dimension();
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Nested holder class. The JVM loads this lazily — it is never referenced on
     * a dedicated server because the caller guards with {@link FMLEnvironment#dist}.
     */
    private static final class ClientAccess {
        static String username() {
            return net.minecraft.client.Minecraft.getInstance().getUser().getName();
        }

        static String uuid() {
            return net.minecraft.client.Minecraft.getInstance().getUser().getUuid();
        }

        static String dimension() {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null) return null;
            return mc.level.dimension().location().toString();
        }

        static Telemetry.GpuInfo gpu() {
            Telemetry.GpuInfo g = new Telemetry.GpuInfo();
            try {
                g.renderer = org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER);
                g.vendor   = org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_VENDOR);
                g.version  = org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_VERSION);
            } catch (Throwable ignored) {}
            try {
                var window = net.minecraft.client.Minecraft.getInstance().getWindow();
                g.screen_width = window.getScreenWidth();
                g.screen_height = window.getScreenHeight();
            } catch (Throwable ignored) {}
            return g;
        }
    }
}
