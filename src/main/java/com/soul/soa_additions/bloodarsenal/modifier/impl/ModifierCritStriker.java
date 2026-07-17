package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.BASounds;
import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * HEAD modifier — CritStriker. The 1.12 behavior lived in TrackerHandler, not the
 * modifier class: on hit, (level+1)/10 chance to crit — bonus damage, "Critical
 * Hit!" action-bar message, and the bloodarsenal:crit sound at 0.2 volume.
 */
public class ModifierCritStriker extends Modifier {

    public ModifierCritStriker() {
        super("crit_striker", EnumModifierType.HEAD, 5);
    }

    @Override
    public void hitEntity(Level level, Player player, ItemStack stack, LivingEntity target, int modLevel) {
        if (level.isClientSide() || !target.isAlive()) return;
        if (level.getRandom().nextInt(10) >= modLevel + 1) return;

        float bonus = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        target.hurt(player.damageSources().playerAttack(player), bonus);
        player.displayClientMessage(Component.translatable("chat.bloodarsenal.crit"), true);
        level.playSound(null, player.blockPosition(), BASounds.CRIT.get(), SoundSource.PLAYERS, 0.2f, 1.0f);
    }
}
