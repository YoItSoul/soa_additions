package com.soul.soa_additions.bloodarsenal.item.sigil;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.item.sigil.ItemSigilToggleableBase;

/**
 * Divinity Sigil — when active: zero fall damage, Absorption V (amplifier 4),
 * full health restore every tick.
 * Damage cancellation is handled separately via BAEventHandler.
 *
 * <p>Ported from: arcaratus.bloodarsenal.item.sigil.ItemSigilDivinity</p>
 */
public class DivinitySigilItem extends ItemSigilToggleableBase {

    public DivinitySigilItem() {
        super("divinity", BAConfig.SIGIL_DIVINITY_COST.get());
    }

    @Override
    public void onSigilUpdate(ItemStack stack, Level level, Player player, int slot, boolean isSelected) {
        if (level.isClientSide()) return;

        // Full health restore every tick
        if (player.getHealth() < player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        // Absorption V (amplifier 4, duration 2 ticks — refreshed every tick)
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2, 4, true, false));

        // Zero fall damage via fall distance reset
        player.fallDistance = 0.0f;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }
}
