package com.soul.soa_additions.loot.artifact;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry wiring for the TConEvo artifact port. The GLM serializer id must be
 * {@code tconevo:artifact} — the 12 loot_modifier JSONs and the forge
 * global_loot_modifiers index reference it verbatim from the 1.12 pack.
 */
public final class TconevoArtifacts {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "tconevo");

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "tconevo");

    public static final RegistryObject<Codec<ArtifactLootModifier>> ARTIFACT_GLM =
            GLM_SERIALIZERS.register("artifact", () -> ArtifactLootModifier.CODEC);

    public static final RegistryObject<RecipeSerializer<UnsealArtifactRecipe>> UNSEAL_SERIALIZER =
            RECIPE_SERIALIZERS.register("unseal_artifact",
                    () -> new SimpleCraftingRecipeSerializer<>(UnsealArtifactRecipe::new));

    private TconevoArtifacts() {}

    public static void register(IEventBus modEventBus) {
        GLM_SERIALIZERS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
