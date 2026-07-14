package com.soul.soa_additions.tinkersaether.modifier;

import com.soul.soa_additions.tinkersaether.modifier.core.AntigravModifier;
import com.soul.soa_additions.tinkersaether.modifier.core.CushyModifier;
import com.soul.soa_additions.tinkersaether.modifier.core.EnlightenedModifier;
import com.soul.soa_additions.tinkersaether.modifier.core.FestiveModifier;
import com.soul.soa_additions.tinkersaether.modifier.core.GildedModifier;
import com.soul.soa_additions.tinkersaether.modifier.core.LaunchingModifier;
import com.soul.soa_additions.tinkersaether.modifier.core.ReachAetherModifier;
import com.soul.soa_additions.tinkersaether.modifier.core.RefrigerationModifier;
import com.soul.soa_additions.tinkersaether.modifier.core.SkyrootedModifier;
import com.soul.soa_additions.tinkersaether.modifier.core.SwettyModifier;
import com.soul.soa_additions.tinkersaether.modifier.core.ZanyModifier;

/**
 * Binds the eleven custom Tinkers' Aether trait classes to {@link
 * TinkersAetherModifiers} ids. Registration fires during
 * {@code ModifierRegistrationEvent}; this class only queues factories.
 *
 * <p>Trait names match the lower-case 1.12 trait names. Material JSON traits
 * reference these as {@code soa_additions:&lt;name&gt;}.</p>
 */
public final class TinkersAetherTraitList {

    public static final TinkersAetherModifiers.Holder<SkyrootedModifier>     SKYROOTED     = TinkersAetherModifiers.register("skyrooted",     SkyrootedModifier::new);
    public static final TinkersAetherModifiers.Holder<EnlightenedModifier>   ENLIGHTENED   = TinkersAetherModifiers.register("enlightened",   EnlightenedModifier::new);
    public static final TinkersAetherModifiers.Holder<GildedModifier>        GILDED        = TinkersAetherModifiers.register("gilded",        GildedModifier::new);
    public static final TinkersAetherModifiers.Holder<AntigravModifier>      ANTIGRAV      = TinkersAetherModifiers.register("antigrav",      AntigravModifier::new);
    public static final TinkersAetherModifiers.Holder<LaunchingModifier>     LAUNCHING     = TinkersAetherModifiers.register("launching",     LaunchingModifier::new);
    public static final TinkersAetherModifiers.Holder<ReachAetherModifier>   REACH         = TinkersAetherModifiers.register("reach_aether", ReachAetherModifier::new);
    public static final TinkersAetherModifiers.Holder<CushyModifier>         CUSHY         = TinkersAetherModifiers.register("cushy",         CushyModifier::new);
    public static final TinkersAetherModifiers.Holder<FestiveModifier>       FESTIVE       = TinkersAetherModifiers.register("festive",       FestiveModifier::new);
    public static final TinkersAetherModifiers.Holder<RefrigerationModifier> REFRIGERATION = TinkersAetherModifiers.register("refrigeration", RefrigerationModifier::new);
    public static final TinkersAetherModifiers.Holder<SwettyModifier>        SWETTY        = TinkersAetherModifiers.register("swetty",        SwettyModifier::new);
    public static final TinkersAetherModifiers.Holder<ZanyModifier>          ZANY          = TinkersAetherModifiers.register("zany",          ZanyModifier::new);

    private TinkersAetherTraitList() {}

    public static void bootstrap() {
        // static init of the fields above enqueues the eleven holders
    }
}
