package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Entropic — 1.12.2 trait that inflicted "entropy" on Draconic armour by
 * writing into the DE energy-shield capability. The DE capability hook that
 * 1.12.2 called ({@code DraconicHooks.inflictEntropy}) doesn't exist on
 * 1.20.1 Draconic Evolution, so this class is a marker until upstream exposes
 * an equivalent. Including the registration keeps the material trait list
 * consistent with the 1.12.2 asset.
 */
public class EntropicModifier extends Modifier {
}
