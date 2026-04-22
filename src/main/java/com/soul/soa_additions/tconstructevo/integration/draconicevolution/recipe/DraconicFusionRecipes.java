package com.soul.soa_additions.tconstructevo.integration.draconicevolution.recipe;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers the custom fusion recipe serializer used by the DE tool-upgrade
 * recipes. The recipes themselves share DE's {@code FUSION_RECIPE_TYPE} via
 * {@link DraconicUpgradeFusionRecipe}'s parent class, so the crafting core
 * iterates them alongside stock DE recipes without any extra wiring.
 */
public final class DraconicFusionRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "tconevo");

    public static final RegistryObject<RecipeSerializer<DraconicUpgradeFusionRecipe>> DRACONIC_TOOL_UPGRADE_SERIALIZER =
            SERIALIZERS.register("draconic_tool_upgrade", DraconicUpgradeFusionRecipe.Serializer::new);

    private DraconicFusionRecipes() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }
}
