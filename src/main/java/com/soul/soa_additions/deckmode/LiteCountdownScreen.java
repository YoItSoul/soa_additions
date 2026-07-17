package com.soul.soa_additions.deckmode;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Full-screen countdown shown after Lite Mode is toggled. The renames are
 * applied by a JVM shutdown hook (plus a small logged helper for jars Windows
 * keeps locked), so closing the game is what applies the change — this screen
 * is deliberately not dismissible.
 */
public final class LiteCountdownScreen extends Screen {

    private static final int COUNTDOWN_TICKS = 5 * 20;

    private final boolean enabled;
    private final int jarCount;
    private int ticksLeft = COUNTDOWN_TICKS;

    public LiteCountdownScreen(boolean enabled, int jarCount) {
        super(Component.literal("Lite Mode"));
        this.enabled = enabled;
        this.jarCount = jarCount;
    }

    @Override
    public void tick() {
        if (--ticksLeft <= 0) {
            Minecraft.getInstance().stop();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int cx = width / 2;
        int cy = height / 2;
        int seconds = (ticksLeft + 19) / 20;

        graphics.drawCenteredString(font,
                Component.literal(enabled ? "Lite Mode enabled" : "Lite Mode disabled")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                cx, cy - 30, 0xFFFFFF);
        graphics.drawCenteredString(font,
                Component.literal(enabled
                        ? jarCount + " cosmetic mods disabled, dynamic resources forced on"
                        : jarCount + " mods will be restored"),
                cx, cy - 14, 0xC0C0C0);
        graphics.drawCenteredString(font,
                Component.literal("The game will close in " + seconds + "…")
                        .withStyle(ChatFormatting.YELLOW),
                cx, cy + 6, 0xFFFFFF);
        graphics.drawCenteredString(font,
                Component.literal("Re-launch the pack from your launcher to apply the change.")
                        .withStyle(ChatFormatting.GRAY),
                cx, cy + 22, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
