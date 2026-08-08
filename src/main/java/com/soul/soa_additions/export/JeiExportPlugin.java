package com.soul.soa_additions.export;

import com.soul.soa_additions.SoaAdditions;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

/**
 * Captures the JEI runtime so {@link JeiRecipeExport} can enumerate every
 * recipe JEI knows about.
 *
 * <p>This is the universal recipe reader. Reading the vanilla
 * {@code RecipeManager} only gets you what the {@code Recipe} interface chooses
 * to expose, and roughly a quarter of this pack's recipes are custom types
 * whose classes return nothing useful from {@code getResultItem()} —
 * CustomMachinery, Create mixing, BotanyPots, Blood Magic altar work. JEI can
 * display all of them because every mod ships a JEI plugin that teaches JEI how
 * to read its own recipe types. Going through JEI inherits all of that work
 * instead of re-deriving it per mod.</p>
 *
 * <p>Client-side only: the JEI runtime does not exist on a dedicated server.</p>
 */
@JeiPlugin
public class JeiExportPlugin implements IModPlugin {

    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(SoaAdditions.MODID, "jei_export");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    public static IJeiRuntime runtime() {
        return runtime;
    }
}
