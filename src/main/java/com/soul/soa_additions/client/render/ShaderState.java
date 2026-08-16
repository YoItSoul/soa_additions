package com.soul.soa_additions.client.render;

import java.lang.reflect.Method;

import net.minecraftforge.fml.ModList;

/**
 * Is a shaderpack currently rendering the world?
 *
 * <p>Answered through Oculus's {@code IrisApi}, reflectively, so soa_additions keeps
 * no compile or load dependency on Oculus. The answer can change at any time — the
 * player can enable or disable a pack from the shader screen without a restart — so
 * callers must ask per draw rather than caching a startup value.</p>
 */
public final class ShaderState {

    private static Object irisApi;
    private static Method isShaderPackInUse;
    private static boolean resolved;

    private ShaderState() {
    }

    /** {@code true} only when Oculus is present and a pack is actually in use. */
    public static boolean shadersActive() {
        if (!resolved) {
            resolve();
        }
        if (isShaderPackInUse == null) {
            return false;
        }
        try {
            return (Boolean) isShaderPackInUse.invoke(irisApi);
        } catch (ReflectiveOperationException | ClassCastException e) {
            isShaderPackInUse = null;
            return false;
        }
    }

    private static void resolve() {
        resolved = true;
        ModList mods = ModList.get();
        if (mods == null || (!mods.isLoaded("oculus") && !mods.isLoaded("iris"))) {
            return;
        }
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            irisApi = api.getMethod("getInstance").invoke(null);
            isShaderPackInUse = api.getMethod("isShaderPackInUse");
        } catch (ReflectiveOperationException | LinkageError e) {
            irisApi = null;
            isShaderPackInUse = null;
        }
    }
}
