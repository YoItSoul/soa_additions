package com.soul.soa_additions.client;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Puts {@link SoaConsentScreen} in front of the title screen on a launch where either choice is
 * unanswered.
 *
 * <p>Driven by the client tick rather than {@code ScreenEvent.Opening}, deliberately. The pack ships
 * FancyMenu with a title-screen layout, and a mod that answers the opening event is competing with
 * FancyMenu over what the title screen is allowed to be — lose that race once and the dialog never
 * appears, which is what happened to the old telemetry prompt. Asking "is the title screen on screen
 * right now?" every tick has no ordering to lose: whatever finally settles as the title screen, this
 * sees it.</p>
 *
 * <p>Nothing latches the check off, also deliberately. A latch would have to be set from the state
 * "no choice is pending", and early in startup that state is indistinguishable from "the config has
 * not loaded yet, so we cannot tell what is pending" — latching there would silently retire the
 * prompt for the whole session. The check only runs while the title screen is up, so leaving it live
 * costs an instanceof per tick on the main menu and nothing at all in a world.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ConsentPrompt {

    private ConsentPrompt() {}

    /**
     * Ticks the title screen must be up before we open on top of it. FancyMenu builds its layout
     * over the first few frames, and taking the screen mid-build has it re-assert its own.
     */
    private static final int SETTLE_TICKS = 5;
    private static int settled;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof TitleScreen) || !SoaConsentScreen.shouldShow()) {
            settled = 0;
            return;
        }
        if (++settled < SETTLE_TICKS) return;

        settled = 0;
        mc.setScreen(new SoaConsentScreen(mc.screen, true));
    }
}
