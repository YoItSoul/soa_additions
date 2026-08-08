package com.soul.soa_additions.mixin.donor;

import com.soul.soa_additions.donor.DonorStyles;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Animates text stamped with a {@code soa_additions:donor_*} marker font.
 *
 * <p>{@code Font$StringRenderOutput#accept} runs once per glyph, in order, every
 * frame, and reads the colour out of the {@link Style} exactly once. Answering
 * with a time-varying colour animates the run without touching the chat
 * pipeline, the glyph cache, or the component tree.
 *
 * <h2>Why it is shaped like this</h2>
 * This mod ships no mixin refmap (see the {@code MixinConfigs} note in
 * build.gradle) because every other mixin here targets another mod's
 * un-obfuscated classes. This one targets vanilla, where member names are SRG at
 * runtime — {@code accept} is {@code m_6411_} in the shipped jar, and
 * {@code Style#getColor} is {@code m_131135_}. So:
 * <ul>
 *   <li>{@code method} lists both the Mojang and SRG names; whichever exists in
 *       the current environment matches and the other is ignored.</li>
 *   <li>The injection points ({@code HEAD} and {@code STORE}) name no Minecraft
 *       member at all, so neither needs remapping. That rules out a single
 *       {@code @Redirect} on {@code getColor()} — its target descriptor would
 *       only be correct in one of the two environments.</li>
 * </ul>
 * With {@code defaultRequire: 0}, a failure to apply leaves donor names at their
 * flat tier colour rather than crashing.
 *
 * <p>A fresh {@code StringRenderOutput} is built per draw call (and again for
 * the shadow pass), so the run-tracking fields never leak between draws.
 */
@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public class DonorGradientMixin {

    /** Style of the glyph currently being drawn, captured on entry. */
    @Unique private Style soa$style;
    /** Index of that glyph within the string being drawn. */
    @Unique private int soa$index;
    /** Ramp of the run in progress, or null when not inside a marked run. */
    @Unique private DonorStyles.Gradient soa$run;
    /** Index the current run started at. */
    @Unique private int soa$runStart;

    @Inject(method = { "accept", "m_6411_" }, at = @At("HEAD"), require = 0)
    private void soa$captureGlyph(int index, Style style, int codePoint,
                                  CallbackInfoReturnable<Boolean> cir) {
        this.soa$index = index;
        this.soa$style = style;
    }

    @ModifyVariable(method = { "accept", "m_6411_" }, at = @At("STORE"), ordinal = 0, require = 0)
    private TextColor soa$shine(TextColor color) {
        Style style = this.soa$style;
        if (style == null) return color;

        DonorStyles.Gradient gradient = DonorStyles.gradientFor(style.getFont());
        if (gradient == null) {
            // Ordinary text — also closes any run we were tracking, so the next
            // marked stretch is measured from its own first character.
            this.soa$run = null;
            return color;
        }

        if (gradient != this.soa$run) {
            this.soa$run = gradient;
            this.soa$runStart = this.soa$index;
        }

        return TextColor.fromRgb(
                DonorStyles.shine(gradient, this.soa$index - this.soa$runStart, this.soa$runStart));
    }
}
