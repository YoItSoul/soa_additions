package com.soul.soa_additions.deckmode;

import com.soul.soa_additions.liteboot.DeckMode;
import com.soul.soa_additions.liteboot.LiteMode;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Adds the performance toggle buttons to the title screen, stacked in the
 * top-right corner: Steam Deck Mode on top, Lite Mode below it. Widgets are
 * registered through the vanilla screen-init event, which FancyMenu leaves
 * alone (it customizes the vanilla TitleScreen instance rather than replacing
 * it), so the buttons survive layout edits without living in the layout file.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DeckModeClientEvents {

    private static final ResourceLocation DECK_ICON =
            new ResourceLocation(SoaAdditions.MODID, "textures/gui/deck_mode.png");
    private static final ResourceLocation LITE_ICON =
            new ResourceLocation(SoaAdditions.MODID, "textures/gui/lite_mode.png");

    /**
     * Whether {@code soa_litemode.jar} is present.
     *
     * <p>Deck/Lite Mode rename jars in the mods folder, which has to happen
     * before the mod loader opens them — so that half lives in a separate
     * boot-layer jar that ships with the modpack, not with this mod. A
     * standalone install of soa_additions (e.g. straight from CurseForge) has
     * nothing to disable and won't include it, so the toggles have to degrade
     * to "not offered" rather than throwing NoClassDefFoundError and taking the
     * title screen down with them.
     */
    private static final boolean LITE_BOOT_PRESENT = liteBootPresent();

    private static boolean liteBootPresent() {
        try {
            Class.forName("com.soul.soa_additions.liteboot.LiteMode", false,
                    DeckModeClientEvents.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            org.slf4j.LoggerFactory.getLogger("soa_additions").info(
                    "soa_litemode.jar not present — Deck/Lite Mode toggles hidden");
            return false;
        }
    }

    private DeckModeClientEvents() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!LITE_BOOT_PRESENT) {
            return;
        }
        if (!(event.getScreen() instanceof TitleScreen screen)) {
            return;
        }
        // Keep this add order stable: FancyMenu identifies unlabeled mod
        // widgets by an order-sensitive hash, and layouts may reference it.
        event.addListener(new DeckToggleButton(screen.width - 24, 4));
        event.addListener(new LiteToggleButton(screen.width - 24, 26));
    }

    /** Shared 20x20 icon button with an active-state dot in the corner. */
    private abstract static class IconToggleButton extends Button {
        private final ResourceLocation icon;

        IconToggleButton(int x, int y, ResourceLocation icon, OnPress onPress) {
            super(x, y, 20, 20, Component.empty(), onPress, DEFAULT_NARRATION);
            this.icon = icon;
        }

        abstract boolean isModeActive();

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            graphics.blit(icon, getX() + 2, getY() + 2, 0, 0, 16, 16, 16, 16);
            int color = isModeActive() ? 0xFF55FF55 : 0xFF555555;
            graphics.fill(getX() + 15, getY() + 15, getX() + 18, getY() + 18, color);
        }
    }

    private static final class DeckToggleButton extends IconToggleButton {

        private DeckToggleButton(int x, int y) {
            super(x, y, DECK_ICON, DeckToggleButton::onPressed);
            updateTooltip(this);
        }

        @Override
        boolean isModeActive() {
            return DeckMode.isActive();
        }

        private static void onPressed(Button button) {
            boolean active = DeckMode.toggle();
            updateTooltip(button);
            Minecraft mc = Minecraft.getInstance();
            SystemToast.add(mc.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                    Component.literal(active ? "Steam Deck Mode: ON" : "Steam Deck Mode: OFF"),
                    Component.literal(active
                            ? "Handheld settings applied, shaders + animation packs off"
                            : "Your previous settings were restored"));
        }

        private static void updateTooltip(Button button) {
            boolean active = DeckMode.isActive();
            button.setTooltip(Tooltip.create(Component.literal(
                    active
                            ? "Steam Deck Mode is ON — click to restore your previous settings"
                            : "Steam Deck Mode: apply handheld-optimized settings"
                            + " (lower distances, shaders off, animation packs off)."
                            + " Triggers a resource reload.")));
        }
    }

    private static final class LiteToggleButton extends IconToggleButton {

        private LiteToggleButton(int x, int y) {
            super(x, y, LITE_ICON, LiteToggleButton::onPressed);
            updateTooltip(this);
        }

        @Override
        boolean isModeActive() {
            return LiteMode.isActive();
        }

        private static void onPressed(Button button) {
            boolean enabling = !LiteMode.isActive();
            int count = LiteMode.applyToggle();
            Minecraft mc = Minecraft.getInstance();
            if (count <= 0) {
                SystemToast.add(mc.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                        Component.literal("Lite Mode"),
                        Component.literal(count == 0
                                ? "Nothing to change — no target mods found."
                                : "Could not schedule the change — see the log."));
                updateTooltip(button);
                return;
            }
            mc.setScreen(new LiteCountdownScreen(enabling, count));
        }

        private static void updateTooltip(Button button) {
            boolean active = LiteMode.isActive();
            button.setTooltip(Tooltip.create(Component.literal(
                    active
                            ? "Lite Mode is ON — click to restore the disabled mods."
                            + " WARNING: the game will close and must be relaunched!"
                            : "Lite Mode: disables all cosmetic client-side mods"
                            + " (animations, ambient audio, overlays) and forces"
                            + " ModernFix dynamic resources ON for maximum FPS."
                            + " WARNING: the game will close and must be relaunched!")));
        }
    }
}
