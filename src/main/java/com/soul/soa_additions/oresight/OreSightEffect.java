package com.soul.soa_additions.oresight;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.function.Consumer;

/**
 * Single MobEffect that signals "ore-sight is active". The actual ores being
 * tracked live on a per-player capability ({@link OreSightTracker}) so the
 * player can stack multiple ore-sights without colliding on the one-effect-
 * per-type vanilla constraint. The MobEffect's only role is the inventory
 * HUD icon.
 *
 * <p>The icon itself is rendered as the tracked block (via
 * {@link com.soul.soa_additions.oresight.client.OreSightIconRenderer}) — when
 * multiple ore-sights are stacked, the icon cycles through the tracked blocks
 * every ~3 seconds so the player can see what's being highlighted.</p>
 */
public final class OreSightEffect extends MobEffect {
    public OreSightEffect() {
        // Color picked to match the soft cyan used by vanilla detector tooltips.
        this(0x4FBFD7);
    }

    public OreSightEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        // Lazy-load the renderer class so server jars don't try to resolve it.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            consumer.accept(new com.soul.soa_additions.oresight.client.OreSightIconRenderer());
        }
    }
}
