package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;

/**
 * HANDLE modifier — ShadowTool.
 * In the original 1.12 source this modifier has no implemented behavior
 * (empty onUpdate/hitEntity/etc). It exists as a registered modifier
 * that can be applied to stasis tools but does nothing at runtime.
 * The damage boost for deactivated stasis tools is handled in
 * the stasis tool classes themselves, not by this modifier.
 */
public class ModifierShadowTool extends Modifier {

    public ModifierShadowTool() {
        super("shadow_tool", EnumModifierType.HANDLE, 5);
    }
}
