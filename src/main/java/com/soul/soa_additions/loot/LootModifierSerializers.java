package com.soul.soa_additions.loot;

import com.mojang.serialization.Codec;
import com.soul.soa_additions.SoaAdditions;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class LootModifierSerializers {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SoaAdditions.MODID);

    public static final RegistryObject<Codec<AddItemLootModifier>> ADD_ITEM =
            SERIALIZERS.register("add_item", () -> AddItemLootModifier.CODEC);

    private LootModifierSerializers() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }
}
