package com.soul.soa_additions.block.entity;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SoaAdditions.MODID);

    public static final RegistryObject<BlockEntityType<GroveBlockEntity>> GROVE_SPAWN_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("grove_spawn_block_entity",
                    () -> BlockEntityType.Builder.of(GroveBlockEntity::new, ModBlocks.GROVE_SPAWN_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<GroveBoonBlockEntity>> GROVE_BOON_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("grove_boon_block_entity",
                    () -> BlockEntityType.Builder.of(GroveBoonBlockEntity::new, ModBlocks.GROVE_BOON_BLOCK.get()).build(null));

    private ModBlockEntities() {}

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
