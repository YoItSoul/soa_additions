package com.soul.soa_additions.bloodarsenal.item.bauble;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import wayoftime.bloodmagic.altar.IBloodAltar;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Sacrifice Amulet — stores LP gained when the wearer damages entities.
 * Right-click on a Blood Altar to transfer stored LP.
 * Curios slot: necklace.
 *
 * The LP accumulation logic is in BAEventHandler (LivingHurtEvent).
 */
public class SacrificeAmuletItem extends Item implements ICurio {

    private static final String TAG_LP = "stored_lp";
    private static final int MAX_LP = 10000;

    public SacrificeAmuletItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public net.minecraft.world.item.ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canEquipFromUse(SlotContext ctx) {
        return true;
    }

    // ── LP storage ──────────────────────────────────────────────────────

    public static int getStoredLP(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt(TAG_LP);
        }
        return 0;
    }

    public static void setStoredLP(ItemStack stack, int lp) {
        stack.getOrCreateTag().putInt(TAG_LP, Math.min(lp, MAX_LP));
    }

    public static int addLP(ItemStack stack, int amount) {
        int current = getStoredLP(stack);
        int toAdd = Math.min(amount, MAX_LP - current);
        setStoredLP(stack, current + toAdd);
        return toAdd;
    }

    // ── Right-click on altar ────────────────────────────────────────────

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide()) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(ctx.getClickedPos());
        if (be instanceof IBloodAltar altar) {
            ItemStack stack = ctx.getItemInHand();
            int stored = getStoredLP(stack);
            if (stored > 0) {
                altar.fillMainTank(stored);
                setStoredLP(stack, 0);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        // Cap LP
        if (stack.hasTag() && stack.getTag().getInt(TAG_LP) > MAX_LP) {
            stack.getTag().putInt(TAG_LP, MAX_LP);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int lp = getStoredLP(stack);
        tooltip.add(Component.translatable("tooltip.soa_additions.sacrifice_amulet.lp", lp, MAX_LP)
                .withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getStoredLP(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getStoredLP(stack) / MAX_LP);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xCC0000; // blood red
    }
}
