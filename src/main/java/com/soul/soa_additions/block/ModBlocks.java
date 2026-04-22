package com.soul.soa_additions.block;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
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

    // Obsidian-like shell host in End cube formations
    private static final BlockBehaviour.Properties OBSIDIORITE_PROPERTIES = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.OBSIDIAN)
            .mapColor(MapColor.COLOR_BLACK)
            .strength(50f, 1200f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE);

    // Uru ore: End-exclusive, netherite tier
    private static final BlockBehaviour.Properties URU_ORE_PROPERTIES = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.OBSIDIAN)
            .mapColor(MapColor.COLOR_RED)
            .strength(60f, 1800f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE);

    private static final BlockBehaviour.Properties URU_BLOCK_PROPERTIES = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHERITE_BLOCK)
            .mapColor(MapColor.COLOR_RED)
            .strength(60f, 1800f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.METAL);

    // Ores
    public static final RegistryObject<Block> INFERNIUM_ORE_BLOCK = registerBlock("infernium_ore_block", () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> VOID_ORE_BLOCK      = registerBlock("void_ore_block",      () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> ABYSSAL_ORE_BLOCK   = registerBlock("abyssal_ore_block",   () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> ETHER_ORE_BLOCK     = registerBlock("ether_ore_block",     () -> new Block(ORE_PROPERTIES));

    // End-exclusive taiga-style formations
    public static final RegistryObject<Block> OBSIDIORITE = registerBlock("obsidiorite", () -> new Block(OBSIDIORITE_PROPERTIES));
    public static final RegistryObject<Block> URU_ORE     = registerBlock("uru_ore",     () -> new Block(URU_ORE_PROPERTIES));
    public static final RegistryObject<Block> URU_BLOCK   = registerBlock("uru_block",   () -> new Block(URU_BLOCK_PROPERTIES));

    // TConEvo metal storage blocks (9 metals). Iron-block-like properties.
    private static final BlockBehaviour.Properties METAL_BLOCK_PROPS = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK)
            .mapColor(MapColor.METAL)
            .strength(5.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops();

    public static final RegistryObject<Block> BOUND_METAL_BLOCK     = registerBlock("bound_metal_block",     () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> CHAOTIC_BLOCK         = registerBlock("chaotic_block",         () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> DRACONIC_METAL_BLOCK  = registerBlock("draconic_metal_block",  () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> ENERGETIC_METAL_BLOCK = registerBlock("energetic_metal_block", () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> ESSENCE_METAL_BLOCK   = registerBlock("essence_metal_block",   () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> PRIMAL_METAL_BLOCK    = registerBlock("primal_metal_block",    () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> SENTIENT_METAL_BLOCK  = registerBlock("sentient_metal_block",  () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> UNIVERSAL_METAL_BLOCK = registerBlock("universal_metal_block", () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> WYVERN_BLOCK          = registerBlock("wyvern_block",          () -> new Block(METAL_BLOCK_PROPS));

    // TConEvo pink slimy mud — slime-dropping earth block
    private static final BlockBehaviour.Properties PINK_SLIMY_MUD_PROPS = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.DIRT)
            .mapColor(MapColor.COLOR_PINK)
            .strength(0.6f)
            .sound(SoundType.SLIME_BLOCK);
    public static final RegistryObject<Block> PINK_SLIMY_MUD = registerBlock("pink_slimy_mud", () -> new Block(PINK_SLIMY_MUD_PROPS));

    // Nyx blocks
    private static final BlockBehaviour.Properties METEOR_BLOCK_PROPS = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK)
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(5.0f, 1000.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops();
    private static final BlockBehaviour.Properties STAR_BLOCK_PROPS = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.STONE)
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .strength(3.0f, 6.0f)
            .sound(SoundType.STONE)
            .lightLevel(s -> 7)
            .requiresCorrectToolForDrops();

    public static final RegistryObject<Block> METEOR_BLOCK        = registerBlock("meteor_block",        () -> new Block(METEOR_BLOCK_PROPS));
    public static final RegistryObject<Block> STAR_BLOCK          = registerBlock("star_block",          () -> new Block(STAR_BLOCK_PROPS));
    public static final RegistryObject<Block> CHISELED_STAR_BLOCK = registerBlock("chiseled_star_block", () -> new Block(STAR_BLOCK_PROPS));
    public static final RegistryObject<Block> STAR_SLAB           = registerBlock("star_slab",           () -> new SlabBlock(STAR_BLOCK_PROPS));
    public static final RegistryObject<Block> STAR_STAIRS         = registerBlock("star_stairs",         () -> new StairBlock(() -> STAR_BLOCK.get().defaultBlockState(), STAR_BLOCK_PROPS));

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
