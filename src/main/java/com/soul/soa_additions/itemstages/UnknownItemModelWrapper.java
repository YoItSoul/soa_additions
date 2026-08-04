package com.soul.soa_additions.itemstages;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.BakedModelWrapper;

import javax.annotation.Nullable;

/**
 * Item model that swaps itself for a question mark while the stack is staged away.
 *
 * <p>{@link net.minecraft.client.renderer.entity.ItemRenderer#getModel} resolves
 * every item render — inventory and JEI slots, dropped entities, item frames,
 * held items — through {@code model.getOverrides().resolve(...)}. Overriding
 * {@link #getOverrides()} therefore intercepts the whole render path without
 * touching baking, which is how HardcoreItemStages hid items in GreedyCraft.</p>
 *
 * <p>The wrapper is deliberately inert once resolution has happened: when the
 * stack is <em>not</em> hidden the delegating overrides hand
 * {@link #originalModel} — not {@code this} — to the wrapped
 * {@link ItemOverrides}, so what the renderer ends up drawing is always either
 * the original model or the question mark, never this wrapper. Mods that
 * {@code instanceof}-check their own {@link BakedModel} implementations during
 * rendering keep seeing their own class.</p>
 */
public final class UnknownItemModelWrapper extends BakedModelWrapper<BakedModel> {

    private final ItemOverrides overrides;

    public UnknownItemModelWrapper(BakedModel originalModel) {
        super(originalModel);
        this.overrides = new UnknownOverrides(originalModel);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    private static final class UnknownOverrides extends ItemOverrides {

        private final BakedModel original;

        private UnknownOverrides(BakedModel original) {
            this.original = original;
        }

        @Nullable
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                  @Nullable LivingEntity entity, int seed) {
            final BakedModel hidden = UnknownItemModels.hiddenModel(stack);
            if (hidden != null) {
                return hidden;
            }
            // Pass the original rather than `model` (which is this wrapper): vanilla's
            // ItemOverrides returns the model it was handed when no override predicate
            // matches, so this unwraps us back out of the render path.
            return this.original.getOverrides().resolve(this.original, stack, level, entity, seed);
        }
    }
}
