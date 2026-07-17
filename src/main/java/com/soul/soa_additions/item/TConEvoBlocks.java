package com.soul.soa_additions.item;

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

public final class TConEvoBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TConEvoItems.TCONEVO_ID);

    private static final BlockBehaviour.Properties METAL_BLOCK_PROPS =
            BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK)
                    .mapColor(MapColor.METAL)
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops();

    private static final BlockBehaviour.Properties PINK_SLIMY_MUD_PROPS =
            BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.DIRT)
                    .mapColor(MapColor.COLOR_PINK)
                    .strength(0.6f)
                    .sound(SoundType.SLIME_BLOCK);

    public static final RegistryObject<Block> BOUND_METAL_BLOCK     = registerBlock("bound_metal_block",     () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> CHAOTIC_BLOCK         = registerBlock("chaotic_block",         () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> DRACONIC_METAL_BLOCK  = registerBlock("draconic_metal_block",  () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> ENERGETIC_METAL_BLOCK = registerBlock("energetic_metal_block", () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> ESSENCE_METAL_BLOCK   = registerBlock("essence_metal_block",   () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> PRIMAL_METAL_BLOCK    = registerBlock("primal_metal_block",    () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> SENTIENT_METAL_BLOCK  = registerBlock("sentient_metal_block",  () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> UNIVERSAL_METAL_BLOCK = registerBlock("universal_metal_block", () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> WYVERN_BLOCK          = registerBlock("wyvern_block",          () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> PINK_SLIMY_MUD        = registerBlock("pink_slimy_mud",        () -> new Block(PINK_SLIMY_MUD_PROPS));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        TConEvoItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    private TConEvoBlocks() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
