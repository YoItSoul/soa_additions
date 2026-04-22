package com.soul.soa_additions.nyx.block;

import com.soul.soa_additions.nyx.NyxWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MeteorRockBlock extends Block {

    public MeteorRockBlock(Properties props) {
        super(props);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level instanceof ServerLevel sl) {
            NyxWorldData data = NyxWorldData.get(sl);
            if (data != null) {
                data.meteorLandingSites.remove(pos);
                data.setDirty();
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.fireImmune() && entity instanceof LivingEntity le
                && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FROST_WALKER,
                        le.getItemBySlot(EquipmentSlot.FEET)) <= 0) {
            entity.hurt(level.damageSources().hotFloor(), 1.0f);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        for (int i = 0; i < 3; i++) {
            boolean side = rand.nextBoolean();
            float x = side ? rand.nextFloat() : (rand.nextBoolean() ? 1.0f : 0.0f);
            float z = !side ? rand.nextFloat() : (rand.nextBoolean() ? 1.0f : 0.0f);
            float y = rand.nextBoolean() ? 1.0f : 0.0f;
            level.addParticle(ParticleTypes.MYCELIUM,
                    pos.getX() + x, pos.getY() + y, pos.getZ() + z, 0.0, 0.0, 0.0);
        }
    }
}
