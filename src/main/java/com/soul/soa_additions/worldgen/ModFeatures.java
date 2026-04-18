package com.soul.soa_additions.worldgen;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, SoaAdditions.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> URU_OBSIDIORITE =
            FEATURES.register("uru_obsidiorite", () -> new UruObsidioriteFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
