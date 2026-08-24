package com.soul.soa_additions.bloodarsenal.item.bauble;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Vampire Ring — heals the attacker on hit proportional to damage dealt.
 * The actual healing logic is in BAEventHandler (LivingHurtEvent),
 * which checks if the attacker has this item equipped via BACuriosHelper.
 * Curios slot: ring.
 */
public class VampireRingItem extends Item {

    public VampireRingItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }
}
