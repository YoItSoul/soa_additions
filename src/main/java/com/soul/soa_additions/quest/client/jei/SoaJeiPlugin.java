package com.soul.soa_additions.quest.client.jei;

import com.soul.soa_additions.SoaAdditions;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

/**
 * JEI integration entry point. JEI scans for {@link JeiPlugin} annotated
 * classes; we only need it to capture the runtime so {@link JeiCompat} can
 * open recipe views from the quest book.
 */
@JeiPlugin
public final class SoaJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(SoaAdditions.MODID, "jei_plugin");

    @Override public ResourceLocation getPluginUid() { return UID; }

    @Override public void onRuntimeAvailable(IJeiRuntime runtime) {
        JeiCompat.setRuntime(runtime);
    }

    @Override public void onRuntimeUnavailable() {
        JeiCompat.setRuntime(null);
    }
}
