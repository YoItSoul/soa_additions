package com.soul.soa_additions.anticheat.client;

import com.soul.soa_additions.network.CheatDetectedPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleplayer answer to a detected cheat: your world, your call.
 *
 * <p>Two buttons and no third option, because the whole point is that a player who did not know
 * they had an xray pack should not have to guess what to do. Quitting to fix it leaves no record;
 * enabling cheating writes the opt-in and flags the save. There is deliberately no "ignore" — the
 * screen would otherwise become something you dismiss without reading.</p>
 */
public final class CheatChoiceScreen extends Screen {

    private static final int WIDTH = 340;

    private final CheatDetectedPacket detection;
    private final List<Component> body = new ArrayList<>();

    public CheatChoiceScreen(CheatDetectedPacket detection) {
        super(Component.literal("Cheat Detected"));
        this.detection = detection;
    }

    @Override
    protected void init() {
        body.clear();
        body.add(Component.literal("Souls of Avarice found a " + detection.category()
                + " that lets you cheat:").withStyle(ChatFormatting.WHITE));
        body.add(Component.literal(detection.detail()).withStyle(ChatFormatting.YELLOW));
        body.add(Component.empty());
        body.add(Component.literal("This is your own world, so it is your call.")
                .withStyle(ChatFormatting.GRAY));
        body.add(Component.empty());
        body.add(Component.literal("Quit and remove it and nothing is recorded against you.")
                .withStyle(ChatFormatting.GRAY));
        body.add(Component.literal("Deleting the file is what counts — turning a pack off in the")
                .withStyle(ChatFormatting.DARK_GRAY));
        body.add(Component.literal("selector leaves it on disk, and it will be found again.")
                .withStyle(ChatFormatting.DARK_GRAY));

        int y = this.height / 2 + 40;
        int btnW = 220;
        int x = (this.width - btnW) / 2;

        this.addRenderableWidget(Button.builder(
                Component.literal("Quit and fix it").withStyle(ChatFormatting.GREEN),
                b -> quitToTitle()).bounds(x, y, btnW, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Enable cheating for this world").withStyle(ChatFormatting.RED),
                b -> enableCheating()).bounds(x, y + 24, btnW, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("What does that mean?").withStyle(ChatFormatting.DARK_GRAY),
                b -> explain()).bounds(x, y + 48, btnW, 20).build());
    }

    private void quitToTitle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) mc.level.disconnect();
        mc.clearLevel();
        mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
    }

    private void enableCheating() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // The command is the single source of truth for the opt-in, so the button runs it
            // rather than duplicating the state change over another packet.
            mc.player.connection.sendCommand("soa quests cheatermode true");
        }
        this.onClose();
    }

    private void explain() {
        body.add(Component.empty());
        body.add(Component.literal("Enabling cheating marks this save as modified. Quests still")
                .withStyle(ChatFormatting.GRAY));
        body.add(Component.literal("work; the mark travels with the save and servers may refuse it.")
                .withStyle(ChatFormatting.GRAY));
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
        int y = this.height / 2 - 80;

        g.drawCenteredString(this.font, Component.literal("Cheat Detected")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), cx, y, 0xFFFFFF);
        y += 18;
        for (Component line : body) {
            g.drawCenteredString(this.font, line, cx, y, 0xFFFFFF);
            y += 11;
        }
        super.render(g, mouseX, mouseY, partial);
    }
}
