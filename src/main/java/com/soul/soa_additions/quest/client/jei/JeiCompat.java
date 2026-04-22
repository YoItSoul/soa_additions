package com.soul.soa_additions.quest.client.jei;

import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.item.ItemStack;

/**
 * Static façade for JEI runtime access. The runtime is captured by
 * {@link SoaJeiPlugin#onRuntimeAvailable(IJeiRuntime)} when JEI starts; if
 * JEI isn't installed, the plugin is never instantiated and the runtime
 * stays null. {@link #showItem(ItemStack)} no-ops in that case so callers
 * never need a presence check.
 */
public final class JeiCompat {

    private static IJeiRuntime runtime;

    private JeiCompat() {}

    static void setRuntime(IJeiRuntime r) { runtime = r; }

    public static boolean available() { return runtime != null; }

    /** Open the JEI recipes view for the given item stack (OUTPUT focus:
     *  shows how to craft this stack). */
    public static void showItem(ItemStack stack) {
        if (runtime == null || stack == null || stack.isEmpty()) return;
        IFocusFactory ff = runtime.getJeiHelpers().getFocusFactory();
        runtime.getRecipesGui().show(ff.createFocus(
                RecipeIngredientRole.OUTPUT,
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                stack));
    }

    /** Open the JEI recipes view for the given item stack (INPUT focus:
     *  shows recipes that consume this stack — useful for "what can I
     *  do with this"). */
    public static void showItemUses(ItemStack stack) {
        if (runtime == null || stack == null || stack.isEmpty()) return;
        IFocusFactory ff = runtime.getJeiHelpers().getFocusFactory();
        runtime.getRecipesGui().show(ff.createFocus(
                RecipeIngredientRole.INPUT,
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                stack));
    }
}
