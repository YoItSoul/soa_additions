package com.soul.soa_additions.bloodarsenal.item.sigil;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.common.item.sigil.ItemSigilBase;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Augmented Holding Sigil — holds up to 9 sigil items internally.
 * Delegates right-click to the currently selected slot's sigil.
 * Ticks all contained sigils every update.
 * Sneak+right-click cycles through non-empty slots.
 */
public class AugmentedHoldingSigilItem extends ItemSigilBase {

    private static final int MAX_SLOTS = 9;
    private static final String TAG_INVENTORY = "holding_inventory";
    private static final String TAG_SELECTED = "holding_selected";

    public AugmentedHoldingSigilItem() {
        super("augmented_holding", 0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isUnusable(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.isShiftKeyDown()) {
            // Cycle to next non-empty slot
            if (!level.isClientSide()) {
                cycleSlot(stack);
                int selected = getSelectedSlot(stack);
                ItemStack held = getSlotStack(stack, selected);
                if (!held.isEmpty()) {
                    player.displayClientMessage(
                            Component.translatable("tooltip.soa_additions.sigil_holding.selected",
                                    held.getHoverName()), true);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // Delegate to the selected sigil
        ItemStack held = getSlotStack(stack, getSelectedSlot(stack));
        if (!held.isEmpty() && held.getItem() instanceof ItemSigilBase sigil) {
            return sigil.use(level, player, hand);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide() || !(entity instanceof Player player)) return;

        // Tick all contained sigils every tick
        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemStack contained = getSlotStack(stack, i);
            if (!contained.isEmpty()) {
                contained.getItem().inventoryTick(contained, level, player, slot, false);
            }
        }
    }

    // ── Inventory helpers ───────────────────────────────────────────────

    public int getSelectedSlot(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt(TAG_SELECTED);
        }
        return 0;
    }

    private void cycleSlot(ItemStack stack) {
        int current = getSelectedSlot(stack);
        int next = current;
        for (int i = 1; i <= MAX_SLOTS; i++) {
            int check = (current + i) % MAX_SLOTS;
            if (!getSlotStack(stack, check).isEmpty()) {
                next = check;
                break;
            }
        }
        stack.getOrCreateTag().putInt(TAG_SELECTED, next);
    }

    public ItemStack getSlotStack(ItemStack holdingStack, int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return ItemStack.EMPTY;
        if (!holdingStack.hasTag()) return ItemStack.EMPTY;

        ListTag list = holdingStack.getTag().getList(TAG_INVENTORY, Tag.TAG_COMPOUND);
        if (slot >= list.size()) return ItemStack.EMPTY;

        CompoundTag itemTag = list.getCompound(slot);
        if (itemTag.isEmpty()) return ItemStack.EMPTY;
        return ItemStack.of(itemTag);
    }

    public void setSlotStack(ItemStack holdingStack, int slot, ItemStack toSet) {
        if (slot < 0 || slot >= MAX_SLOTS) return;

        CompoundTag root = holdingStack.getOrCreateTag();
        ListTag list = root.getList(TAG_INVENTORY, Tag.TAG_COMPOUND);

        // Pad list to required size
        while (list.size() <= slot) {
            list.add(new CompoundTag());
        }

        if (toSet.isEmpty()) {
            list.set(slot, new CompoundTag());
        } else {
            list.set(slot, toSet.save(new CompoundTag()));
        }

        root.put(TAG_INVENTORY, list);
    }

    /**
     * Finds the first empty slot index, or -1 if full.
     */
    public int getFirstEmptySlot(ItemStack holdingStack) {
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (getSlotStack(holdingStack, i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        int selected = getSelectedSlot(stack);
        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemStack contained = getSlotStack(stack, i);
            if (!contained.isEmpty()) {
                Component name = contained.getHoverName();
                if (i == selected) {
                    tooltip.add(Component.literal("> ").withStyle(ChatFormatting.GREEN)
                            .append(name.copy().withStyle(ChatFormatting.GREEN)));
                } else {
                    tooltip.add(Component.literal("  ").append(name.copy().withStyle(ChatFormatting.GRAY)));
                }
            }
        }
    }
}
