package com.soul.soa_additions.worldgen;

import com.google.common.collect.ImmutableSet;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Exposes uru_ore as a locatable Point-of-Interest so players can run
 * {@code /locate poi soa_additions:uru_meteor} to find the nearest End-dim
 * uru meteor cube. POIManager auto-scans every loaded chunk for block states
 * matching a registered PoiType, so this registration alone is sufficient —
 * no per-place bookkeeping required.
 */
public final class ModPoi {

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, SoaAdditions.MODID);

    public static final RegistryObject<PoiType> URU_METEOR = POI_TYPES.register("uru_meteor",
            () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.URU_ORE.get().getStateDefinition().getPossibleStates()),
                    0, 1));

    private ModPoi() {}

    public static void register(IEventBus modEventBus) {
        POI_TYPES.register(modEventBus);
    }
}
