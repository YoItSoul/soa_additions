package com.soul.soa_additions.bloodarsenal.item.sigil;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import wayoftime.bloodmagic.common.item.sigil.ItemSigilBase;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;
import wayoftime.bloodmagic.util.helper.PlayerHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Ender Sigil — sneak+right-click to set a teleport destination (distance-based LP cost),
 * right-click to open ender chest GUI (LP cost).
 * After setting destination, a delay countdown starts; when it reaches 0 the player teleports.
 *
 * <p>Ported from: arcaratus.bloodarsenal.item.sigil.ItemSigilEnder</p>
 */
public class EnderSigilItem extends ItemSigilBase {

    private static final String TAG_DEST_X = "ender_dest_x";
    private static final String TAG_DEST_Y = "ender_dest_y";
    private static final String TAG_DEST_Z = "ender_dest_z";
    private static final String TAG_DELAY = "ender_delay";

    private static final int INITIAL_DELAY = 40;
    private static final int TELEPORT_COOLDOWN = 100;

    private static final Component CONTAINER_TITLE =
            Component.translatable("container.enderchest");

    public EnderSigilItem() {
        super("ender", BAConfig.SIGIL_ENDER_COST.get());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;

        // Decrement delay
        int delay = getDelay(stack);
        if (delay > 0) {
            setDelay(stack, delay - 1);
            return;
        }

        // Check for cached destination
        if (!stack.hasTag() || !stack.getTag().contains(TAG_DEST_X)) return;

        var tag = stack.getTag();
        int destX = tag.getInt(TAG_DEST_X);
        int destY = tag.getInt(TAG_DEST_Y);
        int destZ = tag.getInt(TAG_DEST_Z);

        // Teleport the player
        level.playSound(null, player.xo, player.yo, player.zo,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        player.fallDistance = 0;
        player.teleportTo(destX + 0.5, destY + 0.5, destZ + 0.5);
        player.getCooldowns().addCooldown(this, TELEPORT_COOLDOWN);

        // Clear cached destination
        tag.remove(TAG_DEST_X);
        tag.remove(TAG_DEST_Y);
        tag.remove(TAG_DEST_Z);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isUnusable(stack) || PlayerHelper.isFakePlayer(player)) {
            return InteractionResultHolder.pass(stack);
        }

        if (getDelay(stack) > 0) {
            return super.use(level, player, hand);
        }

        if (player.isShiftKeyDown()) {
            // Set teleport destination via ray trace
            BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = hit.getBlockPos().relative(hit.getDirection());
                double distance = player.blockPosition().distSqr(blockPos);
                distance = Math.sqrt(distance);

                // LP cost scales with distance
                int lpCost = (int) (distance * BAConfig.SIGIL_ENDER_COST.get());
                if (NetworkHelper.getSoulNetwork(player).syphonAndDamage(player,
                        SoulTicket.item(stack, level, player, lpCost)).isSuccess()
                        || player.getAbilities().instabuild) {
                    var tag = stack.getOrCreateTag();
                    tag.putInt(TAG_DEST_X, blockPos.getX());
                    tag.putInt(TAG_DEST_Y, blockPos.getY());
                    tag.putInt(TAG_DEST_Z, blockPos.getZ());
                    setDelay(stack, INITIAL_DELAY);
                    player.getCooldowns().addCooldown(this, 1000);
                }
            }
        } else {
            // Normal right-click: open ender chest
            if (player instanceof ServerPlayer sp) {
                PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
                sp.openMenu(new SimpleMenuProvider(
                        (windowId, inventory, p) -> ChestMenu.threeRows(windowId, inventory, enderChest),
                        CONTAINER_TITLE));
            }
            if (!level.isClientSide()) {
                NetworkHelper.getSoulNetwork(player).syphonAndDamage(player,
                        SoulTicket.item(stack, level, player, getLpUsed()));
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (stack.hasTag() && stack.getTag().contains(TAG_DEST_X)) {
            var tag = stack.getTag();
            tooltip.add(Component.translatable("tooltip.soa_additions.sigil_ender.dest",
                    tag.getInt(TAG_DEST_X),
                    tag.getInt(TAG_DEST_Y),
                    tag.getInt(TAG_DEST_Z)).withStyle(ChatFormatting.GRAY));
        }
    }

    private int getDelay(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(TAG_DELAY) : 0;
    }

    private void setDelay(ItemStack stack, int delay) {
        stack.getOrCreateTag().putInt(TAG_DELAY, delay);
    }
}
