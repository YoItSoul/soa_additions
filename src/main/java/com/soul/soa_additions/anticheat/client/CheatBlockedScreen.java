package com.soul.soa_additions.anticheat.client;

import com.soul.soa_additions.anticheat.CheatCopy;
import com.soul.soa_additions.network.CheatDetectedPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Shown in place of the vanilla disconnect screen when the server refused the connection over a
 * detected cheat.
 *
 * <p>The disconnect itself already happened server-side; this only explains it better than a wall
 * of red text would. The important line is that nothing was recorded — a player who removes the
 * pack and reconnects is in exactly the state they were before, which is what makes "turn around"
 * a real option rather than a euphemism. It says what "nothing" covers, because a bare
 * reassurance from the thing that just kicked you is not worth much.</p>
 */
public final class CheatBlockedScreen extends Screen {

    private static final int WIDTH = 400;
    private static final int LINE_H = 11;

    private final CheatDetectedPacket detection;
    private final Screen background;
    private final List<FormattedCharSequence> body = new ArrayList<>();

    public CheatBlockedScreen(CheatDetectedPacket detection, Screen background) {
        super(Component.literal("Cheat Detected"));
        this.detection = detection;
        this.background = background;
    }

    @Override
    protected void init() {
        body.clear();
        line("The server found a file that lets you cheat:", ChatFormatting.WHITE);
        line(detection.detail(), ChatFormatting.YELLOW);
        blank();

        line("Nothing is recorded against you.", ChatFormatting.GREEN);
        line(CheatCopy.NOTHING_RECORDED, ChatFormatting.GRAY);
        line("Delete the file and you can join straight back in.", ChatFormatting.GRAY);
        blank();

        line(CheatCopy.FILE_LOCATION, ChatFormatting.DARK_GRAY);
        line(CheatCopy.DELETE_NOT_DISABLE, ChatFormatting.DARK_GRAY);

        int btnW = 220;
        int x = (this.width - btnW) / 2;
        int y = this.height - 60;
        this.addRenderableWidget(Button.builder(Component.literal("Back to server list"),
                        b -> Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen())))
                .bounds(x, y, btnW, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Main menu"),
                        b -> Minecraft.getInstance().setScreen(new TitleScreen()))
                .bounds(x, y + 24, btnW, 20).build());
    }

    /** Wraps to {@link #WIDTH} so the copy can be written as sentences rather than hand-cut lines. */
    private void line(String text, ChatFormatting style) {
        body.addAll(this.font.split(Component.literal(text).withStyle(style), WIDTH));
    }

    private void blank() {
        body.add(FormattedCharSequence.EMPTY);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g);
        int cx = this.width / 2;
        int y = 40;
        g.drawCenteredString(this.font, Component.literal("Cheat Detected")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), cx, y, 0xFFFFFF);
        y += 20;
        for (FormattedCharSequence line : body) {
            g.drawCenteredString(this.font, line, cx, y, 0xFFFFFF);
            y += LINE_H;
        }
        super.render(g, mouseX, mouseY, partial);
    }
}
