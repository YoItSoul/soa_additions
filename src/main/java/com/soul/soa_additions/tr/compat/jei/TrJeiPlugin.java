package com.soul.soa_additions.tr.compat.jei;

import com.soul.soa_additions.tr.ThaumicRemnants;
import com.soul.soa_additions.tr.TrItems;
import com.soul.soa_additions.tr.core.Aspect;
import com.soul.soa_additions.tr.knowledge.ClientKnownAspects;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI integration for Thaumic Remnants. Hides aspect rune items the player
 * hasn't discovered yet — both from JEI's ingredient list and from any recipe
 * that produces or consumes them, since JEI filters recipes whose
 * inputs/outputs are entirely hidden.
 *
 * <p>Re-runs the hide pass whenever {@link ClientKnownAspects} changes, so a
 * player who learns Auram via a Monocle scan or an op /tr discover sees the
 * Auram rune (and recipes for/with it) appear in JEI without restarting.
 */
@JeiPlugin
public final class TrJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(ThaumicRemnants.MODID, "jei_plugin");

    private IIngredientManager manager;
    /** Snapshot of what we currently have hidden. We don't ask JEI ("is X
     *  hidden?") because the API is asymmetric — easier to track ourselves
     *  and diff on each refresh. */
    private final List<ItemStack> currentlyHidden = new ArrayList<>();

    @Override public ResourceLocation getPluginUid() { return UID; }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        this.manager = runtime.getIngredientManager();
        ClientKnownAspects.addListener(this::refresh);
        refresh();
    }

    @Override
    public void onRuntimeUnavailable() {
        this.manager = null;
        currentlyHidden.clear();
    }

    /** Recompute the hidden set from the current discovery state.
     *  Re-shows anything we previously hid that the player now knows;
     *  hides anything they don't. */
    private void refresh() {
        if (manager == null) return;
        List<ItemStack> shouldHide = new ArrayList<>();
        for (var entry : TrItems.ASPECT_RUNES.entrySet()) {
            String path = entry.getKey();
            Aspect aspect = com.soul.soa_additions.tr.core.Aspects.byId(
                    new ResourceLocation(ThaumicRemnants.MODID, path));
            if (!ClientKnownAspects.has(aspect)) {
                shouldHide.add(new ItemStack(entry.getValue().get()));
            }
        }
        // Restore anything we hid before but should now be visible.
        List<ItemStack> toShow = new ArrayList<>();
        for (ItemStack stack : currentlyHidden) {
            boolean stillHidden = shouldHide.stream().anyMatch(s -> ItemStack.isSameItem(s, stack));
            if (!stillHidden) toShow.add(stack);
        }
        try {
            if (!toShow.isEmpty()) {
                manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, toShow);
            }
            // removeIngredientsAtRuntime is idempotent on already-removed entries;
            // calling it on the full shouldHide list is safe.
            if (!shouldHide.isEmpty()) {
                manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, shouldHide);
            }
        } catch (Throwable t) {
            ThaumicRemnants.LOG.warn("Failed to refresh JEI rune visibility: {}", t.toString());
            return;
        }
        currentlyHidden.clear();
        currentlyHidden.addAll(shouldHide);
    }
}
