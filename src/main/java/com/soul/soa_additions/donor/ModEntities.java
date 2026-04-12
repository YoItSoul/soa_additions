package com.soul.soa_additions.donor;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Entity type registrations for the donor system.
 */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SoaAdditions.MODID);

    public static final RegistryObject<EntityType<DonorOrbEntity>> DONOR_ORB =
            ENTITY_TYPES.register("donor_orb", () ->
                    EntityType.Builder.<DonorOrbEntity>of(DonorOrbEntity::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .fireImmune()
                            .noSummon()
                            .build("donor_orb"));

    private ModEntities() {}

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
