package com.soul.soa_additions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * GreedyCraft "Tablet of Enlightenment" (contenttweaker:tablet_of_enlightenment,
 * scripts/contenttweaker/items.zs) — a stage-backup item. NBT {@code stage} names
 * a GameStages stage; the first player to hold it is bound as owner
 * ({@code playerName}/{@code playerUUID}). Right-click grants the stage to the
 * owner (not consumed). Crafting leaves a copy (container item) — combined with
 * {@link TabletDupeRecipe} this reproduces GC's "put in a crafting table to
 * duplicate" behaviour exactly.
 */
public class TabletOfEnlightenmentItem extends Item {

    public TabletOfEnlightenmentItem(Properties props) {
        super(props);
    }

    @Override
    public Component getName(ItemStack stack) {
        MutableComponent name = Component.translatable(getDescriptionId(stack))
                .withStyle(ChatFormatting.AQUA);
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("stage")) {
            name = name.append(" ").append(Component.translatable(
                    "soa_additions.tablet_of_enlightenment.stage", tag.getString("stage")));
        }
        if (tag != null && tag.contains("playerName")) {
            name = name.append(" ").append(Component.translatable(
                    "soa_additions.tablet_of_enlightenment.player_name", tag.getString("playerName")));
        }
        return name;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (int i = 1; i <= 5; i++) {
            tooltip.add(Component.translatable("soa_additions.tablet_of_enlightenment.tooltip." + i));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) return;
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("playerName") || !tag.contains("playerUUID")) {
            tag.putString("playerName", player.getGameProfile().getName());
            tag.putUUID("playerUUID", player.getUUID());
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("stage")) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (tag.contains("playerUUID") && !player.getUUID().equals(tag.getUUID("playerUUID"))) {
            player.sendSystemMessage(Component.translatable(
                    "soa_additions.tablet_of_enlightenment.not_owner").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stack);
        }
        String stage = tag.getString("stage");
        StageItem.addStage(player, stage);
        player.sendSystemMessage(Component.translatable(
                        "soa_additions.tablet_of_enlightenment.unlock").withStyle(ChatFormatting.YELLOW)
                .append(" ").append(Component.literal(stage).withStyle(ChatFormatting.GOLD)));
        // GC did not consume the tablet.
        return InteractionResultHolder.success(stack);
    }

    /** Crafting leaves the tablet behind (GC itemGetContainerItem). */
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copy();
    }
}
