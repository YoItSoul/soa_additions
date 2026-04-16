package com.soul.soa_additions.bloodarsenal;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

/**
 * Deferred register for Blood Arsenal fluids — Refined Life Essence.
 */
public final class BAFluids {

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, SoaAdditions.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, SoaAdditions.MODID);

    // Fluid Type
    public static final RegistryObject<FluidType> REFINED_LIFE_ESSENCE_TYPE = FLUID_TYPES.register(
            "ba_refined_life_essence",
            () -> new FluidType(FluidType.Properties.create()
                    .density(1200)
                    .viscosity(1200)
                    .temperature(310)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private static final ResourceLocation STILL =
                                new ResourceLocation(SoaAdditions.MODID, "block/ba_refined_life_essence_still");
                        private static final ResourceLocation FLOWING =
                                new ResourceLocation(SoaAdditions.MODID, "block/ba_refined_life_essence_flowing");

                        @Override
                        public ResourceLocation getStillTexture() { return STILL; }

                        @Override
                        public ResourceLocation getFlowingTexture() { return FLOWING; }

                        @Override
                        public int getTintColor() { return 0xFFCC0000; } // Dark red tint
                    });
                }
            });

    // Source & Flowing fluids
    public static final RegistryObject<FlowingFluid> REFINED_LIFE_ESSENCE_SOURCE = FLUIDS.register(
            "ba_refined_life_essence",
            () -> new ForgeFlowingFluid.Source(BAFluids.refinedLifeEssenceProperties()));

    public static final RegistryObject<FlowingFluid> REFINED_LIFE_ESSENCE_FLOWING = FLUIDS.register(
            "ba_refined_life_essence_flowing",
            () -> new ForgeFlowingFluid.Flowing(BAFluids.refinedLifeEssenceProperties()));

    // Fluid Block (registered in BABlocks)
    public static RegistryObject<LiquidBlock> REFINED_LIFE_ESSENCE_BLOCK;

    // Bucket (registered in BAItems)
    public static RegistryObject<Item> REFINED_LIFE_ESSENCE_BUCKET;

    private static ForgeFlowingFluid.Properties refinedLifeEssenceProperties() {
        return new ForgeFlowingFluid.Properties(
                REFINED_LIFE_ESSENCE_TYPE,
                REFINED_LIFE_ESSENCE_SOURCE,
                REFINED_LIFE_ESSENCE_FLOWING)
                .slopeFindDistance(3)
                .levelDecreasePerBlock(2)
                .block(() -> REFINED_LIFE_ESSENCE_BLOCK.get())
                .bucket(() -> REFINED_LIFE_ESSENCE_BUCKET.get());
    }

    private BAFluids() {}

    public static void register(IEventBus modEventBus) {
        FLUIDS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
    }
}
