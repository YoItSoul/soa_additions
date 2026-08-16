package com.soul.soa_additions.optimizer.client;

import com.mojang.blaze3d.platform.GlUtil;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.optimizer.HardwareProfile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feeds the GPU half of {@link HardwareProfile} on the client.
 *
 * <p>Lives in a client-only class so {@code HardwareProfile} itself never references OpenGL and
 * stays loadable on a dedicated server.</p>
 *
 * <p>Uses the live {@code GL_RENDERER} string rather than enumerating display adapters. Adapter
 * enumeration is actively misleading on real machines — the development box for this pack lists
 * a Parsec virtual display, a Meta virtual monitor and a USB display device <em>before</em> its
 * actual Radeon, so anything picking "the first adapter" would conclude the player had no GPU.
 * {@code GL_RENDERER} names the device actually drawing the frames.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientHardwareProbe {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_Hardware");

    private ClientHardwareProbe() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                String renderer = GlUtil.getRenderer();
                String vendor = GlUtil.getVendor();
                HardwareProfile.setClientGpu(renderer, vendor);
                LOGGER.debug("GPU detected: {} / {}", vendor, renderer);
            } catch (Throwable t) {
                // Headless, an odd driver, or a future mapping change — advice degrades to
                // RAM and cores, which is still useful.
                LOGGER.debug("GPU detection unavailable: {}", t.toString());
            }
        });
    }
}
