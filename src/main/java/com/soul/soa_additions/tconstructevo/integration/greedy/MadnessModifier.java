package com.soul.soa_additions.tconstructevo.integration.greedy;

import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Madness — original GC trait scaled damage with the player's Thaumcraft
 * warp counter (warpNormal + warpTemporary + warpPermanent). Thaumcraft
 * isn't in the SoA pack (only Thaumon, the decorative analog, which has
 * no warp system), so this trait is left as a no-op marker. If a future
 * mod re-introduces warp via a public capability, fill in the
 * MELEE_DAMAGE hook here.
 */
public class MadnessModifier extends Modifier { }
