package com.soul.soa_additions.bloodarsenal.item.bauble;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Vampire Ring — heals the attacker on hit proportional to damage dealt.
 * The actual healing logic is in BAEventHandler (LivingHurtEvent),
 * which checks if the attacker has this item equipped via BACuriosHelper.
 * Curios slot: ring.
 */
public class VampireRingItem extends Item implements ICurio {

    public VampireRingItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canEquipFromUse(SlotContext ctx) {
        return true;
    }
}
