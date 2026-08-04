package com.soul.soa_additions.mixin.itemstages;

import com.google.common.collect.Multimap;
import net.darkhax.itemstages.Restriction;
import net.darkhax.itemstages.RestrictionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes ItemStages' restriction table so the parity dump can enumerate it.
 *
 * <p>{@link RestrictionManager} offers no way to ask "what is staged, and to
 * which stage" — its only public query takes a player and a single stack and
 * returns the first blocking restriction. Reading the backing multimap directly
 * is the only way to produce a complete stage-by-stage listing to diff against
 * GreedyCraft's crafttweaker.log.</p>
 */
@Mixin(value = RestrictionManager.class, remap = false)
public interface RestrictionManagerAccessor {

    @Accessor("restrictions")
    Multimap<String, Restriction> soa$restrictions();
}
