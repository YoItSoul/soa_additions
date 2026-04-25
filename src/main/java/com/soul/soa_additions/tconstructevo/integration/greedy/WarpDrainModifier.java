package com.soul.soa_additions.tconstructevo.integration.greedy;

import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Warp Drain — original GC trait reduced the player's Thaumcraft warp
 * counter every 18000 ticks and granted a random buff if a warp point
 * was successfully drained. Thaumcraft isn't in the SoA pack (only
 * Thaumon, the decorative analog, which has no warp system), so the
 * trait has no firing condition and is left as a no-op marker. If a
 * future mod re-introduces warp via a public capability, fill in the
 * INVENTORY_TICK hook here.
 */
public class WarpDrainModifier extends Modifier { }
