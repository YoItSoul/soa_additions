package com.soul.soa_additions.bloodarsenal.ritual;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import wayoftime.bloodmagic.ritual.RitualRegister;
import wayoftime.bloodmagic.ritual.imperfect.IImperfectRitualStone;
import wayoftime.bloodmagic.ritual.imperfect.ImperfectRitual;

/**
 * Imperfect Ritual: Enchant Reset — resets player's enchantment seed.
 * In original: adds 1 level then removes 1, which resets the enchant table seed.
 * Requires a bookshelf above.
 *
 * <p>Ported from: arcaratus.bloodarsenal.ritual.imperfect.ImperfectRitualEnchantReset</p>
 */
@RitualRegister.Imperfect("blood_arsenal_imperfect_enchant_reset")
public class ImperfectRitualEnchantReset extends ImperfectRitual {

    public ImperfectRitualEnchantReset() {
        super("blood_arsenal_imperfect_enchant_reset",
                state -> state.is(Blocks.BOOKSHELF),
                5000, "ritual.soa_additions.imperfect_enchant_reset");
    }

    @Override
    public boolean onActivate(IImperfectRitualStone stone, Player player) {
        // Reset enchantment seed by giving+removing 1 level (matches original behavior)
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.giveExperienceLevels(1);
            serverPlayer.giveExperienceLevels(-1);
        }
        return true;
    }
}
