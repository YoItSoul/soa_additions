package com.soul.soa_additions.loot;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class LootConditions {

    public static final DeferredRegister<LootItemConditionType> TYPES =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, SoaAdditions.MODID);

    public static final RegistryObject<LootItemConditionType> HAS_STAGE =
            TYPES.register("has_stage", () -> new LootItemConditionType(new HasStageCondition.Serializer()));

    private LootConditions() {}

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
    }
}
