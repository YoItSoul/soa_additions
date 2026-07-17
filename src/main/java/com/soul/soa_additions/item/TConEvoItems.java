package com.soul.soa_additions.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TConEvoItems {

    public static final String TCONEVO_ID = "tconevo";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TCONEVO_ID);

    private static RegistryObject<Item> rare(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    }

    public static final RegistryObject<Item> COALESCENCE_MATRIX = rare("coalescence_matrix");

    // ========== TConEvo metal ingots (9 metals) ==========
    public static final RegistryObject<Item> BOUND_METAL_INGOT     = rare("bound_metal_ingot");
    public static final RegistryObject<Item> CHAOTIC_INGOT         = rare("chaotic_ingot");
    public static final RegistryObject<Item> DRACONIC_METAL_INGOT  = rare("draconic_metal_ingot");
    public static final RegistryObject<Item> ENERGETIC_METAL_INGOT = rare("energetic_metal_ingot");
    public static final RegistryObject<Item> ESSENCE_METAL_INGOT   = rare("essence_metal_ingot");
    public static final RegistryObject<Item> PRIMAL_METAL_INGOT    = rare("primal_metal_ingot");
    public static final RegistryObject<Item> SENTIENT_METAL_INGOT  = rare("sentient_metal_ingot");
    public static final RegistryObject<Item> UNIVERSAL_METAL_INGOT = rare("universal_metal_ingot");
    public static final RegistryObject<Item> WYVERN_INGOT          = rare("wyvern_ingot");

    // ========== TConEvo metal nuggets (9 metals) ==========
    public static final RegistryObject<Item> BOUND_METAL_NUGGET     = rare("bound_metal_nugget");
    public static final RegistryObject<Item> CHAOTIC_NUGGET         = rare("chaotic_nugget");
    public static final RegistryObject<Item> DRACONIC_METAL_NUGGET  = rare("draconic_metal_nugget");
    public static final RegistryObject<Item> ENERGETIC_METAL_NUGGET = rare("energetic_metal_nugget");
    public static final RegistryObject<Item> ESSENCE_METAL_NUGGET   = rare("essence_metal_nugget");
    public static final RegistryObject<Item> PRIMAL_METAL_NUGGET    = rare("primal_metal_nugget");
    public static final RegistryObject<Item> SENTIENT_METAL_NUGGET  = rare("sentient_metal_nugget");
    public static final RegistryObject<Item> UNIVERSAL_METAL_NUGGET = rare("universal_metal_nugget");
    public static final RegistryObject<Item> WYVERN_NUGGET          = rare("wyvern_nugget");

    // ========== TConEvo metal dusts (9 metals) ==========
    public static final RegistryObject<Item> BOUND_METAL_DUST     = rare("bound_metal_dust");
    public static final RegistryObject<Item> CHAOTIC_DUST         = rare("chaotic_dust");
    public static final RegistryObject<Item> DRACONIC_METAL_DUST  = rare("draconic_metal_dust");
    public static final RegistryObject<Item> ENERGETIC_METAL_DUST = rare("energetic_metal_dust");
    public static final RegistryObject<Item> ESSENCE_METAL_DUST   = rare("essence_metal_dust");
    public static final RegistryObject<Item> PRIMAL_METAL_DUST    = rare("primal_metal_dust");
    public static final RegistryObject<Item> SENTIENT_METAL_DUST  = rare("sentient_metal_dust");
    public static final RegistryObject<Item> UNIVERSAL_METAL_DUST = rare("universal_metal_dust");
    public static final RegistryObject<Item> WYVERN_DUST          = rare("wyvern_dust");

    // ========== TConEvo metal plates (9 metals) ==========
    public static final RegistryObject<Item> BOUND_METAL_PLATE     = rare("bound_metal_plate");
    public static final RegistryObject<Item> CHAOTIC_PLATE         = rare("chaotic_plate");
    public static final RegistryObject<Item> DRACONIC_METAL_PLATE  = rare("draconic_metal_plate");
    public static final RegistryObject<Item> ENERGETIC_METAL_PLATE = rare("energetic_metal_plate");
    public static final RegistryObject<Item> ESSENCE_METAL_PLATE   = rare("essence_metal_plate");
    public static final RegistryObject<Item> PRIMAL_METAL_PLATE    = rare("primal_metal_plate");
    public static final RegistryObject<Item> SENTIENT_METAL_PLATE  = rare("sentient_metal_plate");
    public static final RegistryObject<Item> UNIVERSAL_METAL_PLATE = rare("universal_metal_plate");
    public static final RegistryObject<Item> WYVERN_PLATE          = rare("wyvern_plate");

    // ========== TConEvo metal gears (9 metals) ==========
    public static final RegistryObject<Item> BOUND_METAL_GEAR     = rare("bound_metal_gear");
    public static final RegistryObject<Item> CHAOTIC_GEAR         = rare("chaotic_gear");
    public static final RegistryObject<Item> DRACONIC_METAL_GEAR  = rare("draconic_metal_gear");
    public static final RegistryObject<Item> ENERGETIC_METAL_GEAR = rare("energetic_metal_gear");
    public static final RegistryObject<Item> ESSENCE_METAL_GEAR   = rare("essence_metal_gear");
    public static final RegistryObject<Item> PRIMAL_METAL_GEAR    = rare("primal_metal_gear");
    public static final RegistryObject<Item> SENTIENT_METAL_GEAR  = rare("sentient_metal_gear");
    public static final RegistryObject<Item> UNIVERSAL_METAL_GEAR = rare("universal_metal_gear");
    public static final RegistryObject<Item> WYVERN_GEAR          = rare("wyvern_gear");

    public static final RegistryObject<Item> ARTIFACT_UNSEALER = rare("artifact_unsealer");
    public static final RegistryObject<Item> PINK_METAL_INGOT  = rare("pink_metal_ingot");

    // 1.12 tconevo:edible0/edible1 — raw 3 nutrition / 0.6667 sat, cooked steak-tier
    public static final RegistryObject<Item> RAW_MEAT_INGOT = ITEMS.register("raw_meat_ingot",
            () -> new Item(new Item.Properties().food(
                    new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(3).saturationMod(0.6667f).meat().build())));
    public static final RegistryObject<Item> COOKED_MEAT_INGOT = ITEMS.register("cooked_meat_ingot",
            () -> new Item(new Item.Properties().food(
                    new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(8).saturationMod(0.8f).meat().build())));

    private TConEvoItems() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
