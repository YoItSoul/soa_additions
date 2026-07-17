package com.soul.soa_additions.taiga;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class TaigaItems {

    public static final String TAIGA_ID = "taiga";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TAIGA_ID);

    private static final String[] MATERIALS = {
            "abyssum", "adamant", "astrium", "aurorium", "basalt",
            "dilithium", "duranite", "dyonite", "eezo", "fractum",
            "ignitz", "imperomite", "iox", "jauxum", "karmesine",
            "lumix", "meteorite", "nihilite", "niob", "nucleum",
            "obsidiorite", "osram", "ovium", "palladium", "prometheum",
            "proxii", "seismum", "solarium", "terrax", "tiberium",
            "triberium", "tritonite", "valyrium", "vibranium", "violium",
            "yrdeen"
    };

    static {
        for (String mat : MATERIALS) {
            ITEMS.register(mat + "_ingot",  () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
            ITEMS.register(mat + "_nugget", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
            ITEMS.register(mat + "_dust",   () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
        }
        ITEMS.register("dilithium_crystal", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
        ITEMS.register("tiberium_crystal",  () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
        // uru ingot/ore/block live in soa_additions: (kept — renaming would break
        // placed blocks in existing worlds); dust + nugget never existed until now.
        ITEMS.register("uru_dust",   () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
        ITEMS.register("uru_nugget", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    }

    private TaigaItems() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
