package com.soul.soa_additions.mixin.itemstages;

import com.soul.soa_additions.itemstages.UnknownItemModels;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Swaps the model of a staged item for a question mark, the way HardcoreItemStages
 * did in GreedyCraft (see {@link UnknownItemModels} for the parity notes).
 *
 * <p>{@code getModel} is the single funnel every item render path resolves through
 * — inventory and JEI slots, dropped entities, item frames, held items — so one
 * hook covers them all without touching baking or wrapping other mods' models.</p>
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererUnknownMixin {

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void soa$unknownItemModel(ItemStack stack, Level level, LivingEntity entity, int seed,
                                      CallbackInfoReturnable<BakedModel> cir) {
        final BakedModel hidden = UnknownItemModels.hiddenModel(stack);
        if (hidden != null) {
            cir.setReturnValue(hidden);
        }
    }
}
