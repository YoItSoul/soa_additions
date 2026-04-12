package com.soul.soa_additions.donor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Beautiful donor wall GUI. Shows all donors in a scrollable, tier-grouped
 * layout with glowing name cards, tier symbols, and personal messages.
 */
@OnlyIn(Dist.CLIENT)
public final class DonorWallScreen extends Screen {

    private static final int CARD_W = 220;
    private static final int CARD_H = 52;
    private static final int CARD_GAP = 8;
    private static final int HEADER_H = 50;
    private static final int FOOTER_H = 20;

    // Dark elegant palette
    private static final int COL_BG = 0xE00A0D18;
    private static final int COL_CARD_BG = 0xF0141828;
    private static final int COL_CARD_BORDER = 0xFF2A3050;
    private static final int COL_TITLE = 0xFFFFD700;
    private static final int COL_SUBTITLE = 0xFF8A91AE;
    private static final int COL_TEXT = 0xFFE6E9F5;
    private static final int COL_TEXT_DIM = 0xFF6A7192;
    private static final int COL_SEPARATOR = 0xFF2A3050;
    private static final int COL_HEADER_BG = 0xF0101530;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());

    private double scrollOffset;
    private int totalContentHeight;

    public DonorWallScreen() {
        super(Component.literal("Donor Wall"));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        // Full-screen dark background
        g.fill(0, 0, this.width, this.height, COL_BG);

        List<DonorData> donors = ClientDonorCache.all();

        // Calculate layout
        int contentW = Math.min(CARD_W * 2 + CARD_GAP + 40, this.width - 40);
        int cols = contentW >= CARD_W * 2 + CARD_GAP ? 2 : 1;
        int cardW = cols == 1 ? Math.min(CARD_W, this.width - 40) : CARD_W;
        int gridW = cols * cardW + (cols - 1) * CARD_GAP;
        int leftX = (this.width - gridW) / 2;

        // Header
        g.fill(0, 0, this.width, HEADER_H, COL_HEADER_BG);
        g.fill(0, HEADER_H - 1, this.width, HEADER_H, COL_SEPARATOR);

        // Title with decorative lines
        String title = "\u2728  DONOR WALL  \u2728";
        int titleW = this.font.width(title);
        int titleX = (this.width - titleW) / 2;
        g.drawString(this.font, title, titleX, 12, COL_TITLE, true);

        String subtitle = donors.isEmpty()
                ? "No donors yet \u2014 be the first!"
                : donors.size() + " amazing " + (donors.size() == 1 ? "supporter" : "supporters");
        int subW = this.font.width(subtitle);
        g.drawString(this.font, subtitle, (this.width - subW) / 2, 28, COL_SUBTITLE, false);

        // Decorative lines flanking the title
        int lineY = 16;
        g.fill(titleX - 60, lineY, titleX - 8, lineY + 1, COL_SEPARATOR);
        g.fill(titleX + titleW + 8, lineY, titleX + titleW + 60, lineY + 1, COL_SEPARATOR);

        // Scrollable content area
        int viewTop = HEADER_H;
        int viewBottom = this.height - FOOTER_H;
        int viewH = viewBottom - viewTop;

        // Compute total content height
        int rows = (donors.size() + cols - 1) / cols;
        totalContentHeight = rows * (CARD_H + CARD_GAP) + 20;
        int maxScroll = Math.max(0, totalContentHeight - viewH);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        // Clip to content area
        g.enableScissor(0, viewTop, this.width, viewBottom);

        int baseY = viewTop + 10 - (int) scrollOffset;

        // Render tier sections
        DonorData.Tier currentTier = null;
        int cardIdx = 0;

        for (int i = 0; i < donors.size(); i++) {
            DonorData d = donors.get(i);

            // Tier header when the tier changes
            if (d.tier() != currentTier) {
                // Flush partial row
                if (cardIdx % cols != 0) {
                    baseY += CARD_H + CARD_GAP;
                    cardIdx = 0;
                }
                if (currentTier != null) baseY += 8; // extra gap between tier sections

                currentTier = d.tier();
                // Tier banner
                String tierLabel = currentTier.symbol + "  " + currentTier.display.toUpperCase() + "S  " + currentTier.symbol;
                int tierW = this.font.width(tierLabel);
                int tierX = (this.width - tierW) / 2;
                int bannerY = baseY;

                if (bannerY + 16 > viewTop && bannerY < viewBottom) {
                    g.fill(leftX, bannerY + 5, leftX + gridW, bannerY + 6, currentTier.color & 0x40FFFFFF);
                    g.drawString(this.font, tierLabel, tierX, bannerY, currentTier.color, true);
                }
                baseY += 18;
                cardIdx = 0;
            }

            // Card position
            int col = cardIdx % cols;
            int row = cardIdx / cols;
            int cx = leftX + col * (cardW + CARD_GAP);
            int cy = baseY + row * (CARD_H + CARD_GAP);

            // Only render if visible
            if (cy + CARD_H > viewTop && cy < viewBottom) {
                renderDonorCard(g, cx, cy, cardW, d, mouseX, mouseY);
            }

            cardIdx++;

            // If this is the last donor or next donor has different tier, advance baseY
            if (i + 1 >= donors.size() || donors.get(i + 1).tier() != currentTier) {
                int usedRows = (cardIdx + cols - 1) / cols;
                baseY += usedRows * (CARD_H + CARD_GAP);
                cardIdx = 0;
            }
        }

        totalContentHeight = baseY + (int) scrollOffset - viewTop + 10;

        g.disableScissor();

        // Scroll bar
        if (totalContentHeight > viewH) {
            int barH = Math.max(20, viewH * viewH / totalContentHeight);
            int barY = viewTop + (int) ((viewH - barH) * scrollOffset / maxScroll);
            g.fill(this.width - 6, viewTop, this.width - 2, viewBottom, 0x30FFFFFF);
            g.fill(this.width - 5, barY, this.width - 3, barY + barH, 0x80FFFFFF);
        }

        // Footer
        g.fill(0, viewBottom, this.width, this.height, COL_HEADER_BG);
        g.fill(0, viewBottom, this.width, viewBottom + 1, COL_SEPARATOR);
        String footer = "Thank you for your support!";
        g.drawString(this.font, footer, (this.width - this.font.width(footer)) / 2,
                viewBottom + 5, COL_SUBTITLE, false);

        super.render(g, mouseX, mouseY, pt);
    }

    private void renderDonorCard(GuiGraphics g, int x, int y, int w, DonorData d,
                                  int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + CARD_H;

        // Card background with tier-tinted top edge
        int bg = hover ? 0xF01A2038 : COL_CARD_BG;
        g.fill(x, y, x + w, y + CARD_H, bg);
        // Tier color accent on top
        g.fill(x, y, x + w, y + 2, d.tier().color);
        // Border
        drawCardBorder(g, x, y, w, CARD_H, hover ? d.tier().color : COL_CARD_BORDER);

        // Tier symbol
        g.drawString(this.font, d.tier().symbol, x + 6, y + 6, d.tier().color, true);

        // Player name in tier color
        g.drawString(this.font, d.name(), x + 20, y + 6, d.tier().color, true);

        // Tier badge
        String badge = d.tier().display;
        int badgeW = this.font.width(badge) + 6;
        int badgeX = x + w - badgeW - 4;
        g.fill(badgeX, y + 5, badgeX + badgeW, y + 16, d.tier().color & 0x30FFFFFF);
        g.drawString(this.font, badge, badgeX + 3, y + 6, d.tier().color, false);

        // Date
        String date = DATE_FMT.format(d.donatedAt());
        g.drawString(this.font, date, x + 6, y + 20, COL_TEXT_DIM, false);

        // Message (word-wrapped, max 2 lines)
        if (d.message() != null && !d.message().isEmpty()) {
            List<FormattedCharSequence> lines = this.font.split(
                    Component.literal("\"" + d.message() + "\""), w - 12);
            int lineY = y + 32;
            for (int i = 0; i < Math.min(2, lines.size()); i++) {
                g.drawString(this.font, lines.get(i), x + 6, lineY, COL_TEXT, false);
                lineY += 10;
            }
        }
    }

    private static void drawCardBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);           // top
        g.fill(x, y + h - 1, x + w, y + h, color);   // bottom
        g.fill(x, y, x + 1, y + h, color);            // left
        g.fill(x + w - 1, y, x + w, y + h, color);   // right
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollOffset -= delta * 20;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
