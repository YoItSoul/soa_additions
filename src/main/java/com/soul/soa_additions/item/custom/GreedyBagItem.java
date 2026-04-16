package com.soul.soa_additions.item.custom;

import net.darkhax.itemstages.Restriction;
import net.darkhax.itemstages.RestrictionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A bag that automatically absorbs stage-restricted items from the player's
 * inventory and releases them when the player gains the required stages.
 * Works from the regular inventory or a Curios slot.
 */
public class GreedyBagItem extends Item implements ICurio {

    private static final String TAG_STORED = "StoredItems";

    public GreedyBagItem(Properties props) {
        super(props.stacksTo(1));
    }

    // ── NBT helpers ──────────────────────────────────────────────────

    private static ListTag getStoredList(ItemStack bag) {
        return bag.getOrCreateTag().getList(TAG_STORED, Tag.TAG_COMPOUND);
    }

    private static void setStoredList(ItemStack bag, ListTag list) {
        bag.getOrCreateTag().put(TAG_STORED, list);
    }

    // ── Core logic ───────────────────────────────────────────────────

    /** Store an item inside this bag's NBT. */
    public void storeItem(ItemStack bag, ItemStack toStore) {
        ListTag list = getStoredList(bag);
        CompoundTag entry = new CompoundTag();
        toStore.save(entry);
        list.add(entry);
        setStoredList(bag, list);
    }

    /**
     * Attempt to release any stored items whose stage restrictions the
     * player now satisfies.  Items are added directly to the player's
     * inventory; if it's full, remaining items stay in the bag.
     */
    public void releaseUnlockedItems(ServerPlayer player, ItemStack bag) {
        ListTag list = getStoredList(bag);
        if (list.isEmpty()) return;

        List<Integer> toRemove = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            ItemStack stored = ItemStack.of(list.getCompound(i));
            if (stored.isEmpty()) {
                toRemove.add(i);
                continue;
            }

            Restriction restriction = RestrictionManager.INSTANCE
                    .getRestriction(player, stored);
            if (restriction != null && restriction.isRestricted(stored)) {
                continue; // still locked
            }

            if (player.getInventory().add(stored)) {
                toRemove.add(i);
            } else {
                break; // inventory full
            }
        }

        // Remove in reverse so indices stay valid
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            list.remove((int) toRemove.get(i));
        }
        setStoredList(bag, list);
    }

    /**
     * Scan the player's inventory for restricted items and absorb them
     * into this bag. Walks main + armor + offhand to match ItemStages'
     * own tick-scan range — anything we skip would be dropped on the
     * ground by ItemStages on the next tick.
     */
    public void absorbRestrictedItems(ServerPlayer player, ItemStack bag) {
        Inventory inv = player.getInventory();
        int total = inv.getContainerSize();
        for (int i = 0; i < total; i++) {
            ItemStack slot = inv.getItem(i);
            if (slot.isEmpty() || slot.getItem() instanceof GreedyBagItem) continue;

            Restriction restriction = RestrictionManager.INSTANCE
                    .getRestriction(player, slot);
            if (restriction != null && restriction.isRestricted(slot)) {
                storeItem(bag, slot.copy());
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    // ── Right-click: release items ───────────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                   InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (player.isShiftKeyDown()) {
                releaseUnlockedItems(sp, bag);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        return InteractionResultHolder.sidedSuccess(bag, level.isClientSide());
    }

    // ── Tooltip ──────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack bag, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        if (level != null && level.isClientSide()) {
            // Delegate to an inner class so that client-only class references
            // (Minecraft, Screen) are never verified by the JVM on a dedicated
            // server — the inner class is only loaded when this branch runs.
            ClientTooltip.build(bag, tooltip);
        } else {
            ListTag list = getStoredList(bag);
            tooltip.add(Component.literal(list.size() + " items stored")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    /** Isolated in its own class so the JVM never loads Minecraft / Screen
     *  bytecode on a dedicated server. */
    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    private static final class ClientTooltip {
        static void build(ItemStack bag, List<Component> tooltip) {
            Player player = net.minecraft.client.Minecraft.getInstance().player;
            if (player == null) return;

            ListTag list = getStoredList(bag);
            int total = list.size();
            int ready = 0;

            for (int i = 0; i < total; i++) {
                ItemStack stored = ItemStack.of(list.getCompound(i));
                Restriction restriction = RestrictionManager.INSTANCE
                        .getRestriction(player, stored);
                if (restriction == null || !restriction.isRestricted(stored)) {
                    ready++;
                }
            }

            tooltip.add(Component.literal(ready + " items ready to collect")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal(total + " items stored")
                    .withStyle(ChatFormatting.YELLOW));

            if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
                tooltip.add(Component.literal(
                        "Stores items you can't use yet due to stage restrictions.")
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal(
                        "Shift-right-click to collect unlocked items.")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.literal("Hold SHIFT for info")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    // ── ICurio ───────────────────────────────────────────────────────

    @Override
    public ItemStack getStack() {
        return new ItemStack(this);
    }
}
