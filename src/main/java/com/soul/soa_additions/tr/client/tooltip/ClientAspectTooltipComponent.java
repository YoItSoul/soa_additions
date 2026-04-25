package com.soul.soa_additions.tr.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soul.soa_additions.tr.ThaumicRemnants;
import com.soul.soa_additions.tr.core.AspectStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Renders the aspect line under an item tooltip: each known aspect as
 * {@code <16x16 rune> <amount>}, laid out left-to-right, wrapping handled by
 * vanilla's tooltip layout rather than us (everything fits on one line for
 * any item with ≤ 8 aspects).
 *
 * <p>Texture path mirrors the asset layout chosen when we copied the runes:
 * {@code assets/tr/textures/aspect/<aspect-path>.png}, 16x16, RGBA. Drawn
 * with no tint — the rune art is already coloured. The amount text uses the
 * font's default colour (white) to stay legible against vanilla tooltips.
 */
public final class ClientAspectTooltipComponent implements ClientTooltipComponent {

    private static final int ICON_SIZE = 16;
    private static final int TEXT_GAP  = 1;        // rune→amount
    private static final int ENTRY_GAP = 6;        // amount→next rune
    private static final int LINE_PAD  = 2;        // top padding

    private final List<AspectStack> aspects;

    public ClientAspectTooltipComponent(AspectTooltipComponent data) {
        this.aspects = data.aspects();
    }

    @Override
    public int getHeight() {
        return ICON_SIZE + LINE_PAD;
    }

    @Override
    public int getWidth(Font font) {
        int w = 0;
        for (AspectStack as : aspects) {
            w += ICON_SIZE + TEXT_GAP + font.width(String.valueOf(as.amount())) + ENTRY_GAP;
        }
        // Trim the trailing gap so wide tooltips don't get extra slack.
        return Math.max(0, w - ENTRY_GAP);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics g) {
        int cx = x;
        int textY = y + (ICON_SIZE - font.lineHeight) / 2 + 1;
        for (AspectStack as : aspects) {
            ResourceLocation tex = new ResourceLocation(
                    ThaumicRemnants.MODID,
                    "textures/aspect/" + as.aspect().id().getPath() + ".png");
            // GuiGraphics.blit handles texture binding internally in 1.20.1.
            // Reset color in case a previous render path left a tint set.
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            g.blit(tex, cx, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            cx += ICON_SIZE + TEXT_GAP;
            String amt = String.valueOf(as.amount());
            // Drop-shadowed white — same convention as vanilla item count text.
            g.drawString(font, amt, cx, textY, 0xFFFFFFFF, true);
            cx += font.width(amt) + ENTRY_GAP;
        }
    }
}
