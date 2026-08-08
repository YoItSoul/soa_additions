package com.soul.soa_additions.anticheat.client;

import com.soul.soa_additions.network.CheatDetectedPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Shown in place of the vanilla disconnect screen when the server refused the connection over a
 * detected cheat.
 *
 * <p>The disconnect itself already happened server-side; this only explains it better than a wall
 * of red text would. The important line is that nothing was recorded — a player who removes the
 * pack and reconnects is in exactly the state they were before, which is what makes "turn around"
 * a real option rather than a euphemism.</p>
 */
public final class CheatBlockedScreen extends Screen {

    private final CheatDetectedPacket detection;
    private final Screen background;
    private final List<Component> body = new ArrayList<>();

    public CheatBlockedScreen(CheatDetectedPacket detection, Screen background) {
        super(Component.literal("Cheat Detected"));
        this.detection = detection;
        this.background = background;
    }

    @Override
    protected void init() {
        body.clear();
        body.add(Component.literal("The server found a " + detection.category()
                + " that lets you cheat:").withStyle(ChatFormatting.WHITE));
        body.add(Component.literal(detection.detail()).withStyle(ChatFormatting.YELLOW));
        body.add(Component.empty());
        body.add(Component.literal("Nothing has been recorded against you.")
                .withStyle(ChatFormatting.GREEN));
        body.add(Component.literal("Remove it and you can join straight back in.")
                .withStyle(ChatFormatting.GRAY));
        body.add(Component.empty());
        body.add(Component.literal("Resource packs are in your instance's resourcepacks folder.")
                .withStyle(ChatFormatting.GRAY));
        body.add(Component.literal("Delete the file — turning it off in the pack selector is not")
                .withStyle(ChatFormatting.DARK_GRAY));
        body.add(Component.literal("enough, because the file is still on disk.")
                .withStyle(ChatFormatting.DARK_GRAY));
        body.add(Component.empty());
        body.add(Component.literal("Want to keep it? Enable cheating in a singleplayer world first.")
                .withStyle(ChatFormatting.DARK_GRAY));
        body.add(Component.literal("This server may still refuse you afterwards.")
                .withStyle(ChatFormatting.DARK_GRAY));

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
        for (Component line : body) {
            g.drawCenteredString(this.font, line, cx, y, 0xFFFFFF);
            y += 11;
        }
        super.render(g, mouseX, mouseY, partial);
    }
}
