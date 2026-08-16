package com.soul.soa_additions.client;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.anticheat.AntiCheatConsent;
import com.soul.soa_additions.telemetry.TelemetryConsent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * A single 20x20 "C" button in the top-left of the title screen that opens {@link SoaConsentScreen},
 * so either choice can be changed without being in a world or knowing a command.
 *
 * <p>The letter is coloured by the state behind it: green when everything on offer is opted in, red
 * when nothing is, yellow when it is one of each. The tooltip spells out which is which, since a
 * single colour cannot.</p>
 *
 * <p>One button rather than a toggle per system because the screen it opens is where the decisions
 * actually live — a title-screen toggle that flipped consent on directly would be granting
 * permission without the disclosure ever being shown, which is the thing the screen exists to
 * prevent. Turning something off is equally available there, one click further in.</p>
 *
 * <p>Top-left because the Deck/Lite performance toggles own the top-right corner
 * ({@code DeckModeClientEvents}). Registered through the vanilla screen-init event, which FancyMenu
 * leaves alone — it customizes the vanilla TitleScreen instance rather than replacing it.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ConsentTitleButton {

    private static final int SIZE = 20;

    private ConsentTitleButton() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen)) return;
        // Nothing to consent to: both systems switched off in config. A button that opens an empty
        // screen is worse than no button.
        if (!SoaConsentScreen.telemetryOffered() && !SoaConsentScreen.anticheatOffered()) return;
        event.addListener(new ConsentButton(4, 4));
    }

    private static final class ConsentButton extends Button {

        ConsentButton(int x, int y) {
            super(x, y, SIZE, SIZE, Component.empty(), b -> {}, DEFAULT_NARRATION);
            refresh();
        }

        /** Last state the label and tooltip were built from, so a frame that changed nothing costs nothing. */
        private Boolean lastTelemetry;
        private Boolean lastScan;

        /**
         * Re-reads both decisions. Called on construction, and again each render because the player
         * can change them on the screen this button opens and come straight back to a title screen
         * that was never re-initialised — leaving a stale colour on the one widget whose whole job
         * is reporting that state.
         */
        private void refresh() {
            boolean telemetry = SoaConsentScreen.telemetryOffered() && TelemetryConsent.isAccepted();
            boolean scan = SoaConsentScreen.anticheatOffered() && AntiCheatConsent.isAccepted();
            if (lastTelemetry != null && lastTelemetry == telemetry && lastScan == scan) return;
            lastTelemetry = telemetry;
            lastScan = scan;

            int on = (telemetry ? 1 : 0) + (scan ? 1 : 0);
            int offered = (SoaConsentScreen.telemetryOffered() ? 1 : 0)
                    + (SoaConsentScreen.anticheatOffered() ? 1 : 0);

            ChatFormatting colour = on == 0 ? ChatFormatting.RED
                    : on == offered ? ChatFormatting.GREEN
                    : ChatFormatting.YELLOW;
            setMessage(Component.literal("C").withStyle(colour, ChatFormatting.BOLD));

            MutableComponent tooltip = Component.literal("Privacy choices").withStyle(ChatFormatting.WHITE);
            if (SoaConsentScreen.telemetryOffered()) tooltip.append(line("Telemetry", telemetry));
            if (SoaConsentScreen.anticheatOffered()) tooltip.append(line("Cheat scan", scan));
            tooltip.append(Component.literal("\n\nClick to change.").withStyle(ChatFormatting.DARK_GRAY));
            setTooltip(Tooltip.create(tooltip));
        }

        private static Component line(String name, boolean on) {
            return Component.literal("\n" + name + ": ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(on ? "ON" : "OFF")
                            .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
        }

        @Override
        public void renderWidget(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY,
                                 float partialTick) {
            refresh();
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onPress() {
            Minecraft mc = Minecraft.getInstance();
            // Deferred: this runs inside the title screen's click handling, and swapping the screen
            // out from under that is how you get a crash on the way back up the call stack.
            mc.execute(() -> mc.setScreen(new SoaConsentScreen(mc.screen, false)));
        }
    }
}
