package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;

/**
 * CORE modifier — Xperienced.
 * In the original 1.12 source this modifier has no implemented behavior
 * (empty onUpdate/hitEntity/etc). It exists as a registered modifier
 * that can be applied to stasis tools but does nothing at runtime.
 */
public class ModifierXperienced extends Modifier {

    public ModifierXperienced() {
        super("xperienced", EnumModifierType.CORE, 3);
    }
}
