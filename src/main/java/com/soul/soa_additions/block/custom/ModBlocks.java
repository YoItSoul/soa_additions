package com.soul.soa_additions.block.custom;

import com.soul.soa_additions.soa_additions;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, soa_additions.MODID);

    // Registering ore blocks
// Registering ore blocks
    public static final RegistryObject<Block> INFERNIUM_ORE_BLOCK = BLOCKS.register("infernium_ore_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(50f, 3600000.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> VOID_ORE_BLOCK = BLOCKS.register("void_ore_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(50f, 3600000.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> ABYSSAL_ORE_BLOCK = BLOCKS.register("abyssal_ore_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(50f, 3600000.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> ETHER_ORE_BLOCK = BLOCKS.register("ether_ore_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(50f, 3600000.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
