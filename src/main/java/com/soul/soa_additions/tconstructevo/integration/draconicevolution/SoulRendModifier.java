package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * SoulRend — the 1.12.2 trait of Awakened Draconium weapons that conflicted
 * with Reaper-family enchantments. 1.20.1 TConstruct doesn't gate enchantment
 * compatibility through modifiers (enchantments are handled at the tool-type
 * level via tags now), so this class is a pure marker — it exists so the
 * Draconic material can still list {@code soa_additions:tconevo/soul_rend}
 * as a trait in its traits JSON, preserving visual parity.
 */
public class SoulRendModifier extends Modifier {
}
