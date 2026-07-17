package com.soul.soa_additions.telemetry;

import com.soul.soa_additions.config.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * First-launch consent dialog shown before the title screen. Telemetry sends
 * absolutely nothing until the player explicitly opts in here — declining (or
 * never answering) keeps it silent. The choice persists via
 * {@link TelemetryConsent} and can be revisited by deleting the consent file.
 */
public final class TelemetryConsentScreen extends Screen {

    private static final Component TITLE =
            Component.literal("Help optimize Souls of Avarice?")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

    private static final Component[] PARAGRAPHS = {
            Component.literal(
                    "While the pack is in development, it can send performance reports to the pack "
                    + "author. These are used ONLY to troubleshoot and optimize the pack for each "
                    + "player's hardware, and telemetry will be disabled for the stable public release."),
            Component.literal(
                    "If you opt in, each launch sends: your Minecraft username and UUID, "
                    + "OS / CPU / RAM / GPU specs, Java version and memory settings, mod load times, "
                    + "in-world playtime, quest progress (with world name and seed), and periodic "
                    + "in-game performance heartbeats. No file paths, no environment variables, "
                    + "no world contents — ever.").withStyle(ChatFormatting.GRAY),
            Component.literal(
                    "Nothing is sent unless you choose \"Opt in\". Change your mind anytime in "
                    + "config/soa_additions/soa_additions.toml under [telemetry], or delete "
                    + "config/soa_additions/telemetry_consent.txt to see this screen again.")
                    .withStyle(ChatFormatting.DARK_GRAY),
    };

    private final Screen parent;

    public TelemetryConsentScreen(Screen parent) {
        super(Component.literal("Telemetry Consent"));
        this.parent = parent;
    }

    /** True when the dialog still needs to be shown this launch. */
    public static boolean shouldShow() {
        if (!FMLEnvironment.production) return false;
        try {
            if (!ModConfigs.ENABLE_TELEMETRY.get()) return false;
            String endpoint = ModConfigs.TELEMETRY_ENDPOINT.get();
            if (endpoint == null || endpoint.isBlank()) return false;
        } catch (Throwable t) {
            return false;
        }
        return TelemetryConsent.get() == TelemetryConsent.State.UNDECIDED;
    }

    @Override
    protected void init() {
        int buttonY = height - 40;
        addRenderableWidget(Button.builder(
                        Component.literal("Opt in — help development"),
                        b -> choose(TelemetryConsent.State.ACCEPTED))
                .bounds(width / 2 - 155, buttonY, 150, 20)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal("No thanks"),
                        b -> choose(TelemetryConsent.State.DECLINED))
                .bounds(width / 2 + 5, buttonY, 150, 20)
                .build());
    }

    private void choose(TelemetryConsent.State state) {
        TelemetryConsent.set(state);
        if (state == TelemetryConsent.State.ACCEPTED) {
            Telemetry.onConsentGranted();
        }
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int wrapWidth = Math.min(360, width - 60);
        int x = (width - wrapWidth) / 2;

        graphics.drawCenteredString(font, TITLE, width / 2, 24, 0xFFFFFF);

        int y = 48;
        for (Component paragraph : PARAGRAPHS) {
            graphics.drawWordWrap(font, paragraph, x, y, wrapWidth, 0xE0E0E0);
            y += font.wordWrapHeight(paragraph, wrapWidth) + 10;
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    // Force an explicit, informed choice — no dismissing past the dialog.
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
