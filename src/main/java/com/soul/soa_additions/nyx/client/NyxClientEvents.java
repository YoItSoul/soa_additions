package com.soul.soa_additions.nyx.client;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.nyx.NyxEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side mod-bus events for Nyx. Registers renderers for the three
 * Nyx entity types so Oculus's shadow pass doesn't NPE on null renderers.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NyxClientEvents {

    private NyxClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NyxEntities.FALLING_STAR.get(), NoopEntityRenderer::new);
        event.registerEntityRenderer(NyxEntities.FALLING_METEOR.get(), NoopEntityRenderer::new);
        event.registerEntityRenderer(NyxEntities.CAULDRON_TRACKER.get(), NoopEntityRenderer::new);
    }
}
