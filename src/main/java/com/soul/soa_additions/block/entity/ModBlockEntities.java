package com.soul.soa_additions.block.entity;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.nyx.NyxBlocks;
import com.soul.soa_additions.nyx.block.CrystalBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SoaAdditions.MODID);

    public static final RegistryObject<BlockEntityType<CrystalBlockEntity>> CRYSTAL =
            BLOCK_ENTITY_TYPES.register("crystal",
                    () -> BlockEntityType.Builder.of(CrystalBlockEntity::new, NyxBlocks.CRYSTAL.get())
                            .build(null));

    private ModBlockEntities() {}

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
