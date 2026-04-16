package com.soul.soa_additions.bloodarsenal.item.tool;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/**
 * Stygian Dagger — gold tier, low durability. Mostly placeholder from
 * the original mod's WIP content.
 *
 * <p>Ported from: arcaratus.bloodarsenal.item.tool.ItemStygianDagger</p>
 */
public class StygianDaggerItem extends SwordItem {

    public StygianDaggerItem(Properties props) {
        super(Tiers.GOLD, 3, -2.4f, props.durability(3));
    }
}
