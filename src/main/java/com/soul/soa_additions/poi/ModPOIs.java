package com.soul.soa_additions.poi;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public final class ModPOIs {

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, SoaAdditions.MODID);

    public static final RegistryObject<PoiType> SHRINE_BOON_POI = POI_TYPES.register("shrine_boon_block",
            () -> new PoiType(Set.of(ModBlocks.GROVE_BOON_BLOCK.get().defaultBlockState()), 1, 1));

    private ModPOIs() {}

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }
}
