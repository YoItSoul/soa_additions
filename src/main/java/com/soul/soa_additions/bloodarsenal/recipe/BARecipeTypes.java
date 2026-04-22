package com.soul.soa_additions.bloodarsenal.recipe;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for Blood Arsenal recipe types and serializers.
 */
public final class BARecipeTypes {

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, "bloodarsenal");
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "bloodarsenal");

    public static final RegistryObject<RecipeType<SanguineInfusionRecipe>> SANGUINE_INFUSION_TYPE =
            TYPES.register("sanguine_infusion", () -> new RecipeType<>() {
                @Override
                public String toString() { return "bloodarsenal:sanguine_infusion"; }
            });

    public static final RegistryObject<RecipeSerializer<SanguineInfusionRecipe>> SANGUINE_INFUSION_SERIALIZER =
            SERIALIZERS.register("sanguine_infusion", SanguineInfusionRecipe.Serializer::new);

    private BARecipeTypes() {}

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
        SERIALIZERS.register(modEventBus);
    }
}
