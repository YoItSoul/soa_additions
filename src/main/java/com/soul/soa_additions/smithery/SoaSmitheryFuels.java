package com.soul.soa_additions.smithery;

import com.soul.smithery.api.forge.ForgeFuels;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public final class SoaSmitheryFuels {

    public static void register() {
        registerFuel("infernium", 600.0f);
        registerFuel("cosmilite", 2400.0f);
        registerFuel("protonium", 100.0f);
        registerFuel("electronium", 200.0f);
        registerFuel("experience", 200.0f);
        registerFuel("ancient_debris", 200.0f);
        registerFuel("scorched", 200.0f);
        registerFuel("orichalcos", 200.0f);
        registerFuel("gaia", 200.0f);
        registerFuel("ravaging", 200.0f);
        registerFuel("mithminite", 200.0f);
        registerFuel("mithrillium", 200.0f);
        registerFuel("adaminite", 200.0f);
        registerFuel("netherite", 200.0f);
        registerFuel("terra_alloy", 200.0f);
        registerFuel("fierymetal", 200.0f);
        registerFuel("insanium", 200.0f);
        registerFuel("fusion_matrix", 200.0f);
        registerFuel("meteor", 200.0f);
        registerFuel("crimsonite", 200.0f);
    }

    private static void registerFuel(String fluidId, float tempC) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("soa_additions", fluidId);
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid != null) {
            ForgeFuels.register(fluid, new ForgeFuels.Profile(tempC));
        }
    }

    private SoaSmitheryFuels() {}
}