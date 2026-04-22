package com.soul.soa_additions.bloodarsenal;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.bloodarsenal.tile.AltareBlockEntity;
import com.soul.soa_additions.bloodarsenal.tile.BloodCapacitorBlockEntity;
import com.soul.soa_additions.bloodarsenal.tile.StasisPlateBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Deferred register for Blood Arsenal block entities.
 */
public final class BABlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "bloodarsenal");

    public static final RegistryObject<BlockEntityType<StasisPlateBlockEntity>> STASIS_PLATE =
            TYPES.register("stasis_plate",
                    () -> BlockEntityType.Builder.of(StasisPlateBlockEntity::new, BABlocks.STASIS_PLATE.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<AltareBlockEntity>> ALTARE =
            TYPES.register("altare",
                    () -> BlockEntityType.Builder.of(AltareBlockEntity::new, BABlocks.ALTARE.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<BloodCapacitorBlockEntity>> BLOOD_CAPACITOR =
            TYPES.register("blood_capacitor",
                    () -> BlockEntityType.Builder.of(BloodCapacitorBlockEntity::new, BABlocks.BLOOD_CAPACITOR.get())
                            .build(null));

    private BABlockEntities() {}

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
    }
}
