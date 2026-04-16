package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;

/**
 * ABILITY modifier — Area of Destruction.
 * Activated on right-click release (charge system).
 * Range = charge * (level+1) / maxLevel.
 * Sword mode: AOE damage. Tool mode: break all blocks in range.
 * LP cost: (charge^3 * (level+1)) / 2.7
 */
public class ModifierAOD extends Modifier {

    public ModifierAOD() {
        super("aod", EnumModifierType.ABILITY, 5);
    }

    @Override
    public void onRelease(Level level, Player player, ItemStack stack, int charge, int modLevel) {
        if (level.isClientSide() || charge <= 0) return;

        double range = (double) charge * (modLevel + 1) / getMaxLevel();
        int lpCost = (int) (Math.pow(charge, 3) * (modLevel + 1) / 2.7);

        // Drain LP
        Binding binding = Binding.fromStack(stack);
        if (binding != null) {
            NetworkHelper.getSoulNetwork(binding).syphonAndDamage(player,
                    SoulTicket.item(stack, level, player, lpCost));
        }

        // AOE damage to nearby entities
        AABB aoe = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aoe,
                e -> e != player && e.isAlive());

        float damage = 4.0f + modLevel * 2.0f;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), damage);
        }

        // Break blocks in range (for tool mode)
        if (level instanceof ServerLevel serverLevel) {
            BlockPos center = player.blockPosition();
            int r = (int) Math.ceil(range);
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (pos.distSqr(center) <= range * range) {
                            var state = serverLevel.getBlockState(pos);
                            if (!state.isAir() && state.getDestroySpeed(serverLevel, pos) >= 0) {
                                serverLevel.destroyBlock(pos, true, player);
                            }
                        }
                    }
                }
            }
        }
    }
}
