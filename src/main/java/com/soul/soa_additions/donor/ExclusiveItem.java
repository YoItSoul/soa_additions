package com.soul.soa_additions.donor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Base class for items that are exclusive to a specific player. The owner
 * UUID is stored in the item's NBT tag. Only the designated owner (or any
 * donor, if {@link #donorOnly} is true) can use/equip the item.
 *
 * <p>Use {@link #bindToPlayer(ItemStack, UUID, String)} to assign ownership
 * when the item is created or given as a reward.</p>
 */
public class ExclusiveItem extends Item {

    private final boolean donorOnly;

    public ExclusiveItem(boolean donorOnly) {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant());
        this.donorOnly = donorOnly;
    }

    /** Bind this item stack to a specific player. */
    public static void bindToPlayer(ItemStack stack, UUID playerUuid, String playerName) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID("BoundTo", playerUuid);
        tag.putString("BoundName", playerName);
    }

    /** Get the UUID this stack is bound to, or null if unbound. */
    @Nullable
    public static UUID getBoundPlayer(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.hasUUID("BoundTo")) return tag.getUUID("BoundTo");
        return null;
    }

    /** Check if a player is allowed to use this item. */
    public boolean canPlayerUse(ItemStack stack, Player player) {
        UUID bound = getBoundPlayer(stack);
        if (bound != null && !bound.equals(player.getUUID())) return false;
        if (donorOnly && !DonorRegistry.isDonor(player.getUUID())) return false;
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && !canPlayerUse(stack, player)) {
            UUID bound = getBoundPlayer(stack);
            if (bound != null && !bound.equals(player.getUUID())) {
                CompoundTag tag = stack.getTag();
                String name = tag != null && tag.contains("BoundName") ? tag.getString("BoundName") : "another player";
                player.displayClientMessage(
                        Component.literal("This item belongs to " + name + "!")
                                .withStyle(ChatFormatting.RED),
                        true);
            } else if (donorOnly) {
                player.displayClientMessage(
                        Component.literal("This item is exclusive to donors!")
                                .withStyle(ChatFormatting.RED),
                        true);
            }
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        UUID bound = getBoundPlayer(stack);
        if (bound != null) {
            CompoundTag tag = stack.getTag();
            String name = tag != null && tag.contains("BoundName") ? tag.getString("BoundName") : bound.toString();
            tooltip.add(Component.literal("\u2764 Bound to " + name)
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        }
        if (donorOnly) {
            tooltip.add(Component.literal("\u2B50 Donor Exclusive")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
