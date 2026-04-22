package com.soul.soa_additions.tconstructevo;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.tconstructevo.entity.MagicMissileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Entities added by TConstructEvo. Currently just the Magic Missile projectile
 * used by the Sceptre tool.
 */
public final class TConEvoEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "tconevo");

    public static final RegistryObject<EntityType<MagicMissileEntity>> MAGIC_MISSILE =
            ENTITIES.register("magic_missile", () -> EntityType.Builder
                    .<MagicMissileEntity>of(MagicMissileEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(new ResourceLocation("tconevo", "magic_missile").toString()));

    private TConEvoEntities() {}

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
