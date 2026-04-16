package com.soul.soa_additions.bloodarsenal.modifier;

/**
 * Categories of stasis tool modifiers, each with a maximum number of
 * concurrent slots on a single tool.
 */
public enum EnumModifierType {
    HEAD(3),
    CORE(2),
    HANDLE(2),
    ABILITY(1);

    private final int maxSlots;

    EnumModifierType(int maxSlots) {
        this.maxSlots = maxSlots;
    }

    public int getMaxSlots() { return maxSlots; }
}
