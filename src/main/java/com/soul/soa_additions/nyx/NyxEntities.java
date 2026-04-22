package com.soul.soa_additions.nyx;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.nyx.entity.CauldronTrackerEntity;
import com.soul.soa_additions.nyx.entity.FallingMeteorEntity;
import com.soul.soa_additions.nyx.entity.FallingStarEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class NyxEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SoaAdditions.MODID);

    public static final RegistryObject<EntityType<FallingStarEntity>> FALLING_STAR = ENTITY_TYPES.register(
            "falling_star",
            () -> EntityType.Builder.<FallingStarEntity>of(FallingStarEntity::new, MobCategory.MISC)
                    .sized(0.98f, 0.98f)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("falling_star"));

    public static final RegistryObject<EntityType<FallingMeteorEntity>> FALLING_METEOR = ENTITY_TYPES.register(
            "falling_meteor",
            () -> EntityType.Builder.<FallingMeteorEntity>of(FallingMeteorEntity::new, MobCategory.MISC)
                    .sized(2.98f, 2.98f)
                    .clientTrackingRange(32)
                    .updateInterval(1)
                    .build("falling_meteor"));

    public static final RegistryObject<EntityType<CauldronTrackerEntity>> CAULDRON_TRACKER = ENTITY_TYPES.register(
            "cauldron_tracker",
            () -> EntityType.Builder.<CauldronTrackerEntity>of(CauldronTrackerEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .noSummon()
                    .clientTrackingRange(16)
                    .updateInterval(20)
                    .build("cauldron_tracker"));

    private NyxEntities() {}
    public static void register(IEventBus bus) { ENTITY_TYPES.register(bus); }
}
