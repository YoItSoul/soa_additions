package com.soul.soa_additions.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Item with a configurable right-click action. Replaces the bespoke GreedyCraft
 * "Additions" mod effect JSONs (type {@code additions:item_right_click}) with a
 * Java callback. The action fires server-side; the client gets a predicted
 * swing so the animation plays without waiting for the round-trip.
 *
 * <p>The callback returns whether the action succeeded; on success the item is
 * shrunk by one unless {@code consumeOnUse} was disabled at construction.</p>
 */
public class UseActionItem extends Item {

    private final boolean foil;
    private final String[] tooltipLines;
    private final UseAction action;
    private final boolean consumeOnUse;

    public UseActionItem(Properties props, boolean foil, boolean consumeOnUse,
                         UseAction action, String... tooltip) {
        super(props);
        this.foil = foil;
        this.tooltipLines = tooltip;
        this.action = action;
        this.consumeOnUse = consumeOnUse;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return foil || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        for (String line : tooltipLines) {
            list.add(Component.literal(line));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // Client predicts the swing; the authoritative branch is the server.
        if (level.isClientSide) {
            return InteractionResultHolder.success(held);
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(held);
        }

        boolean ok = action.apply(serverLevel, serverPlayer, held);
        if (!ok) return InteractionResultHolder.pass(held);

        if (consumeOnUse && !player.getAbilities().instabuild) {
            held.shrink(1);
        }
        return InteractionResultHolder.consume(held);
    }

    @FunctionalInterface
    public interface UseAction {
        /** Perform the action. Return true if it succeeded (and the item should
         *  consume). Implementations run server-side only. */
        boolean apply(ServerLevel level, ServerPlayer player, ItemStack stack);
    }
}
