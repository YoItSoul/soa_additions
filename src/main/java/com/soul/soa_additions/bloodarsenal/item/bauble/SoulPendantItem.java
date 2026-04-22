package com.soul.soa_additions.bloodarsenal.item.bauble;

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
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.will.PlayerDemonWillHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Soul Pendant — stores demon will. 5 tiers with varying capacities.
 * Absorbs demon will items on pickup (handled in BAEventHandler).
 * Right-click transfers stored will to the player's demon will pool.
 * Curios slot: necklace.
 */
public class SoulPendantItem extends Item implements ICurio {

    private static final String TAG_WILL = "demon_will";
    private static final String TAG_WILL_TYPE = "will_type";

    private final int maxWill;
    private final int tier;

    public SoulPendantItem(int tier, int maxWill) {
        super(new Item.Properties().stacksTo(1).rarity(tier >= 3 ? Rarity.RARE : Rarity.UNCOMMON));
        this.tier = tier;
        this.maxWill = maxWill;
    }

    @Override
    public net.minecraft.world.item.ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canEquipFromUse(SlotContext ctx) {
        return true;
    }

    // ── Will storage ────────────────────────────────────────────────────

    public double getStoredWill(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getDouble(TAG_WILL);
        }
        return 0;
    }

    public void setStoredWill(ItemStack stack, double will) {
        stack.getOrCreateTag().putDouble(TAG_WILL, Math.min(will, maxWill));
    }

    public double addWill(ItemStack stack, double amount) {
        double current = getStoredWill(stack);
        double toAdd = Math.min(amount, maxWill - current);
        if (toAdd > 0) {
            setStoredWill(stack, current + toAdd);
        }
        return toAdd;
    }

    public double drainWill(ItemStack stack, double amount) {
        double current = getStoredWill(stack);
        double drained = Math.min(amount, current);
        setStoredWill(stack, current - drained);
        return drained;
    }

    public EnumDemonWillType getWillType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TAG_WILL_TYPE)) {
            return EnumDemonWillType.getType(stack.getTag().getString(TAG_WILL_TYPE));
        }
        return EnumDemonWillType.DEFAULT;
    }

    public void setWillType(ItemStack stack, EnumDemonWillType type) {
        stack.getOrCreateTag().putString(TAG_WILL_TYPE, type.name.toLowerCase());
    }

    public int getMaxWill() {
        return maxWill;
    }

    public int getTier() {
        return tier;
    }

    // ── Right-click: transfer will to player ────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.pass(stack);

        EnumDemonWillType type = getWillType(stack);
        double drain = Math.min(getStoredWill(stack), maxWill / 10.0);
        if (drain > 0) {
            double added = PlayerDemonWillHandler.addDemonWill(type, player, drain);
            drainWill(stack, added);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        double will = getStoredWill(stack);
        EnumDemonWillType type = getWillType(stack);
        tooltip.add(Component.translatable("tooltip.soa_additions.soul_pendant.will",
                String.format("%.1f", will), maxWill)
                .withStyle(ChatFormatting.DARK_PURPLE));
        if (type != EnumDemonWillType.DEFAULT) {
            tooltip.add(Component.translatable("tooltip.soa_additions.soul_pendant.type",
                    type.name).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getStoredWill(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * (float) (getStoredWill(stack) / maxWill));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float ratio = (float) (getStoredWill(stack) / maxWill);
        return net.minecraft.util.Mth.hsvToRgb(Math.max(0.0F, ratio) / 3.0F, 1.0F, 1.0F);
    }
}
