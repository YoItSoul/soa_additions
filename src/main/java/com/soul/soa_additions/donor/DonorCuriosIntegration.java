package com.soul.soa_additions.donor;

import com.soul.soa_additions.SoaAdditions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optional Curios API integration. Registers a "donor" curio slot type
 * when Curios is present. The slot is cosmetic-only and intended for
 * donor-exclusive trinket items.
 *
 * <p>All Curios API calls are isolated behind {@link ModList#isLoaded}
 * checks so the mod runs fine without Curios installed.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DonorCuriosIntegration {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/donor-curios");

    private DonorCuriosIntegration() {}

    @SubscribeEvent
    public static void onInterModEnqueue(InterModEnqueueEvent event) {
        if (!ModList.get().isLoaded("curios")) {
            LOG.debug("Curios not present — skipping donor slot registration");
            return;
        }

        try {
            registerSlot();
            LOG.info("Registered 'donor' curio slot");
        } catch (Throwable t) {
            LOG.warn("Failed to register curios donor slot", t);
        }
    }

    @SuppressWarnings({"removal", "deprecation"})
    private static void registerSlot() {
        InterModComms.sendTo("curios", top.theillusivec4.curios.api.SlotTypeMessage.REGISTER_TYPE,
                () -> new top.theillusivec4.curios.api.SlotTypeMessage.Builder("donor")
                        .size(1)
                        .cosmetic()
                        .build());
    }
}
