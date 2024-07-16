package com.soul.soa_additions.item;

import com.soul.soa_additions.block.custom.ModBlocks;
import com.soul.soa_additions.soa_additions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, soa_additions.MODID);

    //Block items
    public static final RegistryObject<Item> ABYSSAL_ORE_BLOCK_ITEM = ITEMS.register("abyssal_ore_block", () -> new BlockItem(ModBlocks.ABYSSAL_ORE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> ETHER_ORE_BLOCK_ITEM = ITEMS.register("ether_ore_block", () -> new BlockItem(ModBlocks.ETHER_ORE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> INFERNIUM_ORE_BLOCK_ITEM = ITEMS.register("infernium_ore_block", () -> new BlockItem(ModBlocks.INFERNIUM_ORE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> VOID_ORE_BLOCK_ITEM = ITEMS.register("void_ore_block", () -> new BlockItem(ModBlocks.VOID_ORE_BLOCK.get(), new Item.Properties()));


    //Items
    public static final RegistryObject<Item> ABYSSAL_INGOT = ITEMS.register("abyssal_ingot", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ETHER_INGOT = ITEMS.register("ether_ingot", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> INFERNIUM_INGOT = ITEMS.register("infernium_ingot", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> VOID_INGOT = ITEMS.register("void_ingot", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));


public static void register(IEventBus eventBus) {
    ITEMS.register(eventBus);   }
}
