package com.soul.soa_additions.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class StageItem extends Item {

    private final boolean foil;
    private final String[] tooltipLines;

    public StageItem(Properties props, boolean foil, String... tooltip) {
        super(props);
        this.foil = foil;
        this.tooltipLines = tooltip;
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
}
