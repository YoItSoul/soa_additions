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

    // ============================================================
    // Additions framework blocks — 1:1 port of GC Additions JSON pack.
    // ============================================================

    public static final RegistryObject<Block> AEROITE_ORE    = registerBlock("aeroite_ore",    () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> AQUALITE_ORE   = registerBlock("aqualite_ore",   () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> ASGARDIUM_ORE  = registerBlock("asgardium_ore",  () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> CHROMIUM_ORE   = registerBlock("chromium_ore",   () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> CRYONIUM_ORE   = registerBlock("cryonium_ore",   () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> CYTOSINITE_ORE = registerBlock("cytosinite_ore", () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> EXPERIENCE_ORE = registerBlock("experience_ore", () -> new Block(ORE_PROPERTIES));
    // infernium_ore intentionally not registered — duplicates pre-existing INFERNIUM_ORE_BLOCK above.
    public static final RegistryObject<Block> MANGANESE_ORE  = registerBlock("manganese_ore",  () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> SHADOWIUM_ORE  = registerBlock("shadowium_ore",  () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> SHADOWNIUM_ORE = registerBlock("shadownium_ore", () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> TITANIUM_ORE   = registerBlock("titanium_ore",   () -> new Block(ORE_PROPERTIES));

    public static final RegistryObject<Block> AEONSTEEL_BLOCK             = registerBlock("aeonsteel_block",             () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> AQUALITE_BLOCK              = registerBlock("aqualite_block",              () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> ASTRAL_METAL_BLOCK          = registerBlock("astral_metal_block",          () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> CHROMASTEEL_BLOCK           = registerBlock("chromasteel_block",           () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> COSMILITE_BLOCK             = registerBlock("cosmilite_block",             () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> CRIMSONITE_BLOCK            = registerBlock("crimsonite_block",            () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> CRYONIUM_BLOCK              = registerBlock("cryonium_block",              () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> CYTOSINITE_BLOCK            = registerBlock("cytosinite_block",            () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> DURASTEEL_BLOCK             = registerBlock("durasteel_block",             () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> ELECTRONIUM_BLOCK           = registerBlock("electronium_block",           () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> EXPERIENCE_BLOCK_BLOCK      = registerBlock("experience_block",            () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> FUSION_MATRIX_BLOCK         = registerBlock("fusion_matrix_block",         () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> INFERNIUM_BLOCK             = registerBlock("infernium_block",             () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> MANGANESE_STEEL_BLOCK       = registerBlock("manganese_steel_block",       () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> MATTER_BLOCK                = registerBlock("matter_block",                () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> MODULARIUM_BLOCK            = registerBlock("modularium_block",            () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> PROTONIUM_BLOCK             = registerBlock("protonium_block",             () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> STAINLESS_STEEL_BLOCK       = registerBlock("stainless_steel_block",       () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> TERRA_ALLOY_BLOCK           = registerBlock("terra_alloy_block",           () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> TITANIUM_BLOCK              = registerBlock("titanium_block",              () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> COMPRESSED_EXPERIENCE_BLOCK = registerBlock("compressed_experience_block", () -> new Block(METAL_BLOCK_PROPS));

    public static final RegistryObject<Block> HARDENED_STONE              = registerBlock("hardened_stone",              () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> INFINITY_BLOCK_BLOCK        = registerBlock("infinity_block_block",        () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> INFINITY_BLOCK_BLOCK_BLOCK  = registerBlock("infinity_block_block_block",  () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> UNKNOWN_BLOCK               = registerBlock("unknown_block",               () -> new Block(ORE_PROPERTIES));

    // External 1.12 mod ports (Thermal Foundation, TConstruct, ExtraUtilities2)
    public static final RegistryObject<Block> IRIDIUM_BLOCK               = registerBlock("iridium_block",               () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> IRIDIUM_ORE                 = registerBlock("iridium_ore",                 () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> PLATINUM_BLOCK              = registerBlock("platinum_block",              () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> PLATINUM_ORE                = registerBlock("platinum_ore",                () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> ARDITE_BLOCK                = registerBlock("ardite_block",                () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> ARDITE_ORE                  = registerBlock("ardite_ore",                  () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> EVIL_METAL_BLOCK            = registerBlock("evil_metal_block",            () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> DEMONIC_METAL_BLOCK         = registerBlock("demonic_metal_block",         () -> new Block(METAL_BLOCK_PROPS));

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
