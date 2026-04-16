package com.soul.soa_additions.block;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SoaAdditions.MODID);

    private static final BlockBehaviour.Properties ORE_PROPERTIES = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.STONE)
            .mapColor(MapColor.STONE)
            .strength(50f, 1200f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE);

    // Ores
    public static final RegistryObject<Block> INFERNIUM_ORE_BLOCK = registerBlock("infernium_ore_block", () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> VOID_ORE_BLOCK      = registerBlock("void_ore_block",      () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> ABYSSAL_ORE_BLOCK   = registerBlock("abyssal_ore_block",   () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> ETHER_ORE_BLOCK     = registerBlock("ether_ore_block",     () -> new Block(ORE_PROPERTIES));

    private ModBlocks() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }
}
