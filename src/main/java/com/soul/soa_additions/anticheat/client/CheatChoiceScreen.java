package com.soul.soa_additions.anticheat.client;

import com.soul.soa_additions.anticheat.CheatCopy;
import com.soul.soa_additions.network.CheatDetectedPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleplayer answer to a detected cheat: your world, your call.
 *
 * <p>Two buttons and no third option, because the whole point is that a player who did not know
 * they had an xray pack should not have to guess what to do. Quitting to fix it leaves no record;
 * enabling cheating writes the opt-in and flags the save. There is deliberately no "ignore" — the
 * screen would otherwise become something you dismiss without reading.</p>
 *
 * <p>Both consequences are spelled out on the screen rather than hidden behind a "what does that
 * mean?" button. Someone deciding whether to flag their own save is exactly the person who needs
 * the detail, and they are the least likely to go looking for it.</p>
 */
public final class CheatChoiceScreen extends Screen {

    private static final int WIDTH = 400;
    private static final int LINE_H = 11;
    private static final int TITLE_H = 18;
    private static final int BUTTON_W = 220;
    private static final int BUTTON_H = 20;
    private static final int BUTTON_GAP = 4;

    private final CheatDetectedPacket detection;
    private final List<FormattedCharSequence> body = new ArrayList<>();
    private int contentTop;
    /** Second stage: the player has asked to continue and must confirm it. */
    private boolean confirming;

    public CheatChoiceScreen(CheatDetectedPacket detection) {
        super(Component.literal("Cheat Detected"));
        this.detection = detection;
    }

    @Override
    protected void init() {
        body.clear();
        if (confirming) {
            buildConfirm();
        } else {
            buildChoice();
        }

        int bodyH = body.size() * LINE_H;
        int buttonsH = BUTTON_H * 2 + BUTTON_GAP;
        int total = TITLE_H + bodyH + 12 + buttonsH;
        contentTop = Math.max(4, (this.height - total) / 2);

        int x = (this.width - BUTTON_W) / 2;
        // Anchored under the text so a long detection line can't sit on top of the buttons, and
        // clamped so the buttons stay on screen even if it does. Verified to clear the text at a
        // 240px GUI height, which is the shortest anyone realistically plays at.
        int y = Math.min(contentTop + TITLE_H + bodyH + 12, this.height - buttonsH - 4);

        int y2 = y + BUTTON_H + BUTTON_GAP;

        if (confirming) {
            // The destructive button deliberately takes the TOP slot here, because the button that
            // got us to this screen was in the bottom one. A double-click that carries through the
            // first press lands on "Go back", not on the irreversible answer.
            this.addRenderableWidget(Button.builder(
                    Component.literal("Yes, mark this world").withStyle(ChatFormatting.RED),
                    b -> enableCheating()).bounds(x, y, BUTTON_W, BUTTON_H).build());

            this.addRenderableWidget(Button.builder(
                    Component.literal("Go back").withStyle(ChatFormatting.GREEN),
                    b -> {
                        confirming = false;
                        rebuildWidgets();
                    }).bounds(x, y2, BUTTON_W, BUTTON_H).build());
            return;
        }

        // Labels mirror the two paragraphs above them, so the button a player clicks says the
        // same thing as the warning they just read.
        this.addRenderableWidget(Button.builder(
                Component.literal("Quit and delete the file").withStyle(ChatFormatting.GREEN),
                b -> quitToTitle()).bounds(x, y, BUTTON_W, BUTTON_H).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Continue as a cheater world").withStyle(ChatFormatting.RED),
                b -> {
                    confirming = true;
                    rebuildWidgets();
                }).bounds(x, y2, BUTTON_W, BUTTON_H).build());
    }

    private void buildChoice() {
        line("Souls of Avarice found a file that lets you cheat:", ChatFormatting.WHITE);
        line(detection.detail(), ChatFormatting.YELLOW);
        blank();

        line("Delete the file and nothing is recorded against you.", ChatFormatting.GREEN);
        line(CheatCopy.NOTHING_RECORDED, ChatFormatting.GRAY);
        line(CheatCopy.DELETE_NOT_DISABLE, ChatFormatting.DARK_GRAY);
        blank();

        line("Keep it and this world is marked as a cheater world.", ChatFormatting.RED);
        line(CheatCopy.CHEATER_WORLD, ChatFormatting.GRAY);
    }

    private void buildConfirm() {
        line("Mark this world as a cheater world?", ChatFormatting.WHITE);
        blank();
        line("This cannot be undone.", ChatFormatting.RED);
        line(CheatCopy.CHEATER_WORLD, ChatFormatting.GRAY);
    }

    /** Wraps to {@link #WIDTH} so the copy can be written as sentences rather than hand-cut lines. */
    private void line(String text, ChatFormatting style) {
        body.addAll(this.font.split(Component.literal(text).withStyle(style), WIDTH));
    }

    private void blank() {
        body.add(FormattedCharSequence.EMPTY);
    }

    private void quitToTitle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) mc.level.disconnect();
        mc.clearLevel();
        mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
    }

    /**
     * Marks the world immediately — this screen is the confirmation, so there is no second one.
     *
     * <p>Runs the opt-in command rather than sending a packet of its own: the command is the single
     * source of truth for the flag, and duplicating that state change would give it two ways to be
     * set and one of them to drift.
     */
    private void enableCheating() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.connection.sendCommand("soa quests cheatermode true");
        }
        this.onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    /** No Esc — a screen you can dismiss without choosing is a screen nobody reads. */
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g);
        int cx = this.width / 2;
        int y = contentTop;

        g.drawCenteredString(this.font, Component.literal("Cheat Detected")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), cx, y, 0xFFFFFF);
        y += TITLE_H;
        for (FormattedCharSequence l : body) {
            g.drawCenteredString(this.font, l, cx, y, 0xFFFFFF);
            y += LINE_H;
        }
        super.render(g, mouseX, mouseY, partial);
    }
}
