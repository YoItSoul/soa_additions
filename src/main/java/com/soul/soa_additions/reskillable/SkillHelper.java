package com.soul.soa_additions.reskillable;

import net.bandit.reskillable.common.capabilities.SkillCapability;
import net.bandit.reskillable.common.capabilities.SkillModel;
import net.bandit.reskillable.common.commands.skills.Skill;
import net.minecraft.world.entity.player.Player;

/**
 * Thin wrapper over Reskillable's per-player {@link SkillCapability}. Returns
 * 0 when the capability is absent (e.g. early in player setup or for fake
 * players spawned by automation mods).
 *
 * <p>All trait handlers in {@link ReskillableTraits} read levels through
 * this helper so the capability lookup pattern stays in one place.
 */
public final class SkillHelper {

    private SkillHelper() {}

    public static int getLevel(Player player, Skill skill) {
        if (player == null) return 0;
        return player.getCapability(SkillCapability.INSTANCE)
                .map(model -> model.getSkillLevel(skill))
                .orElse(0);
    }

    /** True iff the player has at least the given level in the given skill. */
    public static boolean hasLevel(Player player, Skill skill, int min) {
        return getLevel(player, skill) >= min;
    }

    /** True iff the player meets two skill thresholds at once. */
    public static boolean hasLevels(Player player, Skill a, int aMin, Skill b, int bMin) {
        if (player == null) return false;
        return player.getCapability(SkillCapability.INSTANCE).map(model ->
                model.getSkillLevel(a) >= aMin && model.getSkillLevel(b) >= bMin
        ).orElse(false);
    }
}
