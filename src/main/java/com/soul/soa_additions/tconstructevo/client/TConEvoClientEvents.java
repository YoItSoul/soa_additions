package com.soul.soa_additions.tconstructevo.client;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.nyx.client.NoopEntityRenderer;
import com.soul.soa_additions.tconstructevo.TConEvoEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TConEvoClientEvents {

    private TConEvoClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TConEvoEntities.MAGIC_MISSILE.get(), NoopEntityRenderer::new);
    }
}
