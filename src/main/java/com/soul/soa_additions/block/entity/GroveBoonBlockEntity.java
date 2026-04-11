package com.soul.soa_additions.block.entity;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.block.custom.GroveBoonBlock;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

/**
 * Behaviour of the Grove Boon block:
 * <ul>
 *     <li>Players who right-click with an emerald receive Resistance + Regeneration I for 10 minutes.</li>
 *     <li>The block flips to its ON state for 10 minutes as a per-shrine cooldown.</li>
 *     <li>Players who stand nearby while the shrine is in any state silently earn the
 *         {@code soa_additions:visit_grove} advancement.</li>
 * </ul>
 */
public class GroveBoonBlockEntity extends BlockEntity {

    private static final int COOLDOWN_TICKS = 12_000;
    private static final int EFFECT_DURATION_TICKS = 12_000;
    private static final double NEARBY_RADIUS = 8.0D;
    private static final ResourceLocation VISIT_GROVE_ADVANCEMENT =
            new ResourceLocation(SoaAdditions.MODID, "visit_grove");

    private static final List<String> BLESSINGS = List.of(
            "Your generosity is appreciated. May you be strengthened in your journey.",
            "A sense of warmth and gratitude fills the air around you.",
            "You feel a renewed sense of purpose as the grove accepts your offering.",
            "A peaceful energy surrounds you, filling you with confidence.",
            "Your offering has been received with gratitude."
    );

    private final Random random = new Random();
    private long activationTime = -1L;

    public GroveBoonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GROVE_BOON_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GroveBoonBlockEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (state.getValue(GroveBoonBlock.ON)
                && entity.activationTime != -1L
                && level.getGameTime() - entity.activationTime >= COOLDOWN_TICKS) {
            entity.turnOff(serverLevel, pos);
            return;
        }

        if (level.getGameTime() % 60L != 0L) return;
        AABB area = new AABB(pos).inflate(NEARBY_RADIUS);
        for (Player player : level.getEntitiesOfClass(Player.class, area)) {
            if (player instanceof ServerPlayer serverPlayer) {
                MinecraftServer server = serverPlayer.getServer();
                if (server == null) continue;
                Advancement advancement = server.getAdvancements().getAdvancement(VISIT_GROVE_ADVANCEMENT);
                if (advancement != null && !serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone()) {
                    serverPlayer.getAdvancements().award(advancement, "near_boon_block");
                }
            }
        }
    }

    public void interact(Player player) {
        if (this.level == null) return;
        BlockState state = this.getBlockState();
        if (state.getValue(GroveBoonBlock.ON)) return;

        ItemStack held = player.getMainHandItem();
        if (held.getItem() != Items.EMERALD) return;

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_DURATION_TICKS, 1));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION_TICKS, 0));

        this.level.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        sendBlessingMessage(player);

        if (this.level instanceof ServerLevel serverLevel) {
            turnOn(serverLevel, this.worldPosition);
        }
    }

    private void sendBlessingMessage(Player player) {
        String message = BLESSINGS.get(this.random.nextInt(BLESSINGS.size()));
        player.sendSystemMessage(Component.literal("§6[Grove]§r " + message));
    }

    private void turnOn(ServerLevel level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (current.getValue(GroveBoonBlock.ON)) return;
        level.setBlock(pos, current.setValue(GroveBoonBlock.ON, true), Block.UPDATE_ALL);
        this.activationTime = level.getGameTime();
        level.scheduleTick(pos, this.getBlockState().getBlock(), COOLDOWN_TICKS);
    }

    private void turnOff(ServerLevel level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (!current.getValue(GroveBoonBlock.ON)) return;
        level.setBlock(pos, current.setValue(GroveBoonBlock.ON, false), Block.UPDATE_ALL);
        this.activationTime = -1L;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("ActivationTime", this.activationTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.activationTime = tag.getLong("ActivationTime");
        if (this.activationTime == -1L) return;
        if (this.level instanceof ServerLevel serverLevel) {
            long elapsed = serverLevel.getGameTime() - this.activationTime;
            if (elapsed >= COOLDOWN_TICKS) {
                turnOff(serverLevel, this.worldPosition);
            } else {
                serverLevel.scheduleTick(this.worldPosition, this.getBlockState().getBlock(), (int) (COOLDOWN_TICKS - elapsed));
            }
        }
    }
}
