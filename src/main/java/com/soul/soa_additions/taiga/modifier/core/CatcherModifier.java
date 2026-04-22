package com.soul.soa_additions.taiga.modifier.core;

import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * TAIGA "catcher": capture-and-release a killed mob via alt+right-click. The
 * 1.12 implementation serialized the entity class name into tool NBT and
 * re-instantiated it via reflection — that's not safe in 1.20 with data-driven
 * entities. Port is a stub: the trait exists so material JSONs can reference it,
 * but the capture mechanic is dropped.
 */
public class CatcherModifier extends Modifier {
}
