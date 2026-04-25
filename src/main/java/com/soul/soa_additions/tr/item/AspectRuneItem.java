package com.soul.soa_additions.tr.item;

import com.soul.soa_additions.tr.core.Aspect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Physical token for one aspect — the player-held crafting/research currency.
 * Ties one Item instance to one {@link Aspect}, so creative tab, recipes, and
 * JEI all see them as distinct registrations. The aspect is exposed for the
 * eventual hex-puzzle marble placement, infusion recipes, and Grimoire focus
 * page binding.
 *
 * <p>Tooltip surfaces tier and parent lineage so a player who opens JEI can
 * see "Auram = Praecantatio + Aer" without leaving the inventory screen.
 */
public final class AspectRuneItem extends Item {

    private final Aspect aspect;

    public AspectRuneItem(Aspect aspect, Properties props) {
        super(props);
        this.aspect = aspect;
    }

    public Aspect aspect() { return aspect; }

    @Override
    public Component getName(ItemStack stack) {
        // "Aer Rune", "Auram Rune", etc. — the aspect's own translation key
        // already resolves to the localized aspect name; we suffix " Rune".
        return Component.translatable("item.tr.aspect_rune.format", aspect.displayName())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(aspect.color())));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tr.aspect_rune.tier", aspect.tier())
                .withStyle(ChatFormatting.GRAY));
        if (!aspect.isPrimal()) {
            MutableComponent line = Component.translatable("tooltip.tr.aspect_rune.parents",
                            aspect.parentA().displayName(),
                            aspect.parentB().displayName())
                    .withStyle(ChatFormatting.DARK_GRAY);
            tooltip.add(line);
        } else {
            tooltip.add(Component.translatable("tooltip.tr.aspect_rune.primal")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
