package com.soul.soa_additions.donor;

import net.minecraft.ChatFormatting;
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

/**
 * The Orb of Avarice — a cosmetic donor item for the Curios "donor" slot.
 * Gives no gameplay advantages — it's a badge of honor with a
 * shimmering enchant glint and custom tooltip. When equipped, it spawns
 * a glowing orb pet that orbits the player.
 *
 * <p>Only donors can use/equip this item. Non-donors who try to use it
 * get a message explaining it's donor-exclusive.</p>
 */
public class DonorTokenItem extends Item {

    public DonorTokenItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // always show enchantment glint
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            if (!DonorRegistry.isDonor(player.getUUID())) {
                player.displayClientMessage(
                        Component.literal("This item is exclusive to donors!")
                                .withStyle(ChatFormatting.RED),
                        true);
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("A shimmering orb pulsing with gratitude.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("Equip in your Donor slot to summon a glowing companion.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("\u2764 Donor Exclusive")
                .withStyle(ChatFormatting.GOLD));
    }
}
