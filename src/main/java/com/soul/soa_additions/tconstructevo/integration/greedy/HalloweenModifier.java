package com.soul.soa_additions.tconstructevo.integration.greedy;

import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Halloween — original GC trait had a 1% chance on mob hit to drop an
 * extrabotany:candybag (a special bag rolling random sweets from a
 * loot table). Extra Botany isn't in the SoA pack, and there's no
 * direct 1.20.1 analog with the same "random sweets bag" concept.
 * Trait is left as a no-op marker per project convention. If a future
 * mod adds a comparable bag, fill in the MELEE_HIT hook here.
 */
public class HalloweenModifier extends Modifier { }
