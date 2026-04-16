package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * HEAD modifier — sets targets on fire. Incompatible with BadPotion.
 * Fire damage = level+1, fire ticks = (level+1)*2 seconds.
 */
public class ModifierFlame extends Modifier {

    public ModifierFlame() {
        super("flame", EnumModifierType.HEAD, 5);
    }

    @Override
    public void hitEntity(Level level, Player player, ItemStack stack, LivingEntity target, int modLevel) {
        target.setSecondsOnFire((modLevel + 1) * 2);
    }
}
