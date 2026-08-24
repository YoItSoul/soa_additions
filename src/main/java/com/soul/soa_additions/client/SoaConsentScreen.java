package com.soul.soa_additions.client;

import com.soul.soa_additions.anticheat.AntiCheatConsent;
import com.soul.soa_additions.config.ModConfigs;
import com.soul.soa_additions.telemetry.Telemetry;
import com.soul.soa_additions.telemetry.TelemetryConsent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The single first-launch consent dialog, shown before the title screen when either decision is
 * still unanswered. Two independent checkboxes, both off until the player ticks them: nothing is
 * collected or sent by either system while its box is clear.
 *
 * <p>One screen rather than two because the player meets both questions at the same moment and a
 * second forced dialog immediately after the first reads as a nag. They stay separate
 * <em>decisions</em> — separate checkboxes, separate state files, separate recipients — and
 * confirming records exactly what is ticked, never one implying the other.</p>
 *
 * <p>Also reachable afterwards from the title-screen "C" button ({@link ConsentTitleButton}) and
 * {@code /soa anticheat review}, in which case the boxes start on the current answers and ESC is
 * allowed.</p>
 */
public final class SoaConsentScreen extends Screen {

    private static final Component TITLE =
            Component.literal("Souls of Avarice — your choices")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

    private static final Component INTRO = Component.literal(
            "Two features want your permission before they do anything. Both are off unless you "
            + "tick them, and you can change either one at any time from the \"C\" button in the "
            + "top-left corner of the main menu.");

    private static final Component TELEMETRY_LABEL = Component.literal("Send performance reports to the pack author");
    private static final Component TELEMETRY_DESC = Component.literal(
            "Used to troubleshoot and optimize the pack for real hardware. Sends your Minecraft "
            + "username and UUID, OS / CPU / RAM / GPU, Java version and memory settings, mod load "
            + "times, in-world playtime and quest progress. No file paths, no world contents — ever. "
            + "Stays off unless you tick it, and you can switch it back off whenever you like.");

    private static final Component ANTICHEAT_LABEL = Component.literal("Let servers check my mods for cheats");
    private static final Component ANTICHEAT_DESC = Component.literal(
            "When you join a world, sends the id and name of every loaded mod and every resource pack "
            + "in your resourcepacks folder — including disabled ones — plus whether any pack contains "
            + "xray-style models, to THAT SERVER and nowhere else. Never to the pack author. Most "
            + "Souls of Avarice servers require this to let you in.");

    private static final Component FOOTER = Component.literal(
            "Neither choice affects singleplayer progression, and declining is never held against you.");

    private final Screen parent;
    /** First-launch prompt: answer it. Opened from a button or command: ESC is fine. */
    private final boolean forced;

    private Checkbox telemetryBox;
    private Checkbox anticheatBox;

    // Resolved in init(), because every y depends on how the paragraphs wrap at this window size.
    private int wrapWidth;
    private int textX;
    private int introY;
    private int telemetryDescY;
    private int anticheatDescY;
    private int footerY;
    private Button confirmButton;
    private int telemetryBoxY;
    private int anticheatBoxY;
    private int confirmY;
    /** Content taller than the window scrolls rather than hiding text under the button. */
    private int scroll;
    private int maxScroll;

    public SoaConsentScreen(Screen parent, boolean forced) {
        super(Component.literal("Souls of Avarice consent"));
        this.parent = parent;
        this.forced = forced;
    }

    /** True when the dialog still needs to be shown this launch — i.e. either answer is missing. */
    public static boolean shouldShow() {
        return telemetryOffered() && TelemetryConsent.get() == TelemetryConsent.State.UNDECIDED
                || anticheatOffered() && AntiCheatConsent.get() == AntiCheatConsent.State.UNDECIDED;
    }

    /** False when telemetry cannot run at all this launch, in which case asking would be dishonest. */
    public static boolean telemetryOffered() {
        try {
            if (!ModConfigs.ENABLE_TELEMETRY.get()) return false;
            String endpoint = ModConfigs.TELEMETRY_ENDPOINT.get();
            return endpoint != null && !endpoint.isBlank();
        } catch (Throwable t) {
            return false;   // config not loaded yet
        }
    }

    public static boolean anticheatOffered() {
        try {
            return ModConfigs.ENABLE_ANTICHEAT_SCAN.get();
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    protected void init() {
        wrapWidth = Math.min(360, width - 40);
        textX = (width - wrapWidth) / 2;

        // Lay the screen out top-down so nothing can land on top of anything else, whatever the
        // window size or GUI scale. The previous consent dialog placed its text at fixed offsets
        // and its buttons at height-40, which collided the moment the paragraphs wrapped further
        // than expected — that is why it never looked right.
        int y = 20 + font.lineHeight + 8;

        introY = y;
        y += font.wordWrapHeight(INTRO, wrapWidth) + 12;

        telemetryBoxY = y;
        telemetryBox = new Checkbox(textX, y, wrapWidth, 20, TELEMETRY_LABEL,
                TelemetryConsent.get() == TelemetryConsent.State.ACCEPTED);
        addRenderableWidget(telemetryBox);
        y += 22;
        telemetryDescY = y;
        y += font.wordWrapHeight(TELEMETRY_DESC, wrapWidth - 16) + 12;

        anticheatBoxY = y;
        anticheatBox = new Checkbox(textX, y, wrapWidth, 20, ANTICHEAT_LABEL,
                AntiCheatConsent.get() == AntiCheatConsent.State.ACCEPTED);
        addRenderableWidget(anticheatBox);
        y += 22;
        anticheatDescY = y;
        y += font.wordWrapHeight(ANTICHEAT_DESC, wrapWidth - 16) + 12;

        footerY = y;
        y += font.wordWrapHeight(FOOTER, wrapWidth) + 12;

        // Anchor the button to the bottom when there is room to spare, but never above the
        // text. Capping it at height - 24 used to defeat exactly that: on a small window the
        // button was pulled back on top of the disclosure it is asking the player to accept, and
        // the paragraphs below the fold had no way to be read at all. Overflow scrolls instead.
        confirmY = Math.max(y, height - 32);
        maxScroll = Math.max(0, confirmY + 28 - height);
        scroll = Math.min(scroll, maxScroll);
        confirmButton = Button.builder(Component.literal("Confirm"), b -> confirm())
                .bounds(width / 2 - 75, confirmY - scroll, 150, 20)
                .build();
        addRenderableWidget(confirmButton);

        // A system that cannot run this launch is not a choice worth offering.
        if (!telemetryOffered()) telemetryBox.visible = telemetryBox.active = false;
        if (!anticheatOffered()) anticheatBox.visible = anticheatBox.active = false;
    }

    private void confirm() {
        if (telemetryOffered()) {
            boolean on = telemetryBox.selected();
            TelemetryConsent.set(on ? TelemetryConsent.State.ACCEPTED : TelemetryConsent.State.DECLINED);
            if (on) Telemetry.onConsentGranted();
        }
        if (anticheatOffered()) {
            AntiCheatConsent.set(anticheatBox.selected()
                    ? AntiCheatConsent.State.ACCEPTED : AntiCheatConsent.State.DECLINED);
        }
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        // Widgets carry their own coordinates, so the scroll offset has to be pushed into them
        // before super.render draws — and before any click is tested against their bounds.
        if (telemetryBox != null) telemetryBox.setY(telemetryBoxY - scroll);
        if (anticheatBox != null) anticheatBox.setY(anticheatBoxY - scroll);
        if (confirmButton != null) confirmButton.setY(confirmY - scroll);

        graphics.drawCenteredString(font, TITLE, width / 2, 20 - scroll, 0xFFFFFF);
        graphics.drawWordWrap(font, INTRO, textX, introY - scroll, wrapWidth, 0xE0E0E0);
        if (telemetryOffered()) {
            graphics.drawWordWrap(font, TELEMETRY_DESC, textX + 16, telemetryDescY - scroll, wrapWidth - 16, 0xA0A0A0);
        }
        if (anticheatOffered()) {
            graphics.drawWordWrap(font, ANTICHEAT_DESC, textX + 16, anticheatDescY - scroll, wrapWidth - 16, 0xA0A0A0);
        }
        graphics.drawWordWrap(font, FOOTER, textX, footerY - scroll, wrapWidth, 0x808080);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (maxScroll > 0) {
            String hint = scroll < maxScroll ? "\u25BC scroll for the rest" : "\u25B2 scroll back up";
            graphics.drawCenteredString(font, hint, width / 2, height - 10, 0x808080);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll <= 0) return super.mouseScrolled(mouseX, mouseY, delta);
        scroll = net.minecraft.util.Mth.clamp(scroll - (int) (delta * 12), 0, maxScroll);
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !forced;
    }
}
