package com.soul.soa_additions.oresight.client;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Tints the {@code layer0} (overlay) of the ore-sight potion item models
 * with the colour of the bottled Potion's effects, matching the way vanilla
 * {@code Items.POTION/SPLASH/LINGERING} pick up their bottle colour. Layer 1
 * (the glass bottle) is left untinted (-1).
 *
 * <p>Without this handler the bottles render as plain pink/white because no
 * mod-side {@code ItemColor} is registered for our custom items.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class OreSightItemColors {
    private OreSightItemColors() {}

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer > 0 ? -1 : PotionUtils.getColor(stack),
                ModItems.ORE_SIGHT_POTION.get(),
                ModItems.ORE_SIGHT_SPLASH_POTION.get(),
                ModItems.ORE_SIGHT_LINGERING_POTION.get());
    }
}
