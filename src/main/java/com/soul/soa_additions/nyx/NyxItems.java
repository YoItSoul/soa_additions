package com.soul.soa_additions.nyx;

import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.nyx.item.LunarWaterBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.RegistryObject;

/** Central reference for Nyx-specific items. Registers onto the shared ITEMS
 *  DeferredRegister so creative tabs / recipes find them automatically. */
public final class NyxItems {

    public static final RegistryObject<Item> LUNAR_WATER_BOTTLE = ModItems.ITEMS.register(
            "lunar_water_bottle",
            () -> new LunarWaterBottleItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    private NyxItems() {}

    /** Touch the class so its static initializers run. Called during mod construction
     *  before DeferredRegisters fire. */
    public static void bootstrap() {}
}
