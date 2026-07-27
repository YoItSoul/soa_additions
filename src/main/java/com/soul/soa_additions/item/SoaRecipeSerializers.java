package com.soul.soa_additions.item;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SoaRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SoaAdditions.MODID);

    public static final RegistryObject<RecipeSerializer<TabletDupeRecipe>> TABLET_DUPE =
            RECIPE_SERIALIZERS.register("tablet_dupe",
                    () -> new SimpleCraftingRecipeSerializer<>(TabletDupeRecipe::new));

    private SoaRecipeSerializers() {}

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
