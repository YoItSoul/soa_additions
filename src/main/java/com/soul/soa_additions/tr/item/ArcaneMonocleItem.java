package com.soul.soa_additions.tr.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Arcane Monocle. Grants two functions while held in inventory or worn in a
 * curios slot:
 * <ul>
 *   <li>Hold ALT while hovering an item in any GUI → reveal/scan that item.</li>
 *   <li>Hold ALT outside a GUI → raycast 4 blocks for a block, mob, or
 *       container; reveal/scan whatever is hit (containers also drill into
 *       their inventory).</li>
 * </ul>
 *
 * <p>The functional behaviour lives in
 * {@link com.soul.soa_additions.tr.item.MonocleClientHandler} (key + raytrace)
 * and {@link com.soul.soa_additions.tr.network.MonocleScanRequestPacket}
 * (server-side scan + container drill + sound). The item itself is mostly a
 * marker — its presence in the player's inventory is what activates everything.
 */
public class ArcaneMonocleItem extends Item {

    public ArcaneMonocleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.tr.arcane_monocle.line1")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.tr.arcane_monocle.line2")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
