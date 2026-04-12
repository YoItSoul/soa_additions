package com.soul.soa_additions.item;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.item.QuestBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SoaAdditions.MODID);

    public static final RegistryObject<Item> ABYSSAL_INGOT   = registerRareIngot("abyssal_ingot");
    public static final RegistryObject<Item> ETHER_INGOT     = registerRareIngot("ether_ingot");
    public static final RegistryObject<Item> INFERNIUM_INGOT = registerRareIngot("infernium_ingot");
    public static final RegistryObject<Item> VOID_INGOT      = registerRareIngot("void_ingot");

    public static final RegistryObject<Item> CHEATER_COIN = ITEMS.register("cheater_coin",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));

    public static final RegistryObject<Item> QUEST_BOOK = ITEMS.register("quest_book", QuestBookItem::new);

    public static final RegistryObject<Item> DONOR_TOKEN = ITEMS.register("donor_token",
            com.soul.soa_additions.donor.DonorTokenItem::new);

    private ModItems() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static RegistryObject<Item> registerRareIngot(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    }
}
