package com.soul.soa_additions.tr.client;

import com.soul.soa_additions.tr.ThaumicRemnants;
import com.soul.soa_additions.tr.client.tooltip.AspectTooltipComponent;
import com.soul.soa_additions.tr.client.tooltip.ClientAspectTooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only setup for Thaumic Remnants. Registered once from the main mod
 * bus — owns the binding between server-side {@link AspectTooltipComponent}
 * (data) and client-side {@link ClientAspectTooltipComponent} (renderer),
 * which is the only client-init step the aspect tooltip system needs.
 */
@Mod.EventBusSubscriber(modid = com.soul.soa_additions.SoaAdditions.MODID,
        value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TrClientSetup {

    private TrClientSetup() {}

    public static void register(IEventBus modEventBus) {
        // Forge event subscription is done via the @EventBusSubscriber on this
        // class — but we keep the explicit register() entry point so
        // ThaumicRemnants.init() has a parallel call to TrCreativeTab.register
        // / TrItems.register. No-op body for now.
        ThaumicRemnants.LOG.debug("TrClientSetup wired");
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AspectTooltipComponent.class, ClientAspectTooltipComponent::new);
    }
}
