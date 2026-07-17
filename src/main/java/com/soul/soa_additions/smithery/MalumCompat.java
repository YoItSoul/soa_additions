package com.soul.soa_additions.smithery;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Isolated Malum calls — only classloaded when malum is present (callers must
 * check {@code ModList.get().isLoaded("malum")} first).
 *
 * <p>Soul harvest: Malum's scythes shatter a slain mob's soul into arcane spirit
 * items ({@code SpiritHarvestHandler}); the mod gates its whole progression on
 * them. Malum's own death handler ignores non-scythe weapons, so calling
 * {@code spawnSpirits} from our trait doesn't double-drop.</p>
 */
final class MalumCompat {

    private MalumCompat() {}

    static void spawnSpirits(LivingEntity victim, LivingEntity attacker, ItemStack weapon) {
        com.sammy.malum.core.handlers.SpiritHarvestHandler.spawnSpirits(victim, attacker, weapon);
    }
}
