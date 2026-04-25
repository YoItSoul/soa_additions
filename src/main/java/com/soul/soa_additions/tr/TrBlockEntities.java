package com.soul.soa_additions.tr;

import com.soul.soa_additions.tr.block.AstralWardBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TrBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ThaumicRemnants.MODID);

    public static final RegistryObject<BlockEntityType<AstralWardBlockEntity>> ASTRAL_WARD =
            BLOCK_ENTITIES.register("astral_ward",
                    () -> BlockEntityType.Builder
                            .of(AstralWardBlockEntity::new, TrBlocks.ASTRAL_WARD.get())
                            .build(null));

    private TrBlockEntities() {}

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
