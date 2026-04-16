package com.soul.soa_additions.bloodarsenal.item;

import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import com.soul.soa_additions.bloodarsenal.modifier.ModifierHelper;
import com.soul.soa_additions.bloodarsenal.modifier.ModifierRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Modifier Tome — stores a modifier key and level in NBT.
 * Right-clicking on a Stasis Plate containing a Stasis Tool applies
 * the modifier to that tool via the Sanguine Infusion system.
 *
 * <p>Each registered modifier gets its own creative-tab entry (a tome
 * ItemStack with pre-set NBT).</p>
 *
 * <p>Ported from: arcaratus.bloodarsenal.item.ItemModifierTome</p>
 */
public class ModifierTomeItem extends Item {

    public ModifierTomeItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        String key = ModifierHelper.getKey(stack);
        if (key != null && !key.isEmpty()) {
            int modLevel = ModifierHelper.getLevel(stack);
            tooltip.add(Component.translatable("tooltip.soa_additions.modifier_tome.modifier",
                            Component.translatable("modifier.soa_additions." + key),
                            modLevel + 1)
                    .withStyle(ChatFormatting.GOLD));
            if (ModifierHelper.isReadyToUpgrade(stack)) {
                tooltip.add(Component.translatable("tooltip.soa_additions.modifier_tome.ready")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ModifierHelper.isReadyToUpgrade(stack);
    }

    /**
     * Creates a tome ItemStack for the given modifier at level 0.
     */
    public static ItemStack createTome(ItemStack base, String modifierKey, int level, boolean readyToUpgrade) {
        ItemStack stack = base.copy();
        ModifierHelper.setKey(stack, modifierKey);
        ModifierHelper.setLevel(stack, level);
        ModifierHelper.setReadyToUpgrade(stack, readyToUpgrade);
        return stack;
    }
}
