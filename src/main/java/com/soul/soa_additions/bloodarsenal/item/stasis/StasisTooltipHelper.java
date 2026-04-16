package com.soul.soa_additions.bloodarsenal.item.stasis;

import com.soul.soa_additions.bloodarsenal.modifier.StasisModifiable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.item.IActivatable;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.Binding;

import java.util.List;

/**
 * Shared tooltip logic for all stasis tools.
 */
final class StasisTooltipHelper {

    private StasisTooltipHelper() {}

    static <T extends IBindable & IActivatable> void addTooltip(ItemStack stack, List<Component> tooltip, T tool) {
        Binding binding = tool.getBinding(stack);
        if (binding != null) {
            tooltip.add(Component.translatable("tooltip.soa_additions.stasis.bound_to", binding.getOwnerName())
                    .withStyle(ChatFormatting.GRAY));
        }

        boolean active = tool.getActivated(stack);
        tooltip.add(Component.translatable(active ? "tooltip.soa_additions.stasis.active" : "tooltip.soa_additions.stasis.inactive")
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));

        StasisModifiable modifiable = StasisModifiable.fromStack(stack);
        for (var pair : modifiable.getAllModifiers()) {
            int level = pair.getRight().getLevel() + 1;
            String roman = switch (level) {
                case 1 -> "I"; case 2 -> "II"; case 3 -> "III";
                case 4 -> "IV"; case 5 -> "V"; case 6 -> "VI";
                default -> String.valueOf(level);
            };
            tooltip.add(Component.literal(" - ")
                    .append(Component.translatable(pair.getLeft().getTranslationKey()))
                    .append(Component.literal(" " + roman))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
